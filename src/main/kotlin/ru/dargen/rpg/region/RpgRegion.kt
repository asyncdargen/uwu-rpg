package ru.dargen.rpg.region

import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import ru.dargen.rpg.entity.RpgEntity
import ru.dargen.rpg.region.script.RpgRegionScript
import ru.dargen.rpg.region.types.RpgRegionFlag
import ru.dargen.rpg.util.rpg.RpgMetaObject
import ru.starfarm.core.util.math.Cuboid

data class RpgRegion(
    val id: Short, val name: String, val levels: IntRange,
    val cuboid: Cuboid, val flags: Set<RpgRegionFlag>, val grave: Location?,
    override val metadata: MutableMap<String, Any?>, val scripts: Set<RpgRegionScript>,
) : RpgMetaObject {

    val minLevel by lazy(levels::min)
    val maxLevel by lazy(levels::max)

    operator fun contains(location: Location) = cuboid.contains(location)

    operator fun contains(entity: Entity) = cuboid.contains(entity)

    operator fun contains(block: Block) = cuboid.contains(block)

    operator fun contains(entity: RpgEntity<*>) = cuboid.contains(entity.handle)

    operator fun contains(flag: RpgRegionFlag) = flags.contains(flag)

}
