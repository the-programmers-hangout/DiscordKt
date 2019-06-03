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
//defined elsewhere in your codebase. It's very tempting to wrap this functionality up into an object, and
//sometimes that is the right solution, however KUtils provides you with a nice mechanism so that you don't
//have to do that.

@CommandSet("ExampleCategory")
//note how we say that we take the formatter as an argument to our function. This is how we provide dependencies
//to kutils command sets.
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

@Service
class Formatter {
    //here is a simple dummy function
    fun formatPrettyString(msg: String) = "Pretty Prefix: $msg"
}

//It's worth noting at this point that you can have Services depend on each other as well, and KUtils will
//very simply figure out how to create them provided there isn't a cyclic dependency.

@Service
class NoDependencies

@Service
class SingleDependency(noDependencies: NoDependencies)

@Service
class DoubleDependency(noDependencies: NoDependencies, singleDependency: SingleDependency) {
    fun pong() = "Pong!"
}

@CommandSet("ExampleCategory2")
//note how you don't need to depend on every service in a command set. If you just depend on a service
//in another service, that's fine.
fun secondExample(doubleDependency: DoubleDependency) = commands {
    command("pong") {
        description = "Pong!"
        execute {
            it.respond(doubleDependency.pong())
        }
    }
}