package network.warzone.pgm.utils

import app.ashcon.intake.CommandException
import app.ashcon.intake.parametric.ProvisionException
import network.warzone.pgm.api.exceptions.ApiException
import network.warzone.pgm.api.http.Response

fun <T : Response> T.except(): T {
    if (this.error) throw ApiException(this.code!!, this.message!!)

    return this
}

suspend fun <T> command(block: suspend () -> T?): T {
    try {
        return block()!!
    } catch (e: ApiException) {
        throw CommandException("[${e.code}] ${e.message}")
    }
}

suspend fun <T> provide(block: suspend () -> T?): T {
    try {
        return block()!!
    } catch (e: ApiException) {
        throw ProvisionException("[${e.code}] ${e.message}")
    }
}