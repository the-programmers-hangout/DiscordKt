package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent

/**
 * Accept a whole number in the int range.
 */
open class IntegerArg(override val name: String = "Integer") : ArgumentType<Int>() {
    /**
     * Accept a whole number in the int range.
     */
    companion object : IntegerArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>) =
        when (val result = arg.toIntOrNull()) {
            null -> Error<Int>("Invalid format")
            else -> Success(result)
        }

    override fun generateExamples(event: CommandEvent<*>) = (0..10).map { it.toString() }
}