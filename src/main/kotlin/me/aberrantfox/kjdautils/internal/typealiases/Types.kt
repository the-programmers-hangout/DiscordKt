package me.aberrantfox.kjdautils.internal.typealiases

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.internal.command.PreconditionResult

typealias UserId = String
typealias GuildId = String
typealias RoleId = String
typealias Action = String
typealias HasPermission = Boolean
typealias PreconditionValue = (CommandEvent) -> PreconditionResult