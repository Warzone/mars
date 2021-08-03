package network.warzone.pgm.api.socket

import network.warzone.pgm.api.AuthData

sealed class OutboundEvent<T>(val eventName: String) {
    object IDENTIFY : OutboundEvent<AuthData>("IDENTIFY")
}