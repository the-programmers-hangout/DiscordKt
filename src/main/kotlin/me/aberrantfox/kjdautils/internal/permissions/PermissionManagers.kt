package me.aberrantfox.kjdautils.internal.permissions

import me.aberrantfox.kjdautils.internal.typealiases.PreconditionValue


interface PermissionManager {
    fun save()
    fun load()
    fun clearAll()
    fun producePrecondition() : PreconditionValue
}

data class Permission(val action: String, val allow: Boolean)

interface GlobalPermissionManager : PermissionManager {
    fun setUserPermission(action: String, userId: String, allow: Boolean)
    fun getUserPermission(userId: String, action: String) : Permission
    fun getUserPermissions(userId: String) : Set<Permission>
}

data class GuildPermissionSet(
    val guildID: String,
    val map: HashMap<String, List<Permission>>
)

interface GuildPermissionManager : PermissionManager {
    fun setRankPermission(guildID: String, action: String, allow: Boolean)
    fun getRankPermission(guildID: String, action: String) : Permission
    fun getRankPermissions(guildID: String, rankID: String) : List<Permission>
    fun getGuildPermissions(guildID: String) : GuildPermissionSet
}