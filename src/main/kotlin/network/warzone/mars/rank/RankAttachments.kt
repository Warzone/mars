package network.warzone.mars.rank

import network.warzone.mars.Mars
import network.warzone.mars.player.PlayerContext
import org.bukkit.permissions.PermissionAttachment
import java.util.*

object RankAttachments {
    private val permissionAttachments: MutableMap<UUID, PermissionAttachment> = HashMap()

    suspend fun refresh(context: PlayerContext) {
        purgePermissions(context)

        context.getPlayerProfile().ranks
            .map { it.get() }
            .forEach { addPermissions(context, it.permissions) }
    }

    fun createAttachment(context: PlayerContext) {
        permissionAttachments[context.uuid] = context.player.addAttachment(Mars.get())
    }

    fun getAttachment(context: PlayerContext): PermissionAttachment? {
        return permissionAttachments[context.uuid]
    }

    fun addPermissions(context: PlayerContext, permissions: List<String>) {
        permissions.forEach { addPermission(context, it) }
    }

    fun addPermission(context: PlayerContext, permission: String) {
        permissionAttachments[context.uuid]?.setPermission(permission, true)
    }

    fun removePermissions(context: PlayerContext, permissions: List<String>) {
        permissions.forEach { removePermission(context, it) }
    }

    fun removePermission(context: PlayerContext, permission: String) {
        permissionAttachments[context.uuid]?.unsetPermission(permission)
    }

    fun removeAttachment(context: PlayerContext) {
        permissionAttachments.remove(context.uuid)?.remove()
    }

    fun purgePermissions(context: PlayerContext) {
        permissionAttachments[context.uuid]?.let {
            it.permissions.keys.forEach(it::unsetPermission)
        }
    }
}