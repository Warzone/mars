package network.warzone.pgm.api.socket

import com.tinder.scarlet.ws.Send

interface WarzoneService {

    @Send
    fun send(bytes: List<Byte>)

//    @Send
//    fun sendMatchStart()

}