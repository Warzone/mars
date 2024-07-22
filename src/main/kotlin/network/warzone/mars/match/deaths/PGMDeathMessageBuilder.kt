package network.warzone.mars.match.deaths

import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import tc.oc.pgm.api.player.MatchPlayer
import tc.oc.pgm.api.player.ParticipantState
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent
import tc.oc.pgm.api.tracker.info.*
import tc.oc.pgm.tracker.Trackers
import tc.oc.pgm.tracker.info.*
import tc.oc.pgm.util.bukkit.EntityTypes
import tc.oc.pgm.util.material.Materials
import tc.oc.pgm.util.named.NameStyle
import tc.oc.pgm.util.text.TextTranslations
import java.lang.Double.isNaN
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.roundToLong

// PGMs DeathMessageBuilder converted to kotlin with extensibility to access text arguments.
open class PGMDeathMessageBuilder(event: MatchPlayerDeathEvent, logger: Logger) {
    internal class NoMessage : Exception()

    private val logger: Logger
    private val victim: MatchPlayer

    private val killer: ParticipantState?
    val predicted: Boolean
    var key: String? = null

    // FIXME: Adventure filters out empty() from translatable arguments
    // and it causes issues with the indexes of translations.
    var weapon: Component = Component.space()
    var mob: Component = Component.space()
    var distance: Long? = null

    val message: Component
        get() {
            var message: Component = Component.translatable(key!!, *args) // key will be non-null when this is called.

            if (predicted) message = message.append(Component.space()).append(Component.translatable("death.predictedSuffix"))

            return message
        }

    val args: Array<Component>
        get() {
            val args = arrayOf<Component>()
            args[0] = victim.getName(NameStyle.COLOR)
            args[1] = if (killer == null) Component.space() else killer.getName(NameStyle.COLOR)
            args[2] = weapon
            args[3] = mob
            args[4] = if (distance == null) Component.space() else Component.text(distance!!)
            return args
        }

    init {
        this.victim = event.victim
        this.killer = event.damageInfo.attacker
        this.predicted = event.isPredicted
        this.logger = logger

        build(event.damageInfo)
    }

    fun setDistance(n: Double) {
        if (!isNaN(n)) {
            distance = 0.0.coerceAtLeast(n).roundToLong()
            if (distance == 1L) distance = 2L // Cleverly ensure the text is always plural
        }
    }
    /*
   * Primitive methods for manipulating the key
   */
    /** Test if the given string is a prefix of any existing key  */
    fun exists(prefix: String?): Boolean {
        val key = getAllKeys()!!.tailSet(prefix).first()
        return key != null && key.startsWith(prefix!!)
    }

    /** Return a new key built from the current key with the given tokens appended  */
    fun append(vararg tokens: String): String? {
        var newKey = key
        for (token: String in tokens) {
            newKey += ".$token"
        }
        return newKey
    }

    /**
     * Try to append an optional sequence of tokens to the current key. If the new key is invalid, the
     * current key is not changed.
     */
    fun option(vararg tokens: String): Boolean {
        val newKey = append(*tokens)
        if (exists(newKey)) {
            key = newKey
            return true
        }
        return false
    }

    /**
     * Append a sequence of tokens to the current key.
     *
     * @throws NoMessage if the new key is not valid
     */
    fun require(vararg tokens: String) {
        val newKey = append(*tokens)
        if (!exists(newKey)) {
            logger.warning("Generated invalid death message key: $newKey")
            throw NoMessage()
        }

        key = newKey
    }

    /**
     * Assert that the current key is complete and valid.
     *
     * @throws NoMessage if it's not
     */
    fun finish() {
        if (!getAllKeys()!!.contains(key)) {
            throw NoMessage()
        }
    }

    /*
   * Optional components
   *
   * These methods all try
   * to append something to the key, and return true if successful.
   * If they fail, they leave the key unchanged.
   */
    fun variant(): Boolean {
        var count = 0
        while (getAllKeys()!!.contains("$key.$count")) {
            count++
        }

        if (count == 0) return false

        val variant = victim.match.random.nextInt(count)
        key += ".$variant"
        return true
    }

    open fun ranged(rangedInfo: RangedInfo?, distanceReference: Location?): Boolean {
        val distance = Trackers.distanceFromRanged(rangedInfo, distanceReference)
        if (!isNaN(distance) && option("distance")) {
            setDistance(distance)
            if (distance >= SNIPE_DISTANCE) {
                option("snipe")
            }
            return true
        }
        return false
    }

    open fun potion(potionInfo: PotionInfo): Boolean {
        if (option("potion")) {
            weapon = potionInfo.name
            return true
        }
        return false
    }

    open fun item(itemInfo: ItemInfo): Boolean {
        // TODO: Bukkit 1.13+ should be able to handle more than just weapons
        if (Materials.isWeapon(itemInfo.item.type) && option("item")) {
            weapon = itemInfo.name
            return true
        }

        return false
    }

    open fun block(blockInfo: BlockInfo): Boolean {
        if (option("block")) {
            weapon = blockInfo.name
            return true
        }
        return false
    }

    open fun entity(entityInfo: EntityInfo): Boolean {
        // Skip for entities that are weird and have no translations
        when (entityInfo.entityType) {
            EntityTypes.UNKNOWN,
            EntityTypes.COMPLEX_PART,
            EntityTypes.ENDER_CRYSTAL -> return false
        }

        if (option("entity")) {
            weapon = entityInfo.name
            option(entityInfo.identifier)
            return true
        }

        return false
    }

    open fun insentient(info: PhysicalInfo?): Boolean {
        if (info is PotionInfo) {
            if (potion(info)) {
                return true
            } else if (option("entity")) {
                // PotionInfo.getName returns a potion name,
                // which doesn't work outside a potion death message.
                weapon = Component.translatable("item.potion.name")
                return true
            }
        } else if (info is EntityInfo) {
            return info !is MobInfo && entity(info)
        } else if (info is BlockInfo) {
            return block(info)
        } else if (info is ItemInfo) {
            return item(info)
        }
        return false
    }

    open fun mob(mobInfo: MobInfo): Boolean {
        if (option("mob")) {
            mob = mobInfo.name
            option(mobInfo.identifier)
            return true
        }
        return false
    }

    open fun physical(info: PhysicalInfo?): Boolean {
        return if (info is MobInfo) {
            mob(info)
        } else {
            insentient(info)
        }
    }

    /*
   * Required components
   *
   * Each of these methods appends several keys to the death message,
   * and generally expects to complete successfully. If they fail, they
   * throw a {@link NoMessage} exception and leave the key in an
   * unknown state.
   */
    open fun player() {
        if (killer != null) require("player")
    }

    open fun attack(attacker: PhysicalInfo?, weapon: PhysicalInfo?) {
        player()

        if (attacker is MobInfo && !mob(attacker)) return

        insentient(weapon)
    }

    open fun generic(info: GenericDamageInfo) {
        when (info.damageType) {
            DamageCause.CONTACT -> require("cactus")
            DamageCause.DROWNING -> require("drown")
            DamageCause.LIGHTNING -> require("lightning")
            DamageCause.STARVATION -> require("starve")
            DamageCause.SUFFOCATION -> require("suffocate")
            DamageCause.CUSTOM -> require("generic")
            else -> require("unknown")
        }
    }

    open fun melee(melee: MeleeInfo) {
        require("melee")
        attack(melee, melee.weapon)
    }

    open fun magic(potion: PotionInfo?, attacker: PhysicalInfo?) {
        require("magic")
        attack(attacker, potion)
    }

    open fun projectile(projectile: ProjectileInfo, distanceReference: Location?) {
        if (projectile.projectile is PotionInfo) {
            try {
                magic(projectile.projectile as PotionInfo, projectile.shooter)
                return
            } catch (ignored: NoMessage) {
                // If we can't generate a magic message (probably because it's part
                // of a fall message), fall back to a projectile message.
            }
        }
        require("projectile")

        var info = projectile.projectile
        if (info is EntityInfo) {
            when (info.entityType) {
                EntityType.UNKNOWN,
                EntityType.ARROW,
                EntityType.WITHER_SKULL -> info = null // "shot by arrow" is redundant
            }
        } else {
            // Projectile name may be different than entity name e.g. custom projectile
            weapon = projectile.name
        }

        attack(projectile.shooter, info)
        ranged(projectile, distanceReference)
    }

    open fun squash(fallingBlock: PhysicalInfo?) {
        require("squash")
        attack(null, fallingBlock)
    }

    open fun suffocate(fallenBlock: PhysicalInfo?) {
        require("suffocate")
        attack(null, fallenBlock)
    }

    open fun cactus(fallenBlock: PhysicalInfo?) {
        require("cactus")
        attack(null, fallenBlock)
    }

    open fun explosion(explosion: ExplosionInfo, distanceReference: Location?) {
        require("explosive")
        player()
        physical(explosion.explosive)
        ranged(explosion, distanceReference)
    }

    open fun fire(fire: FireInfo) {
        require("fire")
        player()

        if (!(fire.igniter is BlockInfo && (fire.igniter as BlockInfo?)!!.material.itemType == Material.FIRE)) {
            // "burned by fire" is redundant
            physical(fire.igniter)
        }
    }

    open fun fall(fall: FallInfo) {
        require("fall")
        require(fall.to.name.toLowerCase())

        val cause = fall.cause
        if (cause is SpleefInfo) {
            require("spleef")
            val breaker = cause.breaker
            if (breaker is ExplosionInfo) {
                explosion(breaker, fall.origin)
            } else {
                player()
            }
        } else if (cause is DamageInfo) {
            damage(cause)
        } else if (fall.to == FallInfo.To.GROUND) {
            setDistance(Trackers.distanceFromRanged(fall, victim.bukkit.location))

            if (distance != null) {
                if (distance!! <= TRIPPED_HEIGHT) {
                    // Very short falls get a "tripped" message
                    option("tripped")
                } else if (distance!! >= ORBIT_HEIGHT) {
                    // Very long falls get an "orbit" message
                    option("orbit")
                } else if (victim.match.random.nextFloat() < 0.01f) {
                    // Occasionally they get a rare message
                    option("rare")
                }

                // Show distance if it's high enough and the message supports it
                if (distance!! >= NOTABLE_HEIGHT) option("distance")
            }
        }
    }

    open fun damage(info: DamageInfo?) {
        when (info) {
            is MeleeInfo -> melee(info)
            is ProjectileInfo -> projectile(info, victim.bukkit.location)
            is ExplosionInfo -> explosion(info, victim.bukkit.location)
            is FireInfo -> fire(info)
            is PotionInfo -> magic(info, null)
            is FallingBlockInfo -> squash(info)
            is BlockInfo ->
                when (info.material.itemType) {
                    Material.ANVIL -> squash(info)
                    Material.CACTUS -> cactus(info)
                    else -> suffocate(info)
                }
            is FallInfo -> fall(info)
            is GenericDamageInfo -> generic(info)
            else -> throw NoMessage()
        }
    }

    fun build(damageInfo: DamageInfo) {
        logger.fine("Generating death message for $damageInfo")
        try {
            key = "death"
            damage(damageInfo)
            variant()
            finish()
        } catch (ex: NoMessage) {
            logger.log(
                Level.SEVERE,
                "Generated invalid death message '$key' for victim=$victim info=$damageInfo killer=$killer weapon=$weapon mob=$mob distance=$distance",
                ex
            )
            key = "death.generic"
        }
    }

    companion object {
        private var _allKeys: SortedSet<String>? = null

        fun getAllKeys(): SortedSet<String>? {
            if (_allKeys == null) {
                _allKeys = TextTranslations.getKeys().tailSet("death.")
            }
            return _allKeys
        }

        private const val SNIPE_DISTANCE: Long = 60
        private const val TRIPPED_HEIGHT = 5
        private const val NOTABLE_HEIGHT = 12
        private const val ORBIT_HEIGHT = 60
    }
}