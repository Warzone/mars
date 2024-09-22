package network.warzone.mars.match.tracker

import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.PartyJoinData
import network.warzone.mars.api.socket.models.PartyLeaveData
import network.warzone.mars.api.socket.models.PlayerDeathData
import network.warzone.mars.match.deaths.LegacyTextDeathMessageBuilder
import network.warzone.mars.match.models.DeathCause
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.utils.AUDIENCE_PROVIDER
import network.warzone.mars.utils.KEvent
import network.warzone.mars.utils.Sounds
import network.warzone.mars.utils.simple
import org.bukkit.ChatColor.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.event.MatchLoadEvent
import tc.oc.pgm.api.party.Competitor
import tc.oc.pgm.api.party.Party
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent
import tc.oc.pgm.events.PlayerJoinPartyEvent
import tc.oc.pgm.events.PlayerLeavePartyEvent
import tc.oc.pgm.util.named.NameStyle
import java.util.*

class PlayerTracker : Listener {

    companion object {
        val ORB_SOUND = Sound.sound(Sounds.RANDOM_ORB, Sound.Source.MASTER, 0.05f, 1f)
        val LEVEL_UP_SOUND = Sound.sound(Sounds.RANDOM_LEVELUP, Sound.Source.MASTER, 1000f, 1f)
    }

    private var pendingFirstBlood = true

    @EventHandler
    fun onMatchStart(event: MatchLoadEvent) {
        pendingFirstBlood = true
    }

    @EventHandler
    fun onPartyJoin(event: PlayerJoinPartyEvent) {
        if (event.newParty !is Competitor || !event.match.isRunning) return // Joining spectator

        val party: Party = event.newParty!!

        ApiClient.emit(
            OutboundEvent.PartyJoin,
            PartyJoinData(event.player.simple, party.defaultName)
        )
    }

    @EventHandler
    fun onPartyLeave(event: PlayerLeavePartyEvent) {
        if (event.party !is Competitor || !event.match.isRunning) return // Leaving spectator

        ApiClient.emit(
            OutboundEvent.PartyLeave,
            PartyLeaveData(event.player.simple)
        )
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerDeath(event: MatchPlayerDeathEvent) {
        // Suicides aren't classified as team kills, so both checks are necessary.
        if (event.isTeamKill) return

        val translatableComponent = LegacyTextDeathMessageBuilder(event)

        val weapon = translatableComponent.weaponString
        val mob = translatableComponent.mobString
        val distance = translatableComponent.distance

        ApiClient.emit(
            OutboundEvent.PlayerDeath,
            PlayerDeathData(
                victim = event.victim.simple,
                attacker = event.killer?.simple,
                weapon = weapon,
                entity = mob,
                distance = distance,
                key = translatableComponent.key!!,
                cause = DeathCause.fromDamageInfo(event.damageInfo)
            )
        )
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onFirstBlood(event: MatchPlayerDeathEvent) {
        if (!pendingFirstBlood) return

        val killer = event.killer ?: return
        if (event.isEnemyKill) {
            pendingFirstBlood = false
            event.match.sendMessage(
                text().append(killer.getName(NameStyle.COLOR)).append(Component.text(" drew first blood!", NamedTextColor.RED))
            )
        }
    }

    // todo: set XP bar progress & level (and ensure compatibility with vanilla exp)
    @EventHandler
    fun onPlayerXPGain(event: PlayerXPGainEvent) {
        val (id, gain, reason, notify, multiplier) = event.data

        val context = PlayerManager.getPlayer(id) ?: return
        val player = context.player
        val profile = context.getPlayerProfileCached() ?: return

        val currentLevel = profile.stats.level

        // Add XP
        profile.stats.xp += gain
        if (notify) {
            AUDIENCE_PROVIDER.player(player).playSound(ORB_SOUND)
            var message = "$LIGHT_PURPLE+$gain XP ($reason)"
            if (multiplier != null && multiplier != 1f) message += " (${multiplier}x multiplier)"
            player.sendMessage(message)
        }

        // Update in cache
        PlayerFeature.add(profile)

        val newLevel = profile.stats.level
        if (newLevel > currentLevel) PlayerLevelUpEvent(PlayerLevelUpData(player, newLevel)).callEvent()
    }

    @EventHandler
    fun onPlayerLevelUp(event: PlayerLevelUpEvent) {
        val (player, level) = event.data

        player.sendMessage("$AQUA$STRIKETHROUGH----------------------------------------")
        player.sendMessage("$GREEN$BOLD Level up!$GREEN You are now level $RED${level}")
        player.sendMessage("$AQUA$STRIKETHROUGH----------------------------------------")
        AUDIENCE_PROVIDER.player(player).playSound(LEVEL_UP_SOUND)
    }
}

data class PlayerLevelUpEvent(val data: PlayerLevelUpData) : KEvent()
data class PlayerLevelUpData(val player: Player, val level: Int)

data class PlayerXPGainEvent(val data: PlayerXPGainData) : KEvent(async = true)
data class PlayerXPGainData(val playerId: UUID, val gain: Int, val reason: String, val notify: Boolean, val multiplier: Float?)