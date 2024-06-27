package network.warzone.mars.feature

import java.util.*

abstract class Feature<T : Resource> {
    /**
     * Fetch latest resource from API, not local cache.
     */
    abstract suspend fun fetch(target: String): T?

    /**
     * Query cache exclusively
     */
    open fun fetchCached(target: String): T? { return null }

    /**
     * Get resource from cache first, and API if not in cache.
     * Defaults to API fetch if feature has no cache.
     */
    open suspend fun get(id: UUID): T? {
        return fetch(id.toString())
    }

    /**
     * Called upon WebSocket connection so the feature can make initial requests (through HTTP or WS).
     */
    open suspend fun init() {}

    /**
     * Command labels mapped to command classes with subcommand handlers returned from this method are registered with the command framework.
     * Example: /rank create, /rank delete - `getSubcommands()` would return "rank" mapped to `RankCommands()`.
     */
    open fun getSubcommands(): Map<List<String>, Any> {
        return emptyMap()
    }

    /**
     * The command classes returned from this method are registered with the command framework.
     * Example: /tags - `getSubcommands()` would return `listOf(TagsCommand())`.
     */
    open fun getCommands(): List<Any> {
        return listOf()
    }
}