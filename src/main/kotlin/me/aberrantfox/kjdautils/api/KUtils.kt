package me.aberrantfox.kjdautils.api

import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.annotation.Data
import me.aberrantfox.kjdautils.api.annotation.Precondition
import me.aberrantfox.kjdautils.api.annotation.PreconditionKind
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.internal.command.*
import me.aberrantfox.kjdautils.internal.di.DIService
import me.aberrantfox.kjdautils.internal.event.EventRegister
import me.aberrantfox.kjdautils.internal.listeners.CommandListener
import me.aberrantfox.kjdautils.internal.listeners.ConversationListener
import me.aberrantfox.kjdautils.internal.logging.BotLogger
import me.aberrantfox.kjdautils.internal.logging.DefaultLogger
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import kotlin.system.exitProcess


class KUtils(val config: KConfiguration) {
    val discord = Discord.build(config)

    private var listener: CommandListener? = null
    private var executor: CommandExecutor? = null
    private val helpService: HelpService
    private val diService = DIService()

    init {
        registerInjectionObject(discord)
    }

    val conversationService: ConversationService = ConversationService(discord, config, diService)
    val container = CommandsContainer()
    var logger: BotLogger = DefaultLogger()

    init {
        registerInjectionObject(conversationService)
        discord.addEventListener(EventRegister)
        helpService = HelpService(container, config)
        registerListeners(ConversationListener(conversationService))
    }

    fun registerInjectionObject(vararg obj: Any) = obj.forEach { diService.addElement(it) }

    fun registerCommandPreconditions(vararg conditions: (CommandEvent) -> PreconditionResult) = listener?.addPreconditions(*conditions)

    fun registerOverridingPreconditions(vararg conditions: (CommandEvent) -> PreconditionResult) = listener?.addOverridingPreconditions(*conditions)
    
    fun configure(setup: KConfiguration.() -> Unit) {
        config.setup()

        detectData()
        detectServices()

        registerCommands()
        registerListenersByPath()
        registerPreconditionsByPath()
        conversationService.registerConversations(config.globalPath)
        setupPermissionManager()
    }

    fun registerListeners(vararg listeners: Any) = listeners.forEach { EventRegister.eventBus.register(it) }

    private fun setupPermissionManager() = config.permissionManager?.let {
        registerCommandPreconditions(it.producePrecondition())
        diService.addElement(it)
    }

    private fun registerCommands(): CommandsContainer {
        val localContainer = produceContainer(config.globalPath, diService)
        CommandRecommender.addAll(localContainer.listCommands())

        val executor = CommandExecutor()
        val listener = CommandListener(config, container, logger, discord, executor)

        this.container.join(localContainer)
        this.executor = executor
        this.listener = listener

        registerListeners(listener)
        return container
    }

    private fun registerListenersByPath() {
        Reflections(config.globalPath, MethodAnnotationsScanner()).getMethodsAnnotatedWith(Subscribe::class.java)
                .map { it.declaringClass }
                .distinct()
                .map { diService.invokeConstructor(it) }
                .forEach { registerListeners(it) }
    }

    private fun registerPreconditionsByPath() {
        val preconditions = Reflections(config.globalPath, MethodAnnotationsScanner()).getMethodsAnnotatedWith(Precondition::class.java)
            .map {
                val kind = (it.annotations.first() as Precondition).kind
                val precondition = diService.invokeReturningMethod(it) as ((CommandEvent) -> PreconditionResult)
                Pair(kind, precondition)
            }

        preconditions.filter { it.first == PreconditionKind.AllOf }.forEach { registerCommandPreconditions(it.second) }
        preconditions.filter { it.first == PreconditionKind.OneOf }.forEach { registerOverridingPreconditions(it.second) }
    }

    private fun detectServices() {
        val services = Reflections(config.globalPath).getTypesAnnotatedWith(Service::class.java)
        diService.invokeDestructiveList(services)
    }

    private fun detectData() {
        val data = Reflections(config.globalPath).getTypesAnnotatedWith(Data::class.java)
        val fillInData = diService.collectDataObjects(data)

        exitIfDataNeedsToBeFilledIn(fillInData)
    }

    private fun exitIfDataNeedsToBeFilledIn(data: ArrayList<String>) {
        if(data.isEmpty()) return

        val dataString = data.joinToString(", ", postfix = ".")

        println("The below data files were generated and must be filled in before re-running.")
        println(dataString)
        exitProcess(0)
    }
}

fun startBot(token: String, operate: KUtils.() -> Unit = {}): KUtils {
    val util = KUtils(KConfiguration(token))
    util.operate()
    return util
}
