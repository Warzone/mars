package network.warzone.pgm.api.http

import kotlinx.serialization.Serializable
import network.warzone.pgm.api.ErrorCode

@Serializable
abstract class Response {
    abstract val code: ErrorCode?
    abstract val message: String?
    abstract val error: Boolean
}