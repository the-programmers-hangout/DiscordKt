package arguments

import me.aberrantfox.kutils.api.arguments.EveryArg
import utilities.ArgumentTestFactory

class EveryArgTest : ArgumentTestFactory {
    override val argumentType = EveryArg

    override val validArgs = listOf(
        "HELLO" to "HELLO",
        "world" to "world",
        "hello world" to "hello world"
    )

    override val invalidArgs = listOf("")
}