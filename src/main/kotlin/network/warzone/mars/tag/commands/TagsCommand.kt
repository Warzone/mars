package network.warzone.mars.tag.commands

import app.ashcon.intake.Command
import app.ashcon.intake.bukkit.parametric.annotation.Sender
import network.warzone.mars.Mars
import net.kyori.adventure.audience.Audience
import network.warzone.mars.player.PlayerContext
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.tag.models.Tag
import network.warzone.mars.utils.color
import network.warzone.mars.utils.menu.GUI
import network.warzone.mars.utils.menu.gui
import network.warzone.mars.utils.menu.item
import network.warzone.mars.utils.menu.open
import org.bukkit.ChatColor.*
import org.bukkit.Material
import org.bukkit.entity.Player

class TagsCommand {
    @Command(
        aliases = ["tags"],
        desc = "View and manage your tags",
        perms = ["mars.tags"]
    )
    fun onTags(@Sender player: Player, context: PlayerContext, audience: Audience) {
        Mars.async {
            val tags = context.getPlayerProfile().tags()

            if (tags.isEmpty()) {
                player.sendMessage("${RED}You do not have any tags. Purchase some with /buy!")
                return@async
            }

            player.open(createTagGUI(context, tags))
        }
    }

    private suspend fun createTagGUI(context: PlayerContext, tags: List<Tag>): GUI {
        val profile = context.getPlayerProfile()
        // todo: show locked tags? - have an "available" prop (-a) so custom tags don't appear? or use player count to decide? or name format?
        val rows = (if (tags.count() < 9) 1 else tags.count() / 9) + 1
        return gui("${DARK_AQUA}Tags", rows) {
            tags.forEachIndexed { index, tag ->
                val isActive = profile.activeTagId != null && profile.activeTagId == tag._id
                slot(index) {
                    val material = if (isActive) Material.MAP else Material.PAPER
                    val itemLore = if (isActive) "${YELLOW}Click to deselect" else "${GREEN}Click to select!"

                    item = item(material) {
                        name = "$GRAY[${tag.display.color()}$GRAY]"
                        lore = listOf("", itemLore)
                    }

                    // todo: send request on GUI close so player can't spam API reqs by clicking
                    onclick = {
                        if (isActive) {
                            PlayerFeature.setActiveTag(context.uuid.toString(), null)
                            profile.activeTagId = null
                        } else if (profile.tagIds.contains(tag._id)) {
                            PlayerFeature.setActiveTag(
                                context.uuid.toString(),
                                tag
                            )
                            profile.activeTagId = tag._id
                        }

                        refresh()
                    }
                }
            }
        }
    }
}