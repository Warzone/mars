package network.warzone.mars.api.socket

import network.warzone.mars.api.socket.models.*

open class SocketEvent<T : Any>(val event: SocketEventType)

open class OutboundEvent<T : Any>(event: SocketEventType) : SocketEvent<T>(event) {
    object PlayerChat : OutboundEvent<PlayerChatData>(SocketEventType.PLAYER_CHAT)
    object PointCapture : OutboundEvent<PointCaptureData>(SocketEventType.CONTROL_POINT_CAPTURE)

    object CoreLeak : OutboundEvent<CoreLeakData>(SocketEventType.CORE_LEAK)

    object DestroyableDestroy : OutboundEvent<DestroyableDestroyData>(SocketEventType.DESTROYABLE_DESTROY)
    object DestroyableDamage : OutboundEvent<DestroyableDamageData>(SocketEventType.DESTROYABLE_DAMAGE)

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

    object Killstreak : OutboundEvent<KillstreakData>(SocketEventType.KILLSTREAK)

    object WoolPickup : OutboundEvent<WoolData>(SocketEventType.WOOL_PICKUP)
    object WoolCapture : OutboundEvent<WoolDropData>(SocketEventType.WOOL_CAPTURE)
    object WoolDrop : OutboundEvent<WoolDropData>(SocketEventType.WOOL_DROP)
    object WoolDefend : OutboundEvent<WoolData>(SocketEventType.WOOL_DEFEND)

    object PlayerAchievement : OutboundEvent<PlayerAchievementData>(SocketEventType.ACHIEVEMENT_EARN)
}
