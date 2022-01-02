package network.warzone.mars.tag

import network.warzone.mars.api.ApiClient
import network.warzone.mars.feature.NamedCachedFeature
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.tag.commands.TagCommand
import network.warzone.mars.tag.commands.TagsCommand
import network.warzone.mars.tag.exceptions.TagConflictException
import network.warzone.mars.tag.exceptions.TagMissingException
import network.warzone.mars.tag.models.Tag
import java.util.*

object TagFeature : NamedCachedFeature<Tag>() {
    override suspend fun init() {
        list()
    }

    override suspend fun fetch(target: String): Tag? {
        return try {
            ApiClient.get<Tag>("/mc/tags/$target")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun create(name: String, display: String): Tag {
        if (has(name)) throw TagConflictException(name)

        return TagService
            .create(name, display)
            .also { add(it) }
    }

    suspend fun update(target: UUID, newTag: Tag): Tag {
        if (has(newTag.name) && getKnown(newTag.name)._id != target) throw TagConflictException(newTag.name)
        if (!has(target)) throw TagMissingException(newTag.name)

        return TagService.update(
            target,
            newTag.name,
            newTag.display
        ).also { add(it) }
    }

    suspend fun delete(uuid: UUID) {
        TagService.delete(uuid)

        PlayerFeature.query {
            it.tagIds.contains(uuid)
        }.forEach {
            it.tagIds.remove(uuid)

            if (it.activeTagId == uuid) it.activeTagId = null
        }

        remove(uuid)
    }

    suspend fun list(): List<Tag> {
        return TagService.list()
            .onEach { add(it) }
    }

    override fun getSubcommands(): Map<List<String>, Any> {
        return mapOf(
            listOf("tag") to TagCommand()
        )
    }

    override fun getCommands(): List<Any> {
        return listOf(TagsCommand())
    }
}