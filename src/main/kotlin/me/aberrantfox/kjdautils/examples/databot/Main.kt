package me.aberrantfox.kjdautils.examples.databot

import me.aberrantfox.kjdautils.api.annotation.Data
import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.api.startBot

fun main(args: Array<String>) {
    val token = args.firstOrNull() ?: System.getenv("BOT_TOKEN")

    if(token == null) {
        println("Please provide a bot token")
        return
    }

    startBot(token) {
        configure {
            globalPath = "me.aberrantfox.kjdautils.examples.databot"
            prefix = "!"
        }
    }
}

@CommandSet("ExampleCategory")
//data objects can be passed around just like services can. They cannot, however, depend on a service.
//You can pass the discord object into a data object.
fun exampleCommandCategory(config: BotConfiguration) = commands {
    command("owner") {
        description = "Display the server owner's id as defined by the config"
        execute {
            it.respond(config.ownerID)
        }
    }
}

//Classes that you annotate with @Data are automatically saved into a .json file at the path you specify.
//In this case, the path specified is `bot/configuration/config.json`. This is how this will work in the framework
// 1. KUtils detects that there is a class annotated with @Data
// 2. KUtils reads the provided path, it then attempts to find the path **relatively**
// 3. If there is a well formed JSON file there, it will load it into the object. This makes it available for other
//    data objects, services, command sets and preconditions.
// 4. If there is no file found, it will call the default constructor (as such you **must** provide default arguments)
// 5. With the newly generated object, it will save the json generated from it to the specified path
// 6. KUtils will do this for all data objects. If any of them are marked as `killIfGenerated` it will stop the bot
//    immediately, and output a message instructing the user to fill out the file.

// Note: It is recommend to have the default values as instructions for what they should be, like `insert-owner-id`
@Data("bot/configuration/config.json", killIfGenerated = true)
data class BotConfiguration(val ownerID: String = "insert-owner-id", val staffRoleId: String = "insert-role-id")