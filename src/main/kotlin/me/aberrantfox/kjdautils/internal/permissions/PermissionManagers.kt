package me.aberrantfox.kjdautils.internal.permissions

import me.aberrantfox.kjdautils.internal.typealiases.*

data class Permission(val action: String, val allow: Boolean)

interface PermissionManager {
    fun save()
    fun load()
    fun clearAll()
    fun producePrecondition() : PreconditionValue
    fun setUserPermission(action: String, userId: UserId, allow: Boolean)
    fun getUserPermission(userId: UserId, action: Action) : Permission
    fun getUserPermissions(userId: UserId) : Set<Permission>
    fun setGuildPermission(roleId: RoleId, action: Action, guildID: GuildId)
    fun removePerGuildPermission(roleId: RoleId, action: Action, guildID: GuildId)
    fun getGuildPermission(action: Action, guildID: GuildId): RoleId
}
