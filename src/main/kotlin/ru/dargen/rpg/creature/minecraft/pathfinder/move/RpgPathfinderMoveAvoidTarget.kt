package ru.dargen.rpg.creature.minecraft.pathfinder.move

import net.minecraft.server.v1_12_R1.PathEntity
import net.minecraft.server.v1_12_R1.RandomPositionGenerator
import net.minecraft.server.v1_12_R1.Vec3D
import ru.dargen.rpg.creature.minecraft.clearPath
import ru.dargen.rpg.creature.minecraft.noPath
import ru.dargen.rpg.creature.minecraft.pathfinder.RpgPathfinderBase
import ru.dargen.rpg.entity.isTargetable
import ru.dargen.rpg.util.distanceTo
import ru.starfarm.core.util.cast

class RpgPathfinderMoveAvoidTarget(private val distance: Double) : RpgPathfinderBase(-1) {

    private var path: PathEntity? = null

    override fun shouldExecution(): Boolean {
        if (!handle.goalTarget.isTargetable)
            return false

        val entity = handle.goalTarget!!
        if (bukkitLocation.distanceTo(entity.bukkitEntity.location) <= distance * .48) {
            val position = RandomPositionGenerator.b(handle.cast(), 2, 1, Vec3D(entity.locX, entity.locY, entity.locZ))

            if (position == null || handle.d(position.x, position.y, position.z) < entity.h(handle)) return false
            else path = navigation.a(position.x, position.y, position.z)

            return path != null
        } else return false
    }

    override fun shouldContinue() = handle.goalTarget.isTargetable && !navigation.noPath()

    override fun execute() {
        navigation.a(path, 1.0)
    }

    override fun reset() = navigation.clearPath()

}