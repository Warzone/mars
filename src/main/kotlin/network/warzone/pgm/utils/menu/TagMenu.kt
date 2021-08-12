package network.warzone.pgm.utils.menu

import network.warzone.pgm.player.PlayerContext
import org.bukkit.ChatColor.GREEN
import org.bukkit.ChatColor.YELLOW
import org.bukkit.Material

class TagMenu(name: String, rows: Int, val context: PlayerContext) : PlayerMenu(name, rows, context.player) {

    override suspend fun draw() {
        val profile = context.getPlayerProfile()
        val tags = profile.tags()

        tags.forEachIndexed { index, tag ->
            val isActive = profile.activeTagId != null && profile.activeTagId!! == tag._id

            // Item
            val material = if (isActive) Material.MAP else Material.PAPER
            val lore = if (isActive) "${YELLOW}Click to deselect." else "${GREEN}Click to select."

            //set(, )
        }
    }

}