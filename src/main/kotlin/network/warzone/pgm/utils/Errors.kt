package network.warzone.pgm.utils

import app.ashcon.intake.CommandException
import network.warzone.pgm.api.exceptions.ApiException
import network.warzone.pgm.api.http.Response

fun <T : Response> T.except(): T {
    if (this.error) throw ApiException(this.code!!, this.message!!)

    return this
}

suspend fun <T> verify(block: suspend () -> T?): T {
    try {
        return block()!!
    } catch (e: ApiException) {
        throw CommandException("[${e.code}] ${e.message}")
    }
}