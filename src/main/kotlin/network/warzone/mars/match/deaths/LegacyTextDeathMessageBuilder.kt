package network.warzone.mars.match.deaths

import network.warzone.mars.utils.createLogger
import org.bukkit.Location
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent
import tc.oc.pgm.api.tracker.info.PhysicalInfo
import tc.oc.pgm.api.tracker.info.PotionInfo
import tc.oc.pgm.tracker.info.*
import tc.oc.pgm.util.bukkit.EntityTypes
import tc.oc.pgm.util.material.Materials

class LegacyTextDeathMessageBuilder(event: MatchPlayerDeathEvent)
    : PGMDeathMessageBuilder(event, createLogger("LegacyTextDeathMessageBuilder")) {

    var weaponString: String? = null
    var mobString: String? = null

    fun test(vararg tokens: String): Boolean = exists(append(*tokens))

    override fun block(blockInfo: BlockInfo): Boolean {
        if (test("block")) weaponString = blockInfo.identifier
        return super.block(blockInfo)
    }

    override fun entity(entityInfo: EntityInfo): Boolean {
        when (entityInfo.entityType) {
            EntityTypes.UNKNOWN,
            EntityTypes.COMPLEX_PART,
            EntityTypes.ENDER_CRYSTAL -> return false
        }

        if (test("entity")) weaponString = entityInfo.identifier
        return super.entity(entityInfo)
    }

    override fun insentient(info: PhysicalInfo?): Boolean {
        if (info is PotionInfo && test("entity")) weaponString = "item.potion.name"
        return super.insentient(info)
    }

    override fun item(itemInfo: ItemInfo): Boolean {
        if (Materials.isWeapon(itemInfo.item.type) && test("item")) weaponString = itemInfo.identifier
        return super.item(itemInfo)
    }

    override fun potion(potionInfo: PotionInfo): Boolean {
        if (test("potion")) weaponString = potionInfo.identifier
        return super.potion(potionInfo)
    }

    override fun projectile(projectile: ProjectileInfo, distanceReference: Location?) {
        if (projectile.projectile !is PotionInfo && projectile.projectile !is EntityInfo) weaponString = projectile.identifier
        super.projectile(projectile, distanceReference)
    }

    override fun mob(mobInfo: MobInfo): Boolean {
        if (test("mob")) mobString = mobInfo.identifier
        return super.mob(mobInfo)
    }
}