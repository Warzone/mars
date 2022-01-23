package network.warzone.mars.player.feature

import com.github.kittinunf.result.getOrNull
import com.github.kittinunf.result.onFailure
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.http.ApiExceptionResponse
import network.warzone.mars.api.http.ApiExceptionType
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.player.feature.exceptions.NoteMissingException
import network.warzone.mars.player.feature.exceptions.PlayerMissingException
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.player.models.Session
import network.warzone.mars.punishment.models.Punishment
import network.warzone.mars.rank.exceptions.RankAlreadyPresentException
import network.warzone.mars.rank.exceptions.RankMissingException
import network.warzone.mars.rank.models.Rank
import network.warzone.mars.tag.TagFeature
import network.warzone.mars.tag.exceptions.TagAlreadyPresentException
import network.warzone.mars.tag.exceptions.TagMissingException
import network.warzone.mars.tag.exceptions.TagNotPresentException
import network.warzone.mars.tag.models.Tag
import network.warzone.mars.utils.mapErrorSmart
import network.warzone.mars.utils.parseHttpException
import java.util.*

object PlayerService {
    suspend fun login(playerId: UUID, playerName: String, ip: String): PlayerLoginResponse {
        return ApiClient.post(
            "/mc/players/login", PlayerLoginRequest(
                SimplePlayer(playerId, playerName),
                ip
            )
        )
    }

    suspend fun logout(playerId: UUID, playerName: String, playtime: Long) {
        ApiClient.post<ApiExceptionResponse, PlayerLogoutRequest>(
            "/mc/players/logout", PlayerLogoutRequest(
                SimplePlayer(playerId, playerName),
                playtime
            )
        )
    }

    suspend fun getPunishmentHistory(player: String): List<Punishment> {
        val request = parseHttpException { ApiClient.get<List<Punishment>>("/mc/players/$player/punishments") }
        val punishments = request.getOrNull()
        if (punishments != null) return punishments

        request.onFailure {
            when (it.code) {
                ApiExceptionType.PLAYER_MISSING -> throw PlayerMissingException(player)
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }

        throw RuntimeException("Unreachable")
    }

    suspend fun lookup(player: String, includeAlts: Boolean): PlayerLookupResponse {
        val request =
            parseHttpException { ApiClient.get<PlayerLookupResponse>("/mc/players/$player/lookup?alts=$includeAlts") }
        val lookup = request.getOrNull()
        if (lookup != null) return lookup

        request.onFailure {
            when (it.code) {
                ApiExceptionType.PLAYER_MISSING -> throw PlayerMissingException(player)
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }

        throw RuntimeException("Unreachable")
    }

    suspend fun addRank(player: String, rank: Rank): PlayerProfile {
        val request = parseHttpException { ApiClient.put<PlayerProfile>("/mc/players/$player/ranks/${rank._id}") }
        val profile = request.getOrNull()
        if (profile != null) return profile

        request.onFailure {
            when (it.code) {
                ApiExceptionType.PLAYER_MISSING -> throw PlayerMissingException(player)
                ApiExceptionType.RANK_MISSING -> throw RankMissingException(rank.name)
                ApiExceptionType.RANK_ALREADY_PRESENT -> throw RankAlreadyPresentException(
                    player,
                    rank.name
                )
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }

        throw RuntimeException("Unreachable")
    }

    suspend fun removeRank(player: String, rank: Rank): PlayerProfile {
        val request = parseHttpException { ApiClient.delete<PlayerProfile>("/mc/players/$player/ranks/${rank._id}") }
        val profile = request.getOrNull()
        if (profile != null) return profile

        request.onFailure {
            when (it.code) {
                ApiExceptionType.PLAYER_MISSING -> throw PlayerMissingException(player)
                ApiExceptionType.RANK_MISSING -> throw RankMissingException(rank.name)
                ApiExceptionType.RANK_NOT_PRESENT -> throw RankAlreadyPresentException(
                    player,
                    rank.name
                )
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }

        throw RuntimeException("Unreachable")
    }

    suspend fun addTag(player: String, tag: String): PlayerProfile {
        val request = parseHttpException { ApiClient.put<PlayerProfile>("/mc/players/$player/tags/${tag}") }
        val profile = request.getOrNull()
        if (profile != null) return profile

        request.onFailure {
            when (it.code) {
                ApiExceptionType.PLAYER_MISSING -> throw PlayerMissingException(player)
                ApiExceptionType.TAG_MISSING -> throw TagMissingException(tag)
                ApiExceptionType.TAG_ALREADY_PRESENT -> throw TagAlreadyPresentException(
                    player,
                    tag
                )
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }

        throw RuntimeException("Unreachable")
    }

    suspend fun removeTag(player: String, tag: String): PlayerProfile {
        val request = parseHttpException { ApiClient.delete<PlayerProfile>("/mc/players/$player/tags/$tag") }
        val profile = request.getOrNull()
        if (profile != null) return profile

        request.onFailure {
            when (it.code) {
                ApiExceptionType.PLAYER_MISSING -> throw PlayerMissingException(player)
                ApiExceptionType.TAG_MISSING -> throw TagMissingException(tag)
                ApiExceptionType.TAG_NOT_PRESENT -> throw TagNotPresentException(
                    player,
                    tag
                )
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }

        throw RuntimeException("Unreachable")
    }

    suspend fun setActiveTag(player: String, tag: Tag?): PlayerProfile {
        val request = parseHttpException {
            ApiClient.put<PlayerProfile, PlayerActiveTagRequest>(
                "/mc/players/$player/active_tag",
                PlayerActiveTagRequest(tag?._id)
            )
        }
        val profile = request.getOrNull()
        if (profile != null) return profile

        request.onFailure {
            when (it.code) {
                ApiExceptionType.PLAYER_MISSING -> throw PlayerMissingException(player)
                ApiExceptionType.TAG_MISSING -> throw TagMissingException(tag!!.name)
                ApiExceptionType.TAG_NOT_PRESENT -> throw TagNotPresentException(
                    player,
                    tag!!.name
                )
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }

        throw RuntimeException("Unreachable")
    }

    suspend fun addNote(player: String, content: String, author: SimplePlayer): PlayerProfile {
        val request = parseHttpException {
            ApiClient.put<PlayerProfile, PlayerAddNoteRequest>(
                "/mc/players/$player/notes",
                PlayerAddNoteRequest(author, content)
            )
        }
        val profile = request.getOrNull()
        if (profile != null) return profile

        request.onFailure {
            when (it.code) {
                ApiExceptionType.PLAYER_MISSING -> throw PlayerMissingException(player)
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }

        throw RuntimeException("Unreachable")
    }

    suspend fun removeNote(player: String, id: Int): PlayerProfile {
        val request = parseHttpException { ApiClient.delete<PlayerProfile>("/mc/players/$player/notes/$id") }
        val profile = request.getOrNull()
        if (profile != null) return profile

        request.onFailure {
            when (it.code) {
                ApiExceptionType.PLAYER_MISSING -> throw PlayerMissingException(player)
                ApiExceptionType.NOTE_MISSING -> throw NoteMissingException(id)
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }

        throw RuntimeException("Unreachable")
    }

    data class PlayerActiveTagRequest(val activeTagId: UUID?)

    data class PlayerAddNoteRequest(val author: SimplePlayer, val content: String)

    data class PlayerLoginRequest(val player: SimplePlayer, val ip: String)

    data class PlayerLoginResponse(
        val new: Boolean,
        val player: PlayerProfile,
        val activeSession: Session?,
        val activePunishments: List<Punishment>
    )

    data class PlayerLogoutRequest(val player: SimplePlayer, val playtime: Long)

    data class PlayerAltResponse(val player: PlayerProfile, val punishments: List<Punishment>)

    data class PlayerLookupResponse(val player: PlayerProfile, val alts: List<PlayerAltResponse>)
}