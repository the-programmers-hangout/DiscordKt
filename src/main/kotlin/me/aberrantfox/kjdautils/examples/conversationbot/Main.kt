package me.aberrantfox.kjdautils.examples.conversationbot

import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.api.startBot
import me.aberrantfox.kjdautils.internal.command.ConversationService
import me.aberrantfox.kjdautils.internal.command.arguments.SentenceArg
import me.aberrantfox.kjdautils.internal.command.arguments.UserArg
import net.dv8tion.jda.core.entities.User
import java.awt.Color


fun main(args: Array<String>) {
    val token = args.firstOrNull() ?: System.getenv("BOT_TOKEN")

    if(token == null) {
        println("Please provide a bot token")
        return
    }

    startBot(token) {
        configure {
            globalPath = "me.aberrantfox.kjdautils.examples.conversationbot"
            prefix = "!"
        }
    }
}

//Conversations are there for those times when regular command argument parsing is just too cumbersome.
//They should be used to build up many arguments without any restriction that is normally implied with having a command
//string. For example, how would you get two sentence args in one command? Well, you can use the SplitterArg, but
//what if you wanted a word, then a sentence, then a word and two more sentences? This kind of really complicated
//parsing is too much for users all at once. For your more complicated commands, definitely make use of the conversation
//service.

@CommandSet("ExampleCategory")

//This name will be used to create the conversation when you need it.
const val conversationName = "test-conversation"

//The conversation service is a container that KUtils automatically injects as a service. It can be injected anywhere.
//Services are explained in the 'dependency injection bot' section.
fun createConversationCommands(conversationService: ConversationService) = commands {
    command("conversationtest") {
        description = "Test the implementation of the ConversationDSL"
        requiresGuild = true
        execute {
            //ConversationService.createConversation can be used to begin a conversation at any time.
            //In this case, we begin our test-conversation which is defined below.
            conversationService.createConversation(it.author.id, it.guild!!.id, conversationName)
        }
    }
}

@Convo
fun testConversation() = conversation {
    //you can see this above
    name = conversationName
    description = "Test conversation to test the implementation within KUtils."

    //Conversations are composed of steps.
    //Each step is composed of two parts
    // 1. The prompt
    // 2. The CommandArgument that you are expecting
    steps {
        step {
            //Here, we attempt to tell the user to give us another user as an argument
            prompt = embed {
                setTitle("Test Conversation")
                field {
                    name = "Step 1"
                    value = "To test various use cases I'd like you to tell me a user Id to start with."
                }
                setColor(Color.CYAN)
            }
            //This step cannot be passed until a valid argument is provided
            expect = UserArg
        }
        step {
            prompt = embed {
                setTitle("Test Conversation")
                field {
                    name = "Step 2"
                    value = "Alright, now tell me a random sentence."
                }
                setColor(Color.CYAN)
            }
            expect = SentenceArg
        }
    }

    //The onComplete block is run once all steps in the conversation have passed. You can now process the responses.
    onComplete {
        val user = it.responses.component1() as User
        val word = it.responses.component2() as String
        val summary = embed {
            setTitle("Summary - That's what you've told me")
            setThumbnail(user.avatarUrl)
            field {
                name = "Some user account"
                value = "The account of **${user.name}** was created on **${user.creationTime}**."
            }
            field {
                name = "Random word"
                value = "You've said **$word**."
            }
            addBlankField(true)
            field {
                name = "Test Completed"
                value = "Thanks for participating!"
            }
            setColor(Color.GREEN)
        }

        it.respond(summary)
    }
}
