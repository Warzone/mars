package network.warzone.mars.punishment.commands

import app.ashcon.intake.Command
import app.ashcon.intake.CommandException
import app.ashcon.intake.bukkit.parametric.annotation.Sender
import app.ashcon.intake.parametric.annotation.Switch
import app.ashcon.intake.parametric.annotation.Text
import kotlinx.coroutines.runBlocking
import net.wesjd.anvilgui.AnvilGUI
import network.warzone.mars.Mars
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.match.MatchManager
import network.warzone.mars.player.PlayerContext
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.player.feature.PlayerService
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.punishment.PunishmentFeature
import network.warzone.mars.punishment.models.*
import network.warzone.mars.utils.*
import network.warzone.mars.utils.menu.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import tc.oc.pgm.lib.net.kyori.adventure.audience.Audience
import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.*
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
import tc.oc.pgm.lib.net.kyori.adventure.text.format.TextDecoration
import java.time.Duration
import java.util.*
import javax.annotation.Nullable

class PunishCommands {
    @Command(aliases = ["punish", "p", "pun"], desc = "Punish a player")
    fun onPunish(
        @Sender player: Player,
        audience: Audience,
        context: PlayerContext,
        target: PlayerProfile,
        @Nullable @Text reason: String?,
        @Switch('s') isSilent: Boolean = false
    ) = runBlocking {
        val types = PunishmentFeature.punishmentTypes.filter { player.hasPermission(it.requiredPermission) }

        try {
            val history = PlayerFeature.getPunishmentHistory(target._id.toString())

            if (reason != null) {
                val searchResults = types.filter {
                    it.name.toLowerCase().contains(reason.toLowerCase()) || it.short.equals(
                        reason,
                        ignoreCase = true
                    )
                }
                if (searchResults.size == 1) player.open(
                    createPunishConfirmGUI(
                        context,
                        target,
                        target.getDisplayName(ChatColor.GRAY),
                        searchResults.last(),
                        history,
                        null,
                        isSilent
                    )
                )
                else if (searchResults.isEmpty()) player.sendMessage("${ChatColor.RED}Could not find punishment types for query '${reason}'")
                else player.open(createPunishGUI(context, target, history, searchResults, isSilent))
                return@runBlocking
            }

            player.open(createPunishGUI(context, target, history, types, isSilent))
        } catch (e: FeatureException) {
            audience.sendMessage(e.asTextComponent())
        }
    }

    @Command(aliases = ["revertp"], desc = "Revert a punishment by ID")
    fun onRevert(
        @Sender player: Player,
        audience: Audience,
        punishment: Punishment,
        @Nullable @Text reason: String?
    ) = runBlocking {
        if (punishment.reversion != null) throw CommandException("This punishment was already reverted ${Date(punishment.reversion.revertedAt).getTimeAgo()} by ${punishment.reversion.reverter.name} for ${ChatColor.WHITE}${punishment.reversion.reason}${ChatColor.RED}.")

        try {
            if (reason == null) {
                val anvil =
                    AnvilGUI.Builder().text("Reversion reason").plugin(Mars.get())
                anvil.onComplete { player, reason ->
                    player.performCommand("mars:revertp ${punishment._id} $reason")
                    return@onComplete AnvilGUI.Response.close()
                }
                anvil.open(player)
            } else {
                val pun = PunishmentFeature.revert(
                    punishment._id,
                    reason,
                    SimplePlayer(id = player.uniqueId, name = player.name)
                )

                val target = PlayerManager.getPlayer(pun.target.id)
                if (target != null) { // Target is online
                    target.activePunishments = target.activePunishments.filterNot { it._id == pun._id }
                }

                sendModeratorBroadcast(
                    text(player.name, NamedTextColor.YELLOW).append(space())
                        .append(text("reverted", NamedTextColor.YELLOW))
                        .append(
                            space()
                        ).append(pun.asTextComponent(false))
                )
            }
        } catch (e: FeatureException) {
            audience.sendMessage(e.asTextComponent())
        }
    }

    @Command(aliases = ["punishments", "puns"], desc = "View a player's punishment history")
    fun onPunishmentHistory(@Sender sender: CommandSender, audience: Audience, target: String) =
        runBlocking {
            try {
                val history = PlayerService.getPunishmentHistory(target)
                if (history.isEmpty()) return@runBlocking sender.sendMessage("${ChatColor.YELLOW}${target} has no punishment history")

                sender.sendMessage("${ChatColor.RED}Punishments for ${target}:")
                history
                    .map(Punishment::asTextComponent)
                    .map { text("- ", NamedTextColor.GRAY).append(it) }
                    .forEach(audience::sendMessage)
            } catch (e: FeatureException) {
                audience.sendMessage(e.asTextComponent())
            }
        }

    private suspend fun createPunishGUI(
        context: PlayerContext,
        target: PlayerProfile,
        history: List<Punishment>,
        types: List<PunishmentType>,
        isSilent: Boolean = false
    ): GUI {
        val targetDisplay = target.getDisplayName(ChatColor.GRAY)

        return gui(
            "${ChatColor.DARK_RED}Punish ${target.name}",
            6
        ) {
            var currentSlot = 18
            types.sortedBy { it.position }.forEach { type ->
                val tip = if (type.tip != null) {
                    type.tip.chunkedWords(7).map { "${ChatColor.GOLD}${ChatColor.BOLD}${it}" }
                } else listOf("-")
                val chunkedMessage = type.message.chunkedWords(6)

                slot(currentSlot++) {
                    val material = Material.matchMaterial(type.material)
                        ?: throw RuntimeException("Material not found: ${type.material}")

                    item = item(material) {
                        name = "${ChatColor.YELLOW}${type.name}"
                        lore = listOf(
                            "",
                            "${ChatColor.BLUE}Right-click to add staff note",
                            "",
                            "${ChatColor.YELLOW}Message: ${ChatColor.RESET}${chunkedMessage.first()}",
                            *chunkedMessage.drop(1).map { "${ChatColor.RESET}${it}" }.toTypedArray(),
                            "",
                            *tip.toTypedArray()
                        ).filterNot { it == "-" }
                    }

                    onclick = {
                        if (this.isRightClick) {
                            val anvil =
                                AnvilGUI.Builder().text("Enter note here").plugin(Mars.get())
                            anvil.onComplete { player, note ->
                                return@onComplete AnvilGUI.Response.openInventory(
                                    createPunishConfirmGUI(
                                        context,
                                        target,
                                        targetDisplay,
                                        type,
                                        history,
                                        note,
                                        isSilent
                                    ).inventory
                                )
                            }
                            anvil.open(this.actor)
                        } else if (this.isLeftClick) {
                            actor.open(
                                createPunishConfirmGUI(
                                    context,
                                    target,
                                    targetDisplay,
                                    type,
                                    history,
                                    null,
                                    isSilent
                                )
                            )
                        }
                    }
                }
            }

            slot(0) {
                item = item(Material.SKULL_ITEM) {
                    stack = getHead(target.name)
                    name = targetDisplay
                    lore = createPlayerLore(target, history)
                }
            }
        }
    }

    private fun createPunishConfirmGUI(
        context: PlayerContext,
        target: PlayerProfile,
        targetDisplay: String,
        type: PunishmentType,
        history: List<Punishment>,
        note: String?,
        isSilent: Boolean = false
    ): GUI {
        val previous =
            history.filter { (it.reason.short == type.short || it.reason.name == type.name) && it.reversion == null }

        val offence = previous.count() + 1
        val offenceAction = type.getActionByOffence(offence)
        var selectedAction = offenceAction
        println("Offence: $offence | Action: $selectedAction")

        return gui("${ChatColor.DARK_RED}${type.name.take(25)} ($offence)", 3) {
            type.actions.forEachIndexed { index, action ->
                val dye = if (action == selectedAction) DyeColor.LIME else DyeColor.WHITE
                val length = action.formatLength()
                slot(index) {
                    item = item(Material.STAINED_GLASS_PANE) {
                        stack = ItemStack(Material.STAINED_GLASS_PANE, 1, dye.data.toShort())

                        val fmtNoun = "${action.kind.colour}${action.kind.noun}"
                        val fmtVerb = "${ChatColor.GRAY}${action.kind.verb}"
                        val fmtReason = "${ChatColor.RED}${type.name}"
                        val fmtTarget = "${ChatColor.AQUA}${target.name}"

                        if (action.length > 0L) {
                            name = "$fmtNoun ${action.kind.colour}($length)"
                            lore =
                                listOf("$fmtVerb $fmtTarget ${ChatColor.GRAY}for ${ChatColor.WHITE}$length ${ChatColor.GRAY}for $fmtReason")
                        } else if (action.isPermanent()) {
                            name = "$fmtNoun ${action.kind.colour}(forever)"
                            lore =
                                listOf("$fmtVerb $fmtTarget ${ChatColor.WHITE}forever ${ChatColor.GRAY}for $fmtReason")
                        } else if (action.isInstant()) {
                            name = fmtNoun
                            lore =
                                listOf("$fmtVerb $fmtTarget ${ChatColor.GRAY}for $fmtReason")
                        }
                    }

                    onclick = {
                        if (action != selectedAction) {
                            selectedAction = action
                            // todo: play sound
                            refresh()
                        }
                    }
                }
            }

            slot(18) {
                item = item(Material.SKULL_ITEM) {
                    stack = getHead(target.name)
                    name = targetDisplay
                    lore = createPlayerLore(target, history)
                }
            }

            slot(26) {
                item = item(Material.ARROW) {
                    name = "${ChatColor.DARK_RED}${selectedAction.kind.verb} ${target.name}"
                    val length =
                        if (selectedAction.isInstant()) "Instant" else if (selectedAction.isPermanent()) "Permanent" else selectedAction.formatLength()
                    lore = listOf(
                        "",
                        "${ChatColor.GRAY}Reason: ${ChatColor.WHITE}${type.name}",
                        "${ChatColor.GRAY}Note: ${ChatColor.WHITE}${note ?: "${ChatColor.ITALIC}(None)"}",
                        "${ChatColor.GRAY}Length: ${ChatColor.WHITE}${length}",
                        "${ChatColor.GRAY}Silent? ${if (isSilent) "${ChatColor.GREEN}Yes" else "${ChatColor.RED}No"}",
                        "${ChatColor.GRAY}Offence: ${ChatColor.WHITE}${offence} ${
                            if (offenceAction != selectedAction) "${ChatColor.RED}(Action: ${
                                type.actions.indexOf(
                                    selectedAction
                                ) + 1
                            })" else ""
                        }"
                    )
                }

                onclick = {
                    issuePunishment(
                        target,
                        target.ips,
                        offence,
                        context,
                        type.toReason(),
                        selectedAction,
                        isSilent,
                        note
                    )
                    actor.closeInventory()
                }
            }
        }
    }

    companion object {

        fun createPlayerLore(player: PlayerProfile, punHistory: List<Punishment>): List<String> {
            val historyLore = mutableListOf<String>()
            punHistory.forEach {
                val time =
                    "${ChatColor.WHITE}${ChatColor.BOLD}${it.issuedAt.getTimeAgo()}${if (it.isReverted) " ${ChatColor.RED}${ChatColor.BOLD}✗" else ""}"
                val kind = "${if (!it.isReverted) it.action.kind.colour else ChatColor.GRAY}${it.action.kind.noun}"
                val bullet = "${ChatColor.GRAY}•"
                val reason = "${if (!it.isReverted) ChatColor.RED else ChatColor.GRAY}${it.reason.name} (${it.offence})"
                val length = it.action.formatLength()
                val staff = "${ChatColor.AQUA}${it.punisher.name}"
                val note = if (it.note != null) "${ChatColor.GRAY}[${it.note}]" else ""

                historyLore.addAll(
                    "$time $bullet $kind ${if (!it.action.isInstant()) "($length) " else ""}$bullet $reason $bullet by $staff $note".chunkedWords(
                        13
                    ).takeWhile { it.isNotEmpty() }.map { "${ChatColor.RED}$it" }
                )
            }

            val isPlayerOnline = Bukkit.getPlayer(player._id) != null

            val profile = mutableListOf<Pair<String, String>>()
            profile.add(Pair("First Joined", player.firstJoinedAt.getTimeAgo()))
            profile.add(
                Pair(
                    "Last Joined",
                    if (isPlayerOnline) "${ChatColor.GREEN}Online" else player.lastJoinedAt.getTimeAgo()
                )
            )
            profile.add(Pair("Playtime", Duration.ofMillis(player.stats.serverPlaytime).conciseFormat()))
            profile.add(Pair("Known IPs", player.ips.count().toString()))

            return mutableListOf(
                "",
                "${ChatColor.AQUA}${ChatColor.UNDERLINE}Profile",
                "",
                *profile.map { "${ChatColor.GRAY}${it.first}: ${ChatColor.WHITE}${it.second}" }.toTypedArray(),
                "",
                "${ChatColor.RED}${ChatColor.UNDERLINE}Punishment History",
                "",
                *historyLore.toTypedArray()
            )
        }

    }

    private suspend fun issuePunishment(
        target: PlayerProfile,
        targetIps: List<String>,
        offence: Int,
        staff: PlayerContext,
        reason: PunishmentReason,
        action: PunishmentAction,
        silent: Boolean,
        note: String?
    ) {
        try {
            val targetContext = PlayerManager.getPlayer(target._id)

            val punishment = PunishmentFeature.issue(
                reason = reason,
                offence = offence,
                action = action,
                note = note,
                punisher = SimplePlayer(staff.getPlayerProfile()._id, staff.getPlayerProfile().name),
                targetName = target.name,
                targetIps = targetIps,
                silent = silent
            )

            val appealLink = Mars.get().config.getString("server.links.appeal")
                ?: throw RuntimeException("No appeal link set in config")

            if (targetContext == null && action.kind == PunishmentKind.KICK) staff.player.sendMessage("${ChatColor.RED}The player could not be kicked as they are not online, but the punishment has been recorded.")

            if (targetContext != null) { // Target is playing
                when (action.kind) {
                    PunishmentKind.WARN -> {
                        targetContext.matchPlayer.sendMessage(
                            newline().append(text("» You have been warned for", NamedTextColor.GRAY)).append(space())
                                .append(text(reason.name, NamedTextColor.RED, TextDecoration.BOLD)).append(newline())
                                .append(
                                    text("»", NamedTextColor.GRAY)
                                ).append(space()).append(text(reason.message, NamedTextColor.RED)).append(newline())
                                .append(
                                    text("» Further offences may result in harsher punishments", NamedTextColor.GRAY)
                                ).append(newline())
                        )
                    }
                    PunishmentKind.KICK -> targetContext.player.kickPlayer("${ChatColor.GRAY}You have been kicked from the server.\n\n${ChatColor.RED}${reason.message}\n\n${ChatColor.GRAY}Further offences may result in harsher punishments.")
                    PunishmentKind.BAN -> targetContext.player.kickPlayer("${ChatColor.GRAY}You have been banned from the server.\n\n${ChatColor.RED}${reason.message}\n\n${ChatColor.GRAY}Appeal at ${ChatColor.AQUA}$appealLink")
                    PunishmentKind.IP_BAN -> targetContext.player.kickPlayer("${ChatColor.GRAY}You have been IP banned from the server.\n\n${ChatColor.RED}${reason.message}\n\n${ChatColor.GRAY}Appeal at ${ChatColor.AQUA}$appealLink")
                    PunishmentKind.MUTE -> {
                        targetContext.activePunishments = targetContext.activePunishments + punishment
                        targetContext.matchPlayer.sendMessage(
                            newline().append(text("» You have been muted for", NamedTextColor.GRAY)).append(space())
                                .append(text(reason.name, NamedTextColor.RED, TextDecoration.BOLD)).append(newline())
                                .append(
                                    text("»", NamedTextColor.GRAY)
                                ).append(space()).append(text(reason.message, NamedTextColor.RED)).append(newline())
                                .append(
                                    text("» Further offences may result in harsher punishments", NamedTextColor.GRAY)
                                ).append(newline())
                        )
                    }
                }
            }

            broadcastPunishment(target, staff, punishment)
        } catch (e: FeatureException) {
            throw e
        }
    }

    private fun broadcastPunishment(
        target: PlayerProfile,
        staff: PlayerContext,
        punishment: Punishment
    ) {
        val action = punishment.action
        val lengthString =
            if (action.isInstant()) "" else if (action.isPermanent()) "${ChatColor.RED}forever " else "${ChatColor.GRAY}for ${ChatColor.RED}${action.formatLength()} "
        val staffBroadcast =
            "${if (punishment.silent) "${ChatColor.GRAY}(Silent) " else ""}${ChatColor.LIGHT_PURPLE}${target.name} ${ChatColor.GRAY}has been ${ChatColor.RED}${action.kind.pastTense} ${ChatColor.GRAY}by ${ChatColor.DARK_PURPLE}${staff.player.name} $lengthString${ChatColor.GRAY}for ${ChatColor.RESET}${punishment.reason.name}"
        val publicBroadcast =
            "${ChatColor.LIGHT_PURPLE}${target.name} ${ChatColor.GRAY}has been ${ChatColor.RED}${action.kind.pastTense} ${ChatColor.GRAY}for ${ChatColor.RESET}${punishment.reason.name}"

        Bukkit.getOnlinePlayers().forEach {
            if (it.uniqueId != target._id) {
                if (it.hasPermission("mars.punish")) it.sendMessage(staffBroadcast)
                if (!punishment.silent && !it.hasPermission("mars.punish")) it.sendMessage(publicBroadcast)
            }
        }
    }

    fun sendModeratorBroadcast(message: Component) {
        MatchManager.match.players.filter { it.bukkit.hasPermission("mars.punish") }
            .forEach { it.player?.sendMessage(message) }
    }
}