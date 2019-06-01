package me.aberrantfox.kjdautils.api.annotation

enum class PreconditionKind {
    OneOf,
    AllOf
}

annotation class Precondition(val kind: PreconditionKind = PreconditionKind.AllOf)