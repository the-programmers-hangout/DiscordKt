package me.aberrantfox.kjdautils.examples.dependencyinjectionbot

import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.api.startBot
import me.aberrantfox.kjdautils.internal.command.arguments.SentenceArg

fun main(args: Array<String>) {
    val token = args.firstOrNull() ?: System.getenv("BOT_TOKEN")

    if(token == null) {
        println("Please provide a bot token")
        return
    }

    startBot(token) {
        configure {
            globalPath = "me.aberrantfox.kjdautils.examples.dependencyinjectionbot"
            prefix = "!"
        }
    }
}

//Sometimes, your commands will need to depend on a database, or on a piece of functionality that you have
//defined elsewhere in your codebase.
//KUtils completely handles this injection. This removes the need to create instances and pass them around yourself.
//Simply mark all of your injectable object with the appropriate annotation, and let KUtils do the work.

@CommandSet("ExampleCategory")
//This is an example of how we provide dependencies to KUtils command sets. Just ask for it as a parameter.
//If you've annotated it properly, it will be registered and passed into this CommandSet automatically.
fun exampleCommandSet(formatter: Formatter) = commands {
    command("message") {
        description = "Format a message to be pretty"
        expect(SentenceArg)
        execute {
            val msg = it.args.first() as String
            it.respond(formatter.formatPrettyString(msg))
        }
    }
}

//All Services should be tagged with the @Service annotation
@Service
class Formatter {
    //Here is an example function that you may want in a service
    fun formatPrettyString(msg: String) = "Pretty Prefix: $msg"
}

//Along with injecting things like configuration Data, Services can also depend on other Services.
//KUtils will sort out how to create them in the correct order, provided there isn't a cyclic dependency.

@Service
class NoDependencies

@Service
class SingleDependency(noDependencies: NoDependencies)

@Service
class DoubleDependency(noDependencies: NoDependencies, singleDependency: SingleDependency) {
    fun pong() = "Pong!"
}

@CommandSet("ExampleCategory2")
fun secondExample(doubleDependency: DoubleDependency) = commands {
    command("pong") {
        description = "Pong!"
        execute {
            it.respond(doubleDependency.pong())
        }
    }
}