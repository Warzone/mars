package network.warzone.mars.match.models

import org.bukkit.Material
import org.bukkit.event.entity.EntityDamageEvent
import tc.oc.pgm.api.tracker.info.DamageInfo
import tc.oc.pgm.api.tracker.info.FallInfo
import tc.oc.pgm.api.tracker.info.MeleeInfo
import tc.oc.pgm.api.tracker.info.PotionInfo
import tc.oc.pgm.tracker.info.*

enum class DeathCause {
    MELEE,
    PROJECTILE,
    EXPLOSION,
    FIRE,
    LAVA,
    POTION,
    FLATTEN,
    SPLEEF,
    FALL,
    PRICK,
    DROWN,
    SHOCK,
    STARVE,
    SUFFOCATE,
    VOID,
    UNKNOWN;

    companion object {
        fun fromDamageInfo(info: DamageInfo): DeathCause {
            return when (info) {
                is MeleeInfo -> MELEE
                is ProjectileInfo -> if (info.projectile is PotionInfo) POTION else PROJECTILE
                is ExplosionInfo -> EXPLOSION
                is FireInfo -> if (info.igniter == null) LAVA else FIRE
                is PotionInfo -> POTION
                is FallingBlockInfo -> FLATTEN
                is BlockInfo ->
                    when (info.material.itemType) {
                        Material.ANVIL -> FLATTEN
                        Material.CACTUS -> PRICK
                        else -> SUFFOCATE
                    }
                is FallInfo ->
                    if (info.cause !is SpleefInfo)
                        when (info.to) {
                            FallInfo.To.LAVA -> LAVA
                            FallInfo.To.VOID -> VOID
                            FallInfo.To.GROUND -> FALL
                            else -> FALL
                        } else SPLEEF
                is GenericDamageInfo ->
                    when (info.damageType) {
                        EntityDamageEvent.DamageCause.CONTACT -> PRICK
                        EntityDamageEvent.DamageCause.DROWNING -> DROWN
                        EntityDamageEvent.DamageCause.LIGHTNING -> SHOCK
                        EntityDamageEvent.DamageCause.STARVATION -> STARVE
                        EntityDamageEvent.DamageCause.SUFFOCATION -> SUFFOCATE
                        EntityDamageEvent.DamageCause.CUSTOM -> UNKNOWN
                        else -> UNKNOWN
                    }
                else -> UNKNOWN
            }
        }
    }
}