package me.aberrantfox.kjdautils.internal.permissions

import com.google.gson.GsonBuilder
import me.aberrantfox.kjdautils.api.dsl.precondition
import me.aberrantfox.kjdautils.extensions.stdlib.unit
import me.aberrantfox.kjdautils.internal.command.Fail
import me.aberrantfox.kjdautils.internal.command.Pass
import me.aberrantfox.kjdautils.internal.typealiases.*
import java.io.File

data class PermissionsContainer(
    val permissions: HashMap<UserId, HashMap<Action, HasPermission>> = HashMap(),
    val guildPermissions: GuildPermissions = GuildPermissions()
)

data class GuildPermissions(val permissions: HashMap<GuildId, HashMap<Action, RoleId>> = HashMap())

private val PType = PermissionsContainer::class.java

class DefaultPermissionManager(savePath: String = "permissions.json") : PermissionManager {
    override fun getGuildPermission(action: Action, guildID: GuildId): RoleId {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val container = PermissionsContainer()

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val file = File(savePath)

    init {
        if(!file.exists()) {
            save()
        }
    }

    override fun clearAll() = container.permissions.clear()

    override fun setUserPermission(action: String, userId: String, allow: Boolean) =
            obtainUserMap(userId).apply { this[action] = allow }.unit()

    override fun getUserPermission(userId: String, action: String) =
            Permission(action, obtainUserMap(userId).getOrDefault(action, false))

    override fun getUserPermissions(userId: String) =
            obtainUserMap(userId).map { Permission(it.key, it.value) }.toSet()

    override fun producePrecondition() = precondition { event ->
        val canUse = obtainUserMap(event.author.id).getOrDefault(event.commandStruct.commandName, false)
        if (canUse) Pass else Fail("No Permission")
    }

    override fun setGuildPermission(roleId: RoleId, action: Action, guildID: GuildId) {
        container.guildPermissions.permissions.getOrPut(guildID) { HashMap() }[action] = roleId
    }

    override fun removePerGuildPermission(roleId: RoleId, action: Action, guildID: GuildId) {
        container.guildPermissions.permissions.getOrPut(guildID) { HashMap() }.remove(action)
    }

    override fun save() = file.writeText(gson.toJson(container.permissions))

    override fun load() = file.exists().run {
        if(this) container.permissions.putAll(gson.fromJson(file.readText(), PType).permissions)
    }

    private fun obtainUserMap(userId: String) = container.permissions.getOrPut(userId) { HashMap() }
}