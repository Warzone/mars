package network.warzone.pgm.api.socket

import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import io.reactivex.Flowable
import network.warzone.pgm.api.Packet

interface WarzoneService {

    @Send
    fun send(packet: Packet<out Any>)

    @Receive
    fun receive(): Flowable<Packet<Any>>

}