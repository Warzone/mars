package network.warzone.mars.utils

import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Key.key
import tc.oc.pgm.util.Version
import tc.oc.pgm.util.platform.Platform

object Sounds {

    private val IS_LEGACY = Platform.MINECRAFT_VERSION.isOlderThan(Version(1, 9, 0))
    
    private val mappings: MutableMap<String, String> = mutableMapOf() // Modern -> Legacy

    //<editor-fold desc="Sound Mappings">
    // Adapted from ViaVersion - https://github.com/ViaVersion/ViaVersion (GPL v3.0)
    // Copyright (C) 2016-2024 ViaVersion and contributors
    // This code is part of a project licensed under the GNU General Public License v3.0
    // Retrieved from https://github.com/ViaVersion/ViaVersion/blob/f8eb57cdbb8d0af8384b4e6edc0b1e82297e3135/common/src/main/java/com/viaversion/viaversion/protocols/v1_8to1_9/data/SoundEffectMappings1_9.java
    val MOB_HORSE_ZOMBIE_IDLE = register("mob.horse.zombie.idle", "entity.zombie_horse.ambient")
    val NOTE_SNARE = register("note.snare", "block.note.snare")
    val RANDOM_WOOD_CLICK = register("random.wood_click", "block.wood_button.click_on")
    val DIG_GRAVEL = register("dig.gravel", "block.gravel.break")
    val RANDOM_BOWHIT = register("random.bowhit", "entity.arrow.hit")
    val DIG_GLASS = register("dig.glass", "block.glass.break")
    val MOB_ZOMBIE_SAY = register("mob.zombie.say", "entity.zombie.ambient")
    val MOB_PIG_DEATH = register("mob.pig.death", "entity.pig.death")
    val MOB_HORSE_DONKEY_HIT = register("mob.horse.donkey.hit", "entity.donkey.hurt")
    val GAME_NEUTRAL_SWIM = register("game.neutral.swim", "entity.player.swim")
    val GAME_PLAYER_SWIM = register("game.player.swim", "entity.player.swim")
    val MOB_ENDERMEN_IDLE = register("mob.endermen.idle", "entity.endermen.ambient")
    val PORTAL_PORTAL = register("portal.portal", "block.portal.ambient")
    val RANDOM_FIZZ = register("random.fizz", "entity.generic.extinguish_fire")
    val NOTE_HARP = register("note.harp", "block.note.harp")
    val STEP_SNOW = register("step.snow", "block.snow.step")
    val RANDOM_SUCCESSFUL_HIT = register("random.successful_hit", "entity.arrow.hit_player")
    val MOB_ZOMBIEPIG_ZPIGHURT = register("mob.zombiepig.zpighurt", "entity.zombie_pig.hurt")
    val MOB_WOLF_HOWL = register("mob.wolf.howl", "entity.wolf.howl")
    val FIREWORKS_LAUNCH = register("fireworks.launch", "entity.firework.launch")
    val MOB_COW_HURT = register("mob.cow.hurt", "entity.cow.hurt")
//    val FIREWORKS_LARGEBLAST = register("fireworks.largeBlast", "entity.firework.large_blast") // legacy key does not follow the allowed pattern
    val MOB_BLAZE_HIT = register("mob.blaze.hit", "entity.blaze.hurt")
    val MOB_VILLAGER_DEATH = register("mob.villager.death", "entity.villager.death")
    val MOB_BLAZE_DEATH = register("mob.blaze.death", "entity.blaze.death")
    val MOB_HORSE_ZOMBIE_DEATH = register("mob.horse.zombie.death", "entity.zombie_horse.death")
    val MOB_SILVERFISH_KILL = register("mob.silverfish.kill", "entity.silverfish.death")
    val MOB_WOLF_PANTING = register("mob.wolf.panting", "entity.wolf.pant")
    val NOTE_BASS = register("note.bass", "block.note.bass")
    val DIG_STONE = register("dig.stone", "block.stone.break")
    val MOB_ENDERMEN_STARE = register("mob.endermen.stare", "entity.endermen.stare")
    val GAME_PLAYER_SWIM_SPLASH = register("game.player.swim.splash", "entity.generic.splash")
    val MOB_SLIME_SMALL = register("mob.slime.small", "entity.small_slime.hurt")
    val MOB_GHAST_DEATH = register("mob.ghast.death", "entity.ghast.death")
    val MOB_GUARDIAN_ATTACK = register("mob.guardian.attack", "entity.guardian.attack")
    val RANDOM_CLICK = register("random.click", "block.dispenser.dispense")
    val MOB_ZOMBIEPIG_ZPIG = register("mob.zombiepig.zpig", "entity.zombie_pig.ambient")
    val GAME_PLAYER_DIE = register("game.player.die", "entity.player.death")
    val FIREWORKS_TWINKLE_FAR = register("fireworks.twinkle_far", "entity.firework.twinkle_far")
    val MOB_GUARDIAN_LAND_IDLE = register("mob.guardian.land.idle", "entity.guardian.ambient_land")
    val DIG_GRASS = register("dig.grass", "block.grass.break")
    val MOB_SKELETON_STEP = register("mob.skeleton.step", "entity.skeleton.step")
    val MOB_WITHER_DEATH = register("mob.wither.death", "entity.wither.death")
    val MOB_WOLF_HURT = register("mob.wolf.hurt", "entity.wolf.hurt")
    val MOB_HORSE_LEATHER = register("mob.horse.leather", "entity.horse.saddle")
    val MOB_BAT_LOOP = register("mob.bat.loop", "entity.bat.loop")
    val MOB_GHAST_SCREAM = register("mob.ghast.scream", "entity.ghast.hurt")
    val GAME_PLAYER_HURT = register("game.player.hurt", "entity.player.hurt")
    val GAME_NEUTRAL_DIE = register("game.neutral.die", "entity.player.death")
    val MOB_CREEPER_DEATH = register("mob.creeper.death", "entity.creeper.death")
    val MOB_HORSE_GALLOP = register("mob.horse.gallop", "entity.horse.gallop")
    val MOB_WITHER_SPAWN = register("mob.wither.spawn", "entity.wither.spawn")
    val MOB_ENDERMEN_HIT = register("mob.endermen.hit", "entity.endermen.hurt")
    val MOB_CREEPER_SAY = register("mob.creeper.say", "entity.creeper.hurt")
    val MOB_HORSE_WOOD = register("mob.horse.wood", "entity.horse.step_wood")
    val MOB_ZOMBIE_UNFECT = register("mob.zombie.unfect", "entity.zombie_villager.converted")
    val RANDOM_ANVIL_USE = register("random.anvil_use", "block.anvil.use")
    val RANDOM_CHESTCLOSED = register("random.chestclosed", "block.chest.close")
    val MOB_SHEEP_SHEAR = register("mob.sheep.shear", "entity.sheep.shear")
    val RANDOM_POP = register("random.pop", "entity.item.pickup")
    val MOB_BAT_DEATH = register("mob.bat.death", "entity.bat.death")
    val DIG_WOOD = register("dig.wood", "block.wood.break")
    val MOB_HORSE_DONKEY_DEATH = register("mob.horse.donkey.death", "entity.donkey.death")
    val FIREWORKS_BLAST = register("fireworks.blast", "entity.firework.blast")
    val MOB_ZOMBIEPIG_ZPIGANGRY = register("mob.zombiepig.zpigangry", "entity.zombie_pig.angry")
    val GAME_HOSTILE_SWIM = register("game.hostile.swim", "entity.player.swim")
    val MOB_GUARDIAN_FLOP = register("mob.guardian.flop", "entity.guardian.flop")
    val MOB_VILLAGER_YES = register("mob.villager.yes", "entity.villager.yes")
    val MOB_GHAST_CHARGE = register("mob.ghast.charge", "entity.ghast.warn")
    val CREEPER_PRIMED = register("creeper.primed", "entity.creeper.primed")
    val DIG_SAND = register("dig.sand", "block.sand.break")
    val MOB_CHICKEN_SAY = register("mob.chicken.say", "entity.chicken.ambient")
    val RANDOM_DOOR_CLOSE = register("random.door_close", "block.wooden_door.close")
    val MOB_GUARDIAN_ELDER_DEATH = register("mob.guardian.elder.death", "entity.elder_guardian.death")
    val FIREWORKS_TWINKLE = register("fireworks.twinkle", "entity.firework.twinkle")
    val MOB_HORSE_SKELETON_DEATH = register("mob.horse.skeleton.death", "entity.skeleton_horse.death")
    val AMBIENT_WEATHER_RAIN = register("ambient.weather.rain", "weather.rain")
    val PORTAL_TRIGGER = register("portal.trigger", "block.portal.trigger")
    val RANDOM_CHESTOPEN = register("random.chestopen", "block.chest.open")
    val MOB_HORSE_LAND = register("mob.horse.land", "entity.horse.land")
    val MOB_SILVERFISH_STEP = register("mob.silverfish.step", "entity.silverfish.step")
    val MOB_BAT_TAKEOFF = register("mob.bat.takeoff", "entity.bat.takeoff")
    val MOB_VILLAGER_NO = register("mob.villager.no", "entity.villager.no")
    val GAME_HOSTILE_HURT_FALL_BIG = register("game.hostile.hurt.fall.big", "entity.hostile.big_fall")
    val MOB_IRONGOLEM_WALK = register("mob.irongolem.walk", "entity.irongolem.step")
    val NOTE_HAT = register("note.hat", "block.note.hat")
    val MOB_ZOMBIE_METAL = register("mob.zombie.metal", "entity.zombie.attack_iron_door")
    val MOB_VILLAGER_HAGGLE = register("mob.villager.haggle", "entity.villager.trading")
    val MOB_GHAST_FIREBALL = register("mob.ghast.fireball", "entity.ghast.shoot")
    val MOB_IRONGOLEM_DEATH = register("mob.irongolem.death", "entity.irongolem.death")
    val RANDOM_BREAK = register("random.break", "entity.item.break")
    val MOB_ZOMBIE_REMEDY = register("mob.zombie.remedy", "entity.zombie_villager.cure")
    val RANDOM_BOW = register("random.bow", "entity.arrow.shoot")
    val MOB_VILLAGER_IDLE = register("mob.villager.idle", "entity.villager.ambient")
    val STEP_CLOTH = register("step.cloth", "block.cloth.step")
    val MOB_SILVERFISH_HIT = register("mob.silverfish.hit", "entity.silverfish.hurt")
    val LIQUID_LAVA = register("liquid.lava", "block.lava.ambient")
    val GAME_NEUTRAL_HURT_FALL_BIG = register("game.neutral.hurt.fall.big", "entity.hostile.big_fall")
    val FIRE_FIRE = register("fire.fire", "block.fire.ambient")
    val MOB_ZOMBIE_WOOD = register("mob.zombie.wood", "entity.zombie.attack_door_wood")
    val MOB_CHICKEN_STEP = register("mob.chicken.step", "entity.chicken.step")
    val MOB_GUARDIAN_LAND_HIT = register("mob.guardian.land.hit", "entity.guardian.hurt_land")
    val MOB_CHICKEN_PLOP = register("mob.chicken.plop", "entity.chicken.egg")
    val MOB_ENDERDRAGON_WINGS = register("mob.enderdragon.wings", "entity.ender_dragon.flap")
    val STEP_GRASS = register("step.grass", "block.grass.step")
    val MOB_HORSE_BREATHE = register("mob.horse.breathe", "entity.horse.breathe")
    val GAME_PLAYER_HURT_FALL_BIG = register("game.player.hurt.fall.big", "entity.hostile.big_fall")
    val MOB_HORSE_DONKEY_IDLE = register("mob.horse.donkey.idle", "entity.donkey.ambient")
    val MOB_SPIDER_STEP = register("mob.spider.step", "entity.spider.step")
    val GAME_NEUTRAL_HURT = register("game.neutral.hurt", "entity.player.death")
    val MOB_COW_SAY = register("mob.cow.say", "entity.cow.ambient")
    val MOB_HORSE_JUMP = register("mob.horse.jump", "entity.horse.jump")
    val MOB_HORSE_SOFT = register("mob.horse.soft", "entity.horse.step")
    val GAME_NEUTRAL_SWIM_SPLASH = register("game.neutral.swim.splash", "entity.generic.splash")
    val MOB_GUARDIAN_HIT = register("mob.guardian.hit", "entity.guardian.hurt")
    val MOB_ENDERDRAGON_END = register("mob.enderdragon.end", "entity.ender_dragon.death")
    val MOB_ZOMBIE_STEP = register("mob.zombie.step", "entity.zombie.step")
    val MOB_ENDERDRAGON_GROWL = register("mob.enderdragon.growl", "entity.ender_dragon.growl")
    val MOB_WOLF_SHAKE = register("mob.wolf.shake", "entity.wolf.shake")
    val MOB_ENDERMEN_DEATH = register("mob.endermen.death", "entity.endermen.death")
    val RANDOM_ANVIL_LAND = register("random.anvil_land", "block.anvil.land")
    val GAME_HOSTILE_HURT = register("game.hostile.hurt", "entity.player.death")
    val MINECART_INSIDE = register("minecart.inside", "entity.minecart.inside")
    val MOB_SLIME_BIG = register("mob.slime.big", "entity.slime.squish")
    val LIQUID_WATER = register("liquid.water", "block.water.ambient")
    val MOB_PIG_SAY = register("mob.pig.say", "entity.pig.ambient")
    val MOB_WITHER_SHOOT = register("mob.wither.shoot", "entity.wither.shoot")
//    val ITEM_FIRECHARGE_USE = register("item.fireCharge.use", "entity.blaze.shoot") // legacy key does not follow the allowed pattern
    val STEP_SAND = register("step.sand", "block.sand.step")
    val MOB_IRONGOLEM_HIT = register("mob.irongolem.hit", "entity.irongolem.hurt")
    val MOB_HORSE_DEATH = register("mob.horse.death", "entity.horse.death")
    val MOB_BAT_HURT = register("mob.bat.hurt", "entity.bat.hurt")
    val MOB_GHAST_AFFECTIONATE_SCREAM = register("mob.ghast.affectionate_scream", "entity.ghast.scream")
    val MOB_GUARDIAN_ELDER_IDLE = register("mob.guardian.elder.idle", "entity.elder_guardian.ambient")
    val MOB_ZOMBIEPIG_ZPIGDEATH = register("mob.zombiepig.zpigdeath", "entity.zombie_pig.death")
    val AMBIENT_WEATHER_THUNDER = register("ambient.weather.thunder", "entity.lightning.thunder")
    val MINECART_BASE = register("minecart.base", "entity.minecart.riding")
    val STEP_LADDER = register("step.ladder", "block.ladder.step")
    val MOB_HORSE_DONKEY_ANGRY = register("mob.horse.donkey.angry", "entity.donkey.angry")
    val AMBIENT_CAVE_CAVE = register("ambient.cave.cave", "ambient.cave")
    val FIREWORKS_BLAST_FAR = register("fireworks.blast_far", "entity.firework.blast_far")
    val GAME_NEUTRAL_HURT_FALL_SMALL = register("game.neutral.hurt.fall.small", "entity.generic.small_fall")
    val GAME_HOSTILE_SWIM_SPLASH = register("game.hostile.swim.splash", "entity.generic.splash")
    val RANDOM_DRINK = register("random.drink", "entity.generic.drink")
    val GAME_HOSTILE_DIE = register("game.hostile.die", "entity.player.death")
    val MOB_CAT_HISS = register("mob.cat.hiss", "entity.cat.hiss")
    val NOTE_BD = register("note.bd", "block.note.basedrum")
    val MOB_SPIDER_SAY = register("mob.spider.say", "entity.spider.ambient")
    val STEP_STONE = register("step.stone", "block.stone.step")
    val RANDOM_LEVELUP = register("random.levelup", "entity.player.levelup")
    val LIQUID_LAVAPOP = register("liquid.lavapop", "block.lava.pop")
    val MOB_SHEEP_SAY = register("mob.sheep.say", "entity.sheep.ambient")
    val MOB_SKELETON_SAY = register("mob.skeleton.say", "entity.skeleton.ambient")
    val MOB_BLAZE_BREATHE = register("mob.blaze.breathe", "entity.blaze.ambient")
    val MOB_BAT_IDLE = register("mob.bat.idle", "entity.bat.ambient")
    val MOB_MAGMACUBE_BIG = register("mob.magmacube.big", "entity.magmacube.squish")
    val MOB_HORSE_IDLE = register("mob.horse.idle", "entity.horse.ambient")
    val GAME_HOSTILE_HURT_FALL_SMALL = register("game.hostile.hurt.fall.small", "entity.generic.small_fall")
    val MOB_HORSE_ZOMBIE_HIT = register("mob.horse.zombie.hit", "entity.zombie_horse.hurt")
    val MOB_IRONGOLEM_THROW = register("mob.irongolem.throw", "entity.irongolem.attack")
    val DIG_CLOTH = register("dig.cloth", "block.cloth.break")
    val STEP_GRAVEL = register("step.gravel", "block.gravel.step")
    val MOB_SILVERFISH_SAY = register("mob.silverfish.say", "entity.silverfish.ambient")
    val MOB_CAT_PURR = register("mob.cat.purr", "entity.cat.purr")
    val MOB_ZOMBIE_INFECT = register("mob.zombie.infect", "entity.zombie.infect")
    val RANDOM_EAT = register("random.eat", "entity.generic.eat")
    val MOB_WOLF_BARK = register("mob.wolf.bark", "entity.wolf.ambient")
    val GAME_TNT_PRIMED = register("game.tnt.primed", "entity.creeper.primed")
    val MOB_SHEEP_STEP = register("mob.sheep.step", "entity.sheep.step")
    val MOB_ZOMBIE_DEATH = register("mob.zombie.death", "entity.zombie.death")
    val RANDOM_DOOR_OPEN = register("random.door_open", "block.wooden_door.open")
    val MOB_ENDERMEN_PORTAL = register("mob.endermen.portal", "entity.endermen.teleport")
    val MOB_HORSE_ANGRY = register("mob.horse.angry", "entity.horse.angry")
    val MOB_WOLF_GROWL = register("mob.wolf.growl", "entity.wolf.growl")
    val DIG_SNOW = register("dig.snow", "block.snow.break")
    val TILE_PISTON_OUT = register("tile.piston.out", "block.piston.extend")
    val RANDOM_BURP = register("random.burp", "entity.player.burp")
    val MOB_COW_STEP = register("mob.cow.step", "entity.cow.step")
    val MOB_WITHER_HURT = register("mob.wither.hurt", "entity.wither.hurt")
    val MOB_GUARDIAN_LAND_DEATH = register("mob.guardian.land.death", "entity.elder_guardian.death_land")
    val MOB_CHICKEN_HURT = register("mob.chicken.hurt", "entity.chicken.hurt")
    val MOB_WOLF_STEP = register("mob.wolf.step", "entity.wolf.step")
    val MOB_WOLF_DEATH = register("mob.wolf.death", "entity.wolf.death")
    val MOB_WOLF_WHINE = register("mob.wolf.whine", "entity.wolf.whine")
    val NOTE_PLING = register("note.pling", "block.note.pling")
    val GAME_PLAYER_HURT_FALL_SMALL = register("game.player.hurt.fall.small", "entity.generic.small_fall")
    val MOB_CAT_PURREOW = register("mob.cat.purreow", "entity.cat.purreow")
//    val FIREWORKS_LARGEBLAST_FAR = register("fireworks.largeBlast_far", "entity.firework.large_blast_far") // legacy key does not follow the allowed pattern
    val MOB_SKELETON_HURT = register("mob.skeleton.hurt", "entity.skeleton.hurt")
    val MOB_SPIDER_DEATH = register("mob.spider.death", "entity.spider.death")
    val RANDOM_ANVIL_BREAK = register("random.anvil_break", "block.anvil.destroy")
    val MOB_WITHER_IDLE = register("mob.wither.idle", "entity.wither.ambient")
    val MOB_GUARDIAN_ELDER_HIT = register("mob.guardian.elder.hit", "entity.elder_guardian.hurt")
    val MOB_ENDERMEN_SCREAM = register("mob.endermen.scream", "entity.endermen.scream")
    val MOB_CAT_HITT = register("mob.cat.hitt", "entity.cat.hurt")
    val MOB_MAGMACUBE_SMALL = register("mob.magmacube.small", "entity.small_magmacube.squish")
    val FIRE_IGNITE = register("fire.ignite", "item.flintandsteel.use")
    val MOB_ENDERDRAGON_HIT = register("mob.enderdragon.hit", "entity.ender_dragon.hurt")
    val MOB_ZOMBIE_HURT = register("mob.zombie.hurt", "entity.zombie.hurt")
    val RANDOM_EXPLODE = register("random.explode", "entity.generic.explode")
    val MOB_SLIME_ATTACK = register("mob.slime.attack", "entity.slime.attack")
    val MOB_MAGMACUBE_JUMP = register("mob.magmacube.jump", "entity.magmacube.jump")
    val RANDOM_SPLASH = register("random.splash", "entity.bobber.splash")
    val MOB_HORSE_SKELETON_HIT = register("mob.horse.skeleton.hit", "entity.skeleton_horse.hurt")
    val MOB_GHAST_MOAN = register("mob.ghast.moan", "entity.ghast.ambient")
    val MOB_GUARDIAN_CURSE = register("mob.guardian.curse", "entity.elder_guardian.curse")
    val GAME_POTION_SMASH = register("game.potion.smash", "block.glass.break")
    val NOTE_BASSATTACK = register("note.bassattack", "block.note.bass")
    val GUI_BUTTON_PRESS = register("gui.button.press", "ui.button.click")
    val RANDOM_ORB = register("random.orb", "entity.experience_orb.pickup")
    val MOB_ZOMBIE_WOODBREAK = register("mob.zombie.woodbreak", "entity.zombie.break_door_wood")
    val MOB_HORSE_ARMOR = register("mob.horse.armor", "entity.horse.armor")
    val TILE_PISTON_IN = register("tile.piston.in", "block.piston.contract")
    val MOB_CAT_MEOW = register("mob.cat.meow", "entity.cat.ambient")
    val MOB_PIG_STEP = register("mob.pig.step", "entity.pig.step")
    val STEP_WOOD = register("step.wood", "block.wood.step")
    val PORTAL_TRAVEL = register("portal.travel", "block.portal.travel")
    val MOB_GUARDIAN_DEATH = register("mob.guardian.death", "entity.guardian.death")
    val MOB_SKELETON_DEATH = register("mob.skeleton.death", "entity.skeleton.death")
    val MOB_HORSE_HIT = register("mob.horse.hit", "entity.horse.hurt")
    val MOB_VILLAGER_HIT = register("mob.villager.hit", "entity.villager.hurt")
    val MOB_HORSE_SKELETON_IDLE = register("mob.horse.skeleton.idle", "entity.skeleton_horse.ambient")
    val RECORDS_CHIRP = register("records.chirp", "record.chirp")
    val MOB_RABBIT_HURT = register("mob.rabbit.hurt", "entity.rabbit.hurt")
    val RECORDS_STAL = register("records.stal", "record.stal")
    val MUSIC_GAME_NETHER = register("music.game.nether", "music.nether")
    val MUSIC_MENU = register("music.menu", "music.menu")
    val RECORDS_MELLOHI = register("records.mellohi", "record.mellohi")
    val RECORDS_CAT = register("records.cat", "record.cat")
    val RECORDS_FAR = register("records.far", "record.far")
    val MUSIC_GAME_END_DRAGON = register("music.game.end.dragon", "music.dragon")
    val MOB_RABBIT_DEATH = register("mob.rabbit.death", "entity.rabbit.death")
    val MOB_RABBIT_IDLE = register("mob.rabbit.idle", "entity.rabbit.ambient")
    val MUSIC_GAME_END = register("music.game.end", "music.end")
    val MUSIC_GAME = register("music.game", "music.game")
    val MOB_GUARDIAN_IDLE = register("mob.guardian.idle", "entity.elder_guardian.ambient")
    val RECORDS_WARD = register("records.ward", "record.ward")
    val RECORDS_13 = register("records.13", "record.13")
    val MOB_RABBIT_HOP = register("mob.rabbit.hop", "entity.rabbit.jump")
    val RECORDS_STRAD = register("records.strad", "record.strad")
    val RECORDS_11 = register("records.11", "record.11")
    val RECORDS_MALL = register("records.mall", "record.mall")
    val RECORDS_BLOCKS = register("records.blocks", "record.blocks")
    val RECORDS_WAIT = register("records.wait", "record.wait")
    val MUSIC_GAME_END_CREDITS = register("music.game.end.credits", "music.credits")
    val MUSIC_GAME_CREATIVE = register("music.game.creative", "music.creative")
    //</editor-fold>

    fun resolve(legacyKey: String, modernKey: String) =
        key(if (IS_LEGACY) legacyKey else modernKey)
    
    private fun register(legacyKey: String, modernKey: String): Key {
        mappings += (modernKey to legacyKey)
        return resolve(legacyKey, modernKey)
    }

    fun resolve(modernKey: String) =
        resolve(mappings[modernKey] ?: modernKey, modernKey)

}