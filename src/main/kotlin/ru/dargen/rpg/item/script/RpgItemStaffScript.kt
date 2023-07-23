package ru.dargen.rpg.item.script

import com.comphenix.protocol.wrappers.BlockPosition
import com.comphenix.protocol.wrappers.WrappedBlockData
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk
import org.bukkit.entity.LivingEntity
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector
import ru.dargen.rpg.entity.asRpg
import ru.dargen.rpg.item.RpgItem
import ru.dargen.rpg.player.RpgPlayer
import ru.dargen.rpg.util.getNearEntities
import ru.dargen.rpg.util.rpg.getMetaValue
import ru.starfarm.core.protocol.block.v1_16_R3PacketMultiBlockChange
import ru.starfarm.core.util.cast


object RpgItemStaffScript : RpgItemScript("staff") {

    override fun interact(item: RpgItem, player: RpgPlayer, event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            val initLocation = player.handle.eyeLocation.apply {
                add(player.location.direction.normalize().divide(Vector(2.0, 1.0, 2.0)))
            }

            val airBlockData = WrappedBlockData.createData(Material.AIR)
            listOf<Block>()
                .groupBy { BlockPosition(it.x shr 4, it.y shr 4, it.z shr 4) }
                .forEach { (chunk, blocks) ->
                    v1_16_R3PacketMultiBlockChange().apply {
                        flag = true
                        sectionPosition = chunk
                        blocksData = blocks.map { Vector(it.x, it.y, it.z) to airBlockData }.toTypedArray()
                    }.send(player)
                }
            throwParticleLine(
                initLocation,
                initLocation.clone().apply {
                    add(initLocation.direction.normalize().multiply(item.getMetaValue("distance", String::toInt)!!))
                }, item.getMetaValue("particles") {
                    split(",").map(String::trim).filter(String::isNotBlank).map(Particle::valueOf)
                }!!
            ) {
                it.asRpg?.damage(player, item)
            }
        }
    }

    fun throwParticleLine(
        start: Location,
        end: Location,
        particles: List<Particle>,
        step: Double = .2,
        entityHandler: (LivingEntity) -> Unit
    ) {
        val distance = start.distance(end)
        var i = 0.0
        while (i <= distance) {
            val x = start.x + (end.x - start.x) * (i / distance)
            val y = start.y + (end.y - start.y) * (i / distance)
            val z = start.z + (end.z - start.z) * (i / distance)
            val location = Location(start.world, x, y, z)

            i += step

            particles.forEach { location.world.spawnParticle(it, location, 1, .0, .0, .0, .0) }

            if (location.getNearEntities(.05).firstOrNull()?.let { entityHandler(it) } != null) return
        }
    }

}