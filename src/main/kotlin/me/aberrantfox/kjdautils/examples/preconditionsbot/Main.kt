package me.aberrantfox.kjdautils.examples.preconditionsbot

import me.aberrantfox.kjdautils.api.annotation.Precondition
import me.aberrantfox.kjdautils.api.annotation.PreconditionKind
import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.api.dsl.precondition
import me.aberrantfox.kjdautils.api.startBot
import me.aberrantfox.kjdautils.internal.command.Fail
import me.aberrantfox.kjdautils.internal.command.Pass

fun main(args: Array<String>) {
    val token = args.firstOrNull() ?: System.getenv("BOT_TOKEN")

    if(token == null) {
        println("Please provide a bot token")
        return
    }

    startBot(token) {
        configure {
            globalPath = "me.aberrantfox.kjdautils.examples.preconditionsbot"
            prefix = "!"
        }
    }
}

//Here I just crafted a simple ping command.
@CommandSet("ExampleCategory")
fun exampleCommand() = commands {
    command("ping") {
        description = "Returns pong. Only accessible to users with a name that begins with F, though."
        execute {
            it.respond("Pong!")
        }
    }
}

//We must flag the function with @Precondition so that KUtils can pick it up
@Precondition
//here we say that the function is equal to the result of the precondition function
fun ensureNameBeginsWithF() = precondition { event ->
    //here we do our check to filter out valid users. If a valid user is found, we return Pass
    if(event.author.name.toLowerCase().startsWith("f")) return@precondition Pass
    //here we return a fail for those who didn't pass the check
    return@precondition Fail("Your name must begin with `f`!")
}

/**
 * Preconditions are simply checks that must pass before any command can be run. Preconditions
 * take the CommandEvent that will execute as an argument, and will return either Pass, to signal that this
 * condition is okay with this event -- or Fail, with a supplied message to indicate that this event has failed
 * this check and the reason is nested in the fail object.
 *
 *
 * There are two kinds of precondition. The default is allOf -- allOf preconditions are separated from the other kind.
 * Essentially, if a precondition is an allOf precondition, it, as well as *allof* the other ones in this category
 * must return pass. This is not the case for oneOf preconditions. OneOf preconditions only require one of them to pass.
 * A good example of a oneOf precondition might be `the user is the server owner` or `the user is me, the bot developer`.
 *
 * A good example of an allOf precondition might be `the user is not muted` AND `the user is not on the blacklist` AND
 * `the user has the required role`. You get the idea. If you're the bot owner, these checks shouldn't even run on you.
 *
 * Below, you can see an example of a oneOf precondition
 */

//Here is how you specify the kind. Because it is a oneOf precondition, only `one` of these needs to pass in order to
//allow the user to run commands.
@Precondition(kind = PreconditionKind.OneOf)
fun produceOneOfPrecondition() = precondition { event ->
    return@precondition if(event.author.id == "222164217707364362") Pass else Fail
}