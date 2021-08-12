package network.warzone.pgm.utils

import com.github.kittinunf.result.Result
import com.google.gson.JsonSyntaxException
import io.ktor.client.features.*
import io.ktor.client.statement.*
import network.warzone.pgm.api.exceptions.ApiException
import network.warzone.pgm.api.http.ApiExceptionResponse
import java.nio.charset.Charset

/**
 * Parses a [ClientRequestException] to an [ApiException] for easier use.
 *
 * @param block The block which can throw a [ClientRequestException].
 * @throws RuntimeException if the [ClientRequestException] can not be parsed as an [ApiException]
 *
 * @return The non-null result [T] of the block or [ApiException] if a [ClientRequestException] was thrown.
 */
suspend fun <T> parseHttpException(block: suspend() -> T?): Result<T, ApiException> {
    try {
        // Try running the block, asserting it to be non-null if no http exception was thrown.
        return Result.success(block()!!)
    } catch (e: ClientRequestException) {
        // Read the lines of the exception.
        val lines = e.response.readText(Charset.defaultCharset())

        println("lines $lines")

        // Try parsing the lines to an ApiExceptionResponse, throwing a RuntimeException if that fails.
        val res = try {
            GSON.fromJson(lines, ApiExceptionResponse::class.java)
        } catch (e: JsonSyntaxException) {
            throw RuntimeException("oh no jellz's api is shit and not giving us a valid errorful response!")
        }

        println("res $res")

        // Throws
        return Result.failure(ApiException(res.code, res.message))
    }
}



/**
 * Really surprised this method doesn't exist already. They have [Result.mapError] but it randomly drops [T] and uses star projection instead.
 */
inline fun <reified T, reified E : Throwable, reified EE : Throwable> Result<T, E>.mapErrorSmart(transform: (E) -> EE): Result<T, EE> = try {
    when (this) {
        is Result.Success -> Result.success(value)
        is Result.Failure -> Result.failure(transform(error))
    }
} catch (ex: Exception) {
    when (ex) {
        is EE -> Result.failure(ex)
        else -> throw ex
    }
}