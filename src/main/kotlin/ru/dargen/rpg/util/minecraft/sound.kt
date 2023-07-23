package ru.dargen.rpg.util.minecraft

import net.minecraft.server.v1_12_R1.SoundEffect
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import java.util.concurrent.ConcurrentHashMap

val SoundPool: MutableMap<String, Sound?> = ConcurrentHashMap()

fun EntityType.soundOf(type: String) = SoundPool.computeIfAbsent("${type}_$name") {
    runCatching {
        when {
            this == EntityType.PLAYER && type == "STEP" -> Sound.BLOCK_GLASS_STEP
            this == EntityType.IRON_GOLEM -> Sound.valueOf("ENTITY_IRONGOLEM_$type")
            this == EntityType.OCELOT -> Sound.valueOf("ENTITY_CAT_$type")
            this == EntityType.CAVE_SPIDER -> Sound.valueOf("ENTITY_SPIDER_$type")
            else -> enumValueOf<Sound>("ENTITY_${this}_$type")
        }
    }.getOrNull()
}


val Sound?.asSoundEffect get() = this?.ordinal?.let(SoundEffect.a::getId)