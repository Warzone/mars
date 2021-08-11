package network.warzone.pgm.tags

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.failure
import com.github.kittinunf.result.map
import network.warzone.pgm.feature.named.NamedCacheFeature
import network.warzone.pgm.player.feature.PlayerFeature
import network.warzone.pgm.ranks.RankFeature
import network.warzone.pgm.ranks.exceptions.RankMissingException
import network.warzone.pgm.tags.commands.TagCommands
import network.warzone.pgm.tags.exceptions.TagConflictException
import network.warzone.pgm.tags.exceptions.TagMissingException
import network.warzone.pgm.tags.models.Tag
import network.warzone.pgm.utils.FeatureException
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
        if (has(newTag.name)) return Result.failure(TagConflictException(newTag.name))
        if (!has(target)) return Result.failure(TagMissingException(target.toString()))

        set(target, newTag)

        return service.update(
            target,
            newTag.name,
            newTag.display
        ).map { newTag }
    }

    suspend fun deleteTag(uuid: UUID): Result<Unit, RankMissingException> {
        RankFeature.service
            .delete(uuid)
            .failure { return Result.failure(it) }

        PlayerFeature.query {
            it.tagIds.contains(uuid)
        }.forEach {
            it.tagIds.remove(uuid)

            it.generate()
        }

        invalidate(uuid)

        return Result.success(Unit)
    }

    suspend fun list(): List<Tag> {
        return service.list()
            .also(::sync)
    }

    override fun getCommands(): List<Any> {
        return listOf(TagCommands())
    }
}