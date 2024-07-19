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
import kotlinx.coroutines.runBlocking
import network.warzone.mars.api.ApiClient
import network.warzone.mars.commands.CommandModule
import network.warzone.mars.feature.FeatureManager
import network.warzone.mars.match.MatchManager
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.achievements.AchievementManager
import network.warzone.mars.player.decoration.PrefixDecorationProvider
import network.warzone.mars.player.feature.PlayerService
import network.warzone.mars.player.models.PlayerStats
import network.warzone.mars.player.tablist.overrideTabManager
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import tc.oc.pgm.api.PGM
import tc.oc.pgm.tablist.MatchTabManager
import java.util.*
import java.util.concurrent.CompletableFuture

class Mars : JavaPlugin() {
    companion object {
        lateinit var instance: Mars

        fun async(block: suspend() -> Unit) {
            Bukkit.getScheduler().runTaskAsynchronously(get()) {
                runBlocking { block.invoke() }
            }
        }

        fun asyncAsFuture(block: suspend() -> Unit) : CompletableFuture<Void?> {
            val future = CompletableFuture<Void?>()
            Bukkit.getScheduler().runTaskAsynchronously(get()) {
                runBlocking {
                    block.invoke()
                    future.complete(null)
                }
            }
            return future
        }

        fun <T> asyncAsFutureWithResult(block: suspend() -> T) : CompletableFuture<T> {
            val future = CompletableFuture<T>()
            Bukkit.getScheduler().runTaskAsynchronously(get()) {
                runBlocking {
                    val data = block.invoke()
                    future.complete(data)
                }
            }
            return future
        }

        fun sync(block: () -> Unit) = Bukkit.getScheduler().runTask(get(), block) // Run next tick

        fun get() = instance

        fun registerEvents(listener: Listener) = Bukkit.getPluginManager().registerEvents(listener, instance)

    }

    lateinit var serverId: String

    override fun onEnable() = runBlocking {
        instance = this@Mars

        this@Mars.saveDefaultConfig()

        serverId = config.getString("server.id")

        val commandGraph = BasicBukkitCommandGraph(CommandModule)


        FeatureManager.registerCommands(commandGraph)
        AchievementManager.load()

        val apiConfigurationSection = config.getConfigurationSection("api")
        ApiClient.loadHttp(apiConfigurationSection)
        ApiClient.loadSocket(serverId, apiConfigurationSection)

        PlayerStats.useExponentialExp(config.getBoolean("server.exponential-exp", false))

        MatchManager.init()

        BukkitIntake(this@Mars, commandGraph).register()

        overrideDefaultProviders()
    }

    override fun onDisable() = runBlocking {
        Bukkit.getOnlinePlayers().forEach {
            val activeSession = PlayerManager.getPlayer(it.uniqueId)?.activeSession!!
            val sessionLength = Date().time - activeSession.createdAt.time
            PlayerService.logout(it.uniqueId, it.name, activeSession._id, sessionLength)
        }

        AchievementManager.unload()
    }

    private fun overrideDefaultProviders() {
        PGM.get().nameDecorationRegistry.setProvider(PrefixDecorationProvider())
        this.overrideTabManager()
    }

}
