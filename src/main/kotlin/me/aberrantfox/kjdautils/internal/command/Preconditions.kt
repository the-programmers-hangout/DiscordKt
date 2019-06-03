package me.aberrantfox.kjdautils.internal.command

sealed class PreconditionResult

object Pass : PreconditionResult()
open class Fail(val reason: String? = null) : PreconditionResult() {
    companion object : Fail("")
}