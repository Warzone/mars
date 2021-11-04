package network.warzone.mars.tags

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.failure
import com.github.kittinunf.result.map
import network.warzone.mars.feature.named.NamedCacheFeature
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.tags.commands.TagCommand
import network.warzone.mars.tags.commands.TagsCommand
import network.warzone.mars.tags.exceptions.TagConflictException
import network.warzone.mars.tags.exceptions.TagMissingException
import network.warzone.mars.tags.models.Tag
import network.warzone.mars.utils.FeatureException
import java.util.*

object TagFeature : NamedCacheFeature<Tag, TagService>() {

    override val service = TagService

    override suspend fun init() {
        list()
    }

    suspend fun createTag(name: String, display: String): Result<Tag, TagConflictException> {
        if (has(name)) throw TagConflictException(name)

        return service
            .create(name, display)
            .map { add(it) }
    }

    suspend fun updateTag(target: UUID, newTag: Tag): Result<Tag, FeatureException> {
        if (has(newTag.name) && getKnown(newTag.name)._id != target) return Result.failure(TagConflictException(newTag.name))
        if (!has(target)) return Result.failure(TagMissingException(target.toString()))

        set(target, newTag)

        return service.update(
            target,
            newTag.name,
            newTag.display
        ).map { newTag }
    }

    suspend fun deleteTag(uuid: UUID): Result<Unit, TagMissingException> {
        service
            .delete(uuid)
            .failure { return Result.failure(it) }

        PlayerFeature.query {
            it.tagIds.contains(uuid)
        }.forEach {
            it.tagIds.remove(uuid)

            if (it.activeTagId == uuid) it.activeTagId = null

            it.generate()
        }

        invalidate(uuid)

        return Result.success(Unit)
    }

    suspend fun list(): List<Tag> {
        return service.list()
            .also(::sync)
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