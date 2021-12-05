package network.warzone.mars.punishment.models

import kotlinx.serialization.Serializable
import network.warzone.mars.utils.format
import org.bukkit.ChatColor
import java.time.Duration

@Serializable
data class PunishmentAction(val kind: PunishmentKind, val length: Long) {
    fun isPermanent(): Boolean {
        return length == -1L
    }

    fun isInstant(): Boolean {
        return length == 0L;
    }

    fun formatLength(): String {
        return when {
            isPermanent() -> "Permanent"
            isInstant() -> "Instant"
            else -> Duration.ofMillis(length).format()
        }
    }

    fun isBan(): Boolean {
        return kind == PunishmentKind.BAN || kind == PunishmentKind.IP_BAN
    }
}
