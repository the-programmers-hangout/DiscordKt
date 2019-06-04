package me.aberrantfox.kjdautils.examples.argumentsbot

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.arg
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.api.startBot
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType
import me.aberrantfox.kjdautils.internal.command.arguments.IntegerArg
import me.aberrantfox.kjdautils.internal.command.arguments.SentenceArg
import me.aberrantfox.kjdautils.internal.command.arguments.UserArg
import me.aberrantfox.kjdautils.internal.command.arguments.WordArg
import net.dv8tion.jda.core.entities.User

fun main(args: Array<String>) {
    val token = args.firstOrNull() ?: System.getenv("BOT_TOKEN")

    if(token == null) {
        println("Please provide a bot token")
        return
    }

    startBot(token) {
        configure {
            globalPath = "me.aberrantfox.kjdautils.examples.argumentsbot"
            prefix = "!"
        }
    }
}

@CommandSet("ExampleCategory")
fun exampleCommandsSet() = commands {
    command("echo") {
        description = "Responds with whatever message you give it"
        //The expect function is where you can specify what type of data you want the bot to receive in a command
        expect(SentenceArg) //Here we use a SentenceArg, which takes the entire message minus the command name and prefix
        execute {
            //We can then extract the sentence that was requested in the 'expect'
            val whatTheySaid = it.args.first() as String //All arguments are initially type 'Any', but can be casted to the argument type
            it.respond(whatTheySaid)
        }
    }

    command("add") {
        description = "Add two numbers together"
        //You can expect as many arguments as you want, of any type. In this case, two integers
        expect(IntegerArg, IntegerArg)
        execute {
            //You then extract each of them similarly to how you would for a single argument
            val first = it.args.component1() as Int //You can safely cast them to Int, as that is what IntegerArg guarantees
            val second = it.args.component2() as Int
            it.respond("Result = ${first + second}")
        }
    }

    command("welcome") {
        description = "Welcome a user"
        //Here we leverage the `arg` function to create an optional argument with a default value.

        //If this command is invoked with an argument that satisfies the expected argument, that data will be used.
        //!!welcome Hello -> Hello

        //If this command is invoked without an argument that satisfies the expected argument, the default value will be used.
        //!!welcome -> Welcome to the server!

        //You can use positional or named arguments, but we will use named argument for the sake of clarity :)
        expect(arg(SentenceArg, optional = true, default = "Welcome to the server!"))
        execute {
            val welcomeMessage = it.args.first() as String
            it.respond(welcomeMessage)
        }
    }

    command("country") {
        description = "Display the flag emoji for a country"
        //You can see here that we are expecting a WordArg, which takes in a single word (no spaces).
        //But as you can see, we've passed something extra. This is the display name of the argument.
        //In the case of this command, the word we expect is a country name, so we should label it as such.
        //This means that when a user runs !!help country, the documentation will show 'Country Name' instead of 'Word'
        //This allows you to help users understand what the expected input is for a given command.
        expect(WordArg("Country Name"))
        execute {
            val countryName = it.args.first() as String

            val emoji = when(countryName.toLowerCase()) {
                "ireland" -> "\uD83C\uDDEE\uD83C\uDDEA"
                else -> "Not supported, sorry!!"
            }

            it.respond(emoji)
        }
    }

    command("rate") {
        description = "Rate a user!"
        expect(RatingArg, UserArg)
        execute {
            //We know for certain that our RatingArg 'convert' function returns a result that is of type Rating, so it's
            //safe to cast here. The naming convention for arguments is to prefix the Arg with the conversion type.
            //In this case, a 'RatingArg' produces a 'Rating'. You can see the definition of the Arg below.
            val rating = it.args.component1() as Rating
            val user = it.args.component2() as User

            it.respond("${user.name} was rated ${rating.name} by ${it.author.name}")
        }
    }
}

//To create a custom argument, inherit from ArgumentType
//This defines a few things
//1. A name - self explanatory, but should be named as described above
//2. Consumption type - How many space separated words will this argument consume (a single word; multiple words; all of them)
//3. Examples - A list of strings which are valid string representations of an argument. For integer, "1" is such a string
//4. A convert function which processes the argument or arguments and determines if it was successful

open class RatingArg(override val name: String = "Rating") : ArgumentType {
    //Here, we state that the companion object is just a RatingArg that has been invoked. This way,
    //you can Pass `RatingArg` into the expect function without needing to use the constructor.
    companion object : RatingArg()

    //Here we say that we are consuming just one arg. So we only look at the arg value passed into the convert function
    //and nothing else.
    override val consumptionType: ConsumptionType = ConsumptionType.Single
    //Here we build our examples programmatically, but it can be done by hand.
    //An example from this list will picked from randomly when a user invokes '!!help' on a command.
    //This helps to create better documentation by providing an example of what input the user should give.
    override val examples: ArrayList<String> = ArrayList(Rating.values().map { it.name })

    //Here we write the convert function, which is what converts the input to out desired output type
    override fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult {
        //First we determine if the argument we have is a valid Rating arg
        val result = Rating.values().firstOrNull { it.name.toLowerCase() == arg.toLowerCase() }

        //If it is valid, we can return it wrapped in a Result, and consume that piece of the input.
        //If it is not valid, provide a meaningful error message. You can fail in multiple places if you have a more complicated argument.
        //The error string can be whatever you want, such as a list of valid arguments.
        return if(result == null) ArgumentResult.Error("$arg is not a valid rating.") else ArgumentResult.Single(result)
    }
}

//A simple enum with our valid rating types
enum class Rating {
    Good, Alright, Bad
}