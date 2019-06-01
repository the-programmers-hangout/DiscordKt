package me.aberrantfox.kjdautils.api.dsl

import me.aberrantfox.kjdautils.internal.command.PreconditionResult

fun precondition(condition: (CommandEvent) -> PreconditionResult): (CommandEvent) -> PreconditionResult = condition
