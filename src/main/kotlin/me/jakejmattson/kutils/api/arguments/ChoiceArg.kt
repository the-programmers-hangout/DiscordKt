package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import me.jakejmattson.kutils.internal.utils.InternalLogger

/**
 * Accepts a choice from the provided list.
 *
 * @param choices The available choices. Can be any type, but associated by toString value.
 */
open class ChoiceArg<T>(override val name: String, vararg choices: T) : ArgumentType<T>() {
    private val enumerations = choices.associateBy { it.toString().toLowerCase() }
    private val options = enumerations.keys

    init {
        if (choices.size != options.size)
            InternalLogger.error("ChoiceArg elements must be unique.")
    }

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<T> {
        val selection = enumerations[arg.toLowerCase()]
            ?: return Error("Invalid selection")

        return Success(selection)
    }

    override fun generateExamples(event: CommandEvent<*>) = options.toList()
}