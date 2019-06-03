package me.aberrantfox.kjdautils.examples.listenerbot

import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.api.startBot
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

fun main(args: Array<String>) {
    val token = args.firstOrNull() ?: System.getenv("BOT_TOKEN")

    if(token == null) {
        println("Please provide a bot token")
        return
    }

    startBot(token) {
        configure {
            globalPath = "me.aberrantfox.kjdautils.examples.listenerbot"
            prefix = "!"
        }
    }
}

//Making a listener is incredibly easy. You need to do the following:
// 1. Define a class
// 2. Define a function in that class
// 3. Annotate that function with @Subscribe
// 4. Set it so that the function takes the event you want to listen to as an argument,
//    in this case, I used GuildMessageReceivedEvent, however you can use any from here:
//    https://ci.dv8tion.net/job/JDA/javadoc/net/dv8tion/jda/core/events/Event.html -- see direct subclasses.
class MyListener {
    @Subscribe
    fun someFunctionName(event: GuildMessageReceivedEvent) {
        println(event.message)
    }
}