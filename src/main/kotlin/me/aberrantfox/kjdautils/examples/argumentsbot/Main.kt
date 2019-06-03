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
        //the expect function is where you specify what you want the bot to receive
        expect(SentenceArg) // here we are saying we want a sentence, this means the entire message minus the command name and prefix
        execute {
            //here we are extracting the sentence that we said would be there earlier
            val whatTheySaid = it.args.first() as String //You must cast it to a String, it's currently type Any
            it.respond(whatTheySaid)
        }
    }

    command("add") {
        description = "Add two numbers together"
        //here we simply say that we want two arguments instead of one
        expect(IntegerArg, IntegerArg)
        execute {
            //you must get them both out of the array as well
            val first = it.args.component1() as Int //you must cast them to Integer, as that is what IntegerArg guarantees will be there
            val second = it.args.component2() as Int
            it.respond("Result = ${first + second}")
        }
    }

    command("welcome") {
        description = "Welcome a user"
        //here we leverage the `arg` function to state that the argument that we are providing is optional,
        //and that if it is not invoked with a value, the default will be `Welcome to the Server!`
        //Of course, you don't need to use named arguments here, I just did that for the sake of clarity :)
        expect(arg(SentenceArg, optional = true, default = "Welcome to the server!"))
        execute {
            val welcomeMessage = it.args.first() as String
            it.respond(welcomeMessage)
        }
    }

    command("country") {
        description = "Display the flag emoji for a country"
        //You can see here that we are expecting a wordArg (which is only one word, not the whole sentence, i.e. no spaces allowed)
        //but that we invoked it like it was a constructor. Well, it is a constructor. The string that we gave it is
        //the name of this *specific* WordArg. In our case, we expect a country name, so I set it to that.
        //This means that when a user runs !!help country, the documentation will show `Country Name` instead of `Word`
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
            //note: We **know** that our rating arg convert function returns a result that is of type Rating, so it's
            //safe to cast here. That is why the convention is Data for the data, and DataArg for the argument.
            val rating = it.args.component1() as Rating
            val user = it.args.component2() as User

            it.respond("${user.name} was rated ${rating.name} by ${it.author.name}")
        }
    }
}

//you Must extend ArgumentType to create a Custom argument
//This defines a few things
//1. A name - self explanatory
//2. Consumption type - How many space separated words will this argument consume (1, many, all of them)
//3. Examples - A list of strings which are valid string representations of an argument. For integer, "1" is such a string
//4. A convert function which processes the arg or arguments and determines if it was a successful parse

open class RatingArg(override val name: String = "Rating") : ArgumentType {
    //Here, we state that the companion object is just a RatingArg that has been invoked. This way,
    //you can Pass `RatingArg` into the expect Function without needing to use the constructor.
    companion object : RatingArg()

    //Here we say that we are consuming just one arg. So we only look at the arg value passed into the convert function
    //and nothing else.
    override val consumptionType: ConsumptionType = ConsumptionType.Single
    //Here we just build all possible examples programmatically, examples are picked from randomly. This helps
    //to create better documentation
    override val examples: ArrayList<String> = ArrayList(Rating.values().map { it.name })

    //Here we write the convert function, our implementation is as follows
    override fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult {
        //first we determine if the `arg` we have in our hand is a valid Rating arg
        val result = Rating.values().firstOrNull { it.name.toLowerCase() == arg.toLowerCase() }

        //if it is, return it wrapped in a Result. Otherwise, provide a meaningful error message.
        //You could also provide a list of valid arguments here too in the Error string.
        return if(result == null) ArgumentResult.Error("$arg is not a valid rating.") else ArgumentResult.Single(result)
    }
}

//A simple enum with 3 values
enum class Rating {
    Good, Alright, Bad
}