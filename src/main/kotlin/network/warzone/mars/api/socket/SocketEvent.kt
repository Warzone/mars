package network.warzone.mars.api.socket

import network.warzone.mars.api.socket.models.*
import network.warzone.mars.utils.KEvent
import kotlin.reflect.full.isSubclassOf

open class SocketEvent<T : Any>(val event: SocketEventType)

open class OutboundEvent<T : Any>(event: SocketEventType) : SocketEvent<T>(event) {
    object PlayerChat : OutboundEvent<PlayerChatData>(SocketEventType.PLAYER_CHAT)
    object PointCapture : OutboundEvent<PointCaptureData>(SocketEventType.CONTROL_POINT_CAPTURE)

    object CoreDamage : OutboundEvent<CoreDamageData>(SocketEventType.CORE_DAMAGE)
    object CoreLeak : OutboundEvent<CoreLeakData>(SocketEventType.CORE_LEAK)

    object DestroyableDamage : OutboundEvent<DestroyableDamageData>(SocketEventType.DESTROYABLE_DAMAGE)
    object DestroyableDestroy : OutboundEvent<DestroyableDestroyData>(SocketEventType.DESTROYABLE_DESTROY)

    object FlagPickup : OutboundEvent<FlagPickupData>(SocketEventType.FLAG_PICKUP)
    object FlagCapture : OutboundEvent<FlagCaptureData>(SocketEventType.FLAG_CAPTURE)
    object FlagDrop : OutboundEvent<FlagDropData>(SocketEventType.FLAG_DROP)
    object FlagDefend : OutboundEvent<FlagDefendData>(SocketEventType.FLAG_DEFEND)

    object MatchLoad : OutboundEvent<MatchLoadData>(SocketEventType.MATCH_LOAD)
    object MatchStart : OutboundEvent<MatchStartData>(SocketEventType.MATCH_START)
    object MatchEnd : OutboundEvent<MatchEndData>(SocketEventType.MATCH_END)

    object PartyJoin : OutboundEvent<PartyJoinData>(SocketEventType.PARTY_JOIN)
    object PartyLeave : OutboundEvent<PartyLeaveData>(SocketEventType.PARTY_LEAVE)
    object PlayerDeath : OutboundEvent<PlayerDeathData>(SocketEventType.PLAYER_DEATH)
    object Killstreak : OutboundEvent<KillStreakData>(SocketEventType.KILLSTREAK)

    object WoolPickup : OutboundEvent<WoolData>(SocketEventType.WOOL_PICKUP)
    object WoolCapture : OutboundEvent<WoolData>(SocketEventType.WOOL_CAPTURE)
    object WoolDrop : OutboundEvent<WoolData>(SocketEventType.WOOL_DROP)
    object WoolDefend : OutboundEvent<WoolData>(SocketEventType.WOOL_DEFEND)
}

sealed class InboundEvent<T : Any>(event: SocketEventType, val bukkitEventFactory: (T) -> KEvent) : SocketEvent<T>(event) {
    companion object {
        val map = InboundEvent::class.nestedClasses
            .filter { it.isSubclassOf(InboundEvent::class) }
            .map { it.objectInstance }
            .filterIsInstance<InboundEvent<Any>>()
            .associateBy { it.event }

        fun get(type: SocketEventType): InboundEvent<Any>? = map[type]
    }

    // Currently unreferenced, like all inbound events will be, but could be useful to be able to reference them
    // in the future, i.e some sort of waitFor method
    object PlayerChat : InboundEvent<PlayerChatData>(SocketEventType.PLAYER_CHAT, ::PlayerChatEvent)
}