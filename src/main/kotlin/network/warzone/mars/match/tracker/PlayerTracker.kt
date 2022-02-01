package network.warzone.mars.match.tracker

import kotlinx.coroutines.runBlocking
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.PartyJoinData
import network.warzone.mars.api.socket.models.PartyLeaveData
import network.warzone.mars.api.socket.models.PlayerDeathData
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.match.deaths.LegacyTextDeathMessageBuilder
import network.warzone.mars.match.models.DeathCause
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.utils.KEvent
import network.warzone.mars.utils.simple
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.ChatColor.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.util.Vector
import tc.oc.pgm.api.match.event.MatchLoadEvent
import tc.oc.pgm.api.party.Competitor
import tc.oc.pgm.api.party.Party
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent
import tc.oc.pgm.events.PlayerJoinPartyEvent
import tc.oc.pgm.events.PlayerLeavePartyEvent
import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
import tc.oc.pgm.util.named.NameStyle
import java.util.*

class PlayerTracker : Listener {
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
                killer.getName(NameStyle.COLOR).append(Component.text(" drew first blood!", NamedTextColor.RED))
            )
        }
    }

    // todo: set XP bar progress & level (and ensure compatibility with vanilla exp)
    @EventHandler
    fun onPlayerXPGain(event: PlayerXPGainEvent) = runBlocking {
        val (id, gain, reason, notify) = event.data

        val context = PlayerManager.getPlayer(id) ?: return@runBlocking
        val player = context.player
        val profile = context.getPlayerProfile()

        val currentLevel = profile.stats.level

        // Add XP
        profile.stats.xp += gain
        if (notify) {
            player.playSound(player.location, Sound.ORB_PICKUP, 1000f, 1f)
            player.sendMessage("$LIGHT_PURPLE+$gain XP ($reason)")
        }

        // Update in cache
        PlayerFeature.add(profile)

        val newLevel = profile.stats.level
        if (newLevel > currentLevel) PlayerLevelUpEvent(PlayerLevelUpData(player, newLevel)).callEvent()
    }

    @EventHandler
    fun onPlayerLevelUp(event: PlayerLevelUpEvent) = runBlocking {
        val (player, level) = event.data

        player.playSound(player.location, Sound.LEVEL_UP, 1000f, 1f)
        player.sendMessage("$AQUA$STRIKETHROUGH----------------------------------------")
        player.sendMessage("$GREEN$BOLD Level up!$GREEN You are now level $RED${level}")
        player.sendMessage("$AQUA$STRIKETHROUGH----------------------------------------")
    }
}

data class PlayerLevelUpEvent(val data: PlayerLevelUpData) : KEvent()
data class PlayerLevelUpData(val player: Player, val level: Int)

data class PlayerXPGainEvent(val data: PlayerXPGainData) : KEvent()
data class PlayerXPGainData(val playerId: UUID, val gain: Int, val reason: String, val notify: Boolean)