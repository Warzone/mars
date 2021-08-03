package network.warzone.pgm.api.http

import kotlinx.serialization.Serializable
import network.warzone.pgm.api.ErrorCode
import network.warzone.pgm.api.exceptions.ApiException

@Serializable
abstract class Response {
    abstract val code: ErrorCode?
    abstract val message: String?
    abstract val error: Boolean
}