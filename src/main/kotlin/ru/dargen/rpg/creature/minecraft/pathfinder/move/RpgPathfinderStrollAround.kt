package ru.dargen.rpg.creature.minecraft.pathfinder.move

import net.minecraft.server.v1_12_R1.BlockPosition
import net.minecraft.server.v1_12_R1.Material
import org.bukkit.Location
import org.bukkit.Particle
import ru.dargen.rpg.creature.minecraft.clearPath
import ru.dargen.rpg.creature.minecraft.move
import ru.dargen.rpg.creature.minecraft.noPath
import ru.dargen.rpg.creature.minecraft.pathfinder.RpgPathfinderBase
import ru.dargen.rpg.entity.isTargetable
import ru.dargen.rpg.util.distanceTo
import ru.dargen.rpg.util.random.percentRandomSuccess
import ru.dargen.rpg.util.random.randomInt

class RpgPathfinderStrollAround(
    private val location: Location,
    private val distance: Double,
    private val water: Boolean = false
) : RpgPathfinderBase(1) {

    var retries = 0
    private var targetLocation: Location? = null

    override fun shouldExecution(): Boolean {
        if (handle.goalTarget.isTargetable) return false

        val force = bukkitLocation.distanceTo(location) > distance
        if (!force && !.07.percentRandomSuccess) return false

        targetLocation = if (distance <= 0) location else generateRandomLocation() ?: location.takeIf { force }
        targetLocation?.world?.spawnParticle(Particle.BARRIER, targetLocation!!, 1)
        return targetLocation != null
    }

    override fun shouldContinue() = !handle.goalTarget.isTargetable && !navigation.noPath()

    override fun reset() = navigation.clearPath()

    override fun navigate() {
        if (targetLocation == null) return
        if (!navigation.move(targetLocation!!.x, targetLocation!!.y, targetLocation!!.z, .8)) {
            if (retries >= 5) {
                asBukkit.teleport(location)
                retries = 0
            } else retries++
        } else retries = 0
    }

    fun generateRandomLocation(): Location? {
        for(i in 0..9) {
            val offX = randomInt((distance * 2 + 1).toInt()) - distance
            val offY = randomInt((distance * 2 + 1).toInt()) - distance
            val offZ = randomInt((distance * 2 + 1).toInt()) - distance
            val position = BlockPosition(location.x + offX, location.y + offY, location.z + offZ)
            if (!water && handle.world.getType(position).material == Material.WATER) continue
            return Location(handle.world.world, position.x.toDouble(), position.y.toDouble(), position.z.toDouble())
        }

        return null
    }

}