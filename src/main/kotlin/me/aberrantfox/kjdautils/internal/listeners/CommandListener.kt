package me.aberrantfox.kjdautils.internal.listeners


import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.CommandsContainer
import me.aberrantfox.kjdautils.api.dsl.KConfiguration
import me.aberrantfox.kjdautils.api.dsl.PrefixDeleteMode
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.jda.deleteIfExists
import me.aberrantfox.kjdautils.extensions.jda.descriptor
import me.aberrantfox.kjdautils.extensions.jda.isCommandInvocation
import me.aberrantfox.kjdautils.extensions.stdlib.sanitiseMentions
import me.aberrantfox.kjdautils.internal.command.*
import me.aberrantfox.kjdautils.internal.command.CommandExecutor
import me.aberrantfox.kjdautils.internal.logging.BotLogger
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent

internal class CommandListener(val config: KConfiguration,
                               val container: CommandsContainer,
                               var log: BotLogger,
                               val discord: Discord,
                               private val executor: CommandExecutor,
                               private val preconditions: ArrayList<(CommandEvent) -> PreconditionResult> = ArrayList(),
                               private val overridingPreconditions: ArrayList<(CommandEvent) -> PreconditionResult> = ArrayList()) {

    @Subscribe
    fun guildMessageHandler(e: GuildMessageReceivedEvent) =
            handleMessage(e.channel, e.message, e.author, e.guild)

    @Subscribe
    fun privateMessageHandler(e: PrivateMessageReceivedEvent) =
            handleMessage(e.channel, e.message, e.author)


    fun addPreconditions(vararg conditions: (CommandEvent) -> PreconditionResult) = preconditions.addAll(conditions)

    fun addOverridingPreconditions(vararg conditions: (CommandEvent) -> PreconditionResult) = overridingPreconditions.addAll(conditions)

    private fun handleMessage(channel: MessageChannel, message: Message, author: User, guild: Guild? = null) {

        if (!isUsableCommand(message, author)) return

        val commandStruct = cleanCommandMessage(message.contentRaw, config)
        val (commandName, actualArgs, isDoubleInvocation) = commandStruct
        if (commandName.isEmpty())
            return

        val invokedInGuild = guild != null
        val shouldDelete = invokedInGuild && when (config.deleteMode){
            PrefixDeleteMode.Single -> !isDoubleInvocation
            PrefixDeleteMode.Double ->  isDoubleInvocation
            PrefixDeleteMode.None   ->  false
        }

        val event = CommandEvent(commandStruct, message, actualArgs, container,
                discord = discord,
                stealthInvocation = shouldDelete,
                guild = guild
        )

        getPreconditionError(event)?.let {
            if (it != "") {
                event.respond(it)
            }
            return
        }

        val command = container[commandName]

        if (command == null) {
            val recommended = CommandRecommender.recommendCommand(commandName) { config.visibilityPredicate(it, author, channel, guild) }
            val cleanName = commandName.sanitiseMentions()

            if (shouldDelete) message.deleteIfExists()

            channel.sendMessage("I don't know what $cleanName is, perhaps you meant $recommended?").queue()
            return
        }

        if (command.requiresGuild && !invokedInGuild) {
            channel.sendMessage("This command must be invoked in a guild channel and not through PM").queue()
            return
        }

        executor.executeCommand(command, actualArgs, event)

        if (!shouldDelete && config.reactToCommands) message.addReaction("\uD83D\uDC40").queue()


        log.cmd("${author.descriptor()} -- invoked $commandName in ${channel.name}")

        if (shouldDelete) message.deleteIfExists()
    }

    private fun isUsableCommand(message: Message, author: User): Boolean {
        if (message.contentRaw.length > 1500) return false

        if (!(message.isCommandInvocation(config))) return false

        if (author.isBot) return false

        return true
    }

    private fun getPreconditionError(event: CommandEvent): String? {
        val anyOverridingConditionPass = this.overridingPreconditions.any { it.invoke(event) is Pass }

        if(anyOverridingConditionPass) return null

        val failedPrecondition = preconditions
                .map { it.invoke(event) }
                .firstOrNull { it is Fail }

        if (failedPrecondition != null && failedPrecondition is Fail) {
            return failedPrecondition.reason ?: ""
        }

        return null
    }
}
