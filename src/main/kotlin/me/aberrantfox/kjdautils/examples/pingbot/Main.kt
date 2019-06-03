package me.aberrantfox.kjdautils.examples.pingbot

import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.api.startBot

fun main(args: Array<String>) {
    val token = args.firstOrNull() ?: System.getenv("BOT_TOKEN")

    if(token == null) {
        println("Please provide a bot token")
        return
    }

    //Call the start procedure for the bot.
    startBot(token) {
        //configure the globalPath for your bot
        //For this example bot, it's set to the package path of the ping bot so that it does not
        //load up any extra things from other packages.
        //You should make it your root package,
        //e.g. me.awesomedeveloper.myawesomebot
        //That will allow KUtils to pick up all of your commands and such
        configure {
            globalPath = "me.aberrantfox.kjdautils.examples.pingbot"
            prefix = "!"
        }
    }
}

@CommandSet("ExampleCategory")
fun createExampleCommands() = commands {
    command("ping") {
        description = "Pong!"
        execute {
            val currentPingMS = it.discord.jda.ping
            it.respond("Pong! ($currentPingMS ms)")
        }
    }
}