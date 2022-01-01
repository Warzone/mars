/*
    Warzone Mars - Interface with PGM for the purposes of data persistence & enhancing gameplay with new features

    Copyright (C) 2021 Warzone Contributors

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package network.warzone.mars

import app.ashcon.intake.bukkit.BukkitIntake
import app.ashcon.intake.bukkit.graph.BasicBukkitCommandGraph
import network.warzone.mars.api.ApiClient
import network.warzone.mars.commands.CommandModule
import network.warzone.mars.feature.FeatureManager
import network.warzone.mars.match.MatchManager
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class Mars : JavaPlugin() {
    companion object {
        lateinit var instance: Mars

        fun get() = instance

        fun registerEvents(listener: Listener) = Bukkit.getPluginManager().registerEvents(listener, instance)
    }

    lateinit var serverId: String

    override fun onEnable() {
        println("enabling!")
        instance = this

        serverId = config.getString("server.id")

        val commandGraph = BasicBukkitCommandGraph(CommandModule)

        FeatureManager.init()
        FeatureManager.registerCommands(commandGraph)

        val apiConfigurationSection = config.getConfigurationSection("api")
        ApiClient.loadHttp(apiConfigurationSection)
        ApiClient.loadSocket(serverId, apiConfigurationSection)

        MatchManager.init()

        BukkitIntake(this, commandGraph).register()
    }

    override fun onDisable() {
        println("hello!")
    }
}

