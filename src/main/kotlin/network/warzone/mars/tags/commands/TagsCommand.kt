package network.warzone.mars.tags.commands

import app.ashcon.intake.Command
import app.ashcon.intake.bukkit.parametric.annotation.Sender
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.audience.Audience
import network.warzone.mars.player.PlayerContext
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.tags.models.Tag
import network.warzone.mars.utils.color
import network.warzone.mars.utils.menu.GUI
import network.warzone.mars.utils.menu.gui
import network.warzone.mars.utils.menu.item
import network.warzone.mars.utils.menu.open
import org.bukkit.ChatColor.*
import org.bukkit.Material
import org.bukkit.entity.Player

class TagsCommand {

    @Command(aliases = ["tags"], desc = "View and manage your tags.")
    fun onTags(@Sender player: Player, context: PlayerContext, audience: Audience) = runBlocking {
        val tags = context.getPlayerProfile().tags()

        if (tags.isEmpty()) {
            player.sendMessage("${RED}You do not have any tags.") //TODO: advertise
            return@runBlocking
        }

        player.open(createTagGUI(context, tags))
    }

    private suspend fun createTagGUI(context: PlayerContext, tags: List<Tag>): GUI {
        val profile = context.getPlayerProfile()
        return gui("${AQUA}Tags", 6) {
            tags.forEachIndexed { index, tag ->
                val isActive = profile.activeTagId != null && profile.activeTagId == tag._id
                slot(index) {
                    val material = if (isActive) Material.MAP else Material.PAPER
                    val itemLore = if (isActive) "${YELLOW}Click to deselect" else "${GREEN}Click to select!"

                    item = item(material) {
                        name = "$GRAY[${tag.display.color()}$GRAY]"
                        lore = listOf("", itemLore)
                    }

                    onclick = {
                        if (isActive) {
                            PlayerFeature.removeActiveTag(context)
                        } else {
                            if (profile.tagIds.contains(tag._id)) PlayerFeature.setActiveTag(context, tag)
                        }

                        refresh()
                    }
                }
            }
        }
    }

}