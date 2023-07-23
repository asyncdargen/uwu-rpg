package ru.dargen.rpg.creature.minecraft.pathfinder.attack

import net.minecraft.server.v1_12_R1.EntityLiving
import ru.dargen.rpg.creature.minecraft.pathfinder.RpgPathfinderBase
import ru.dargen.rpg.entity.isTargetable
import ru.dargen.rpg.util.distanceTo

abstract class RpgPathfinderAttackBase(protected val distance: Double, protected val speed: Int, priority: Int = 2) : RpgPathfinderBase(priority) {

    private var outTicks = 0

    override fun shouldExecution() =
        handle.goalTarget.isTargetable

    override fun shouldContinue() = shouldExecution()

    override fun navigate() {
        val entity = handle.goalTarget ?: return
        lookController.a(entity, 20f, 20f)
        if (outTicks-- <= 0 && isInArea(handle.goalTarget!!)) {
            outTicks = speed
            damage(entity)
        }
    }

    abstract fun damage(entityLiving: EntityLiving)

    protected fun isInArea(entityLiving: EntityLiving) = entityLiving.bukkitEntity.location.distanceTo(bukkitLocation) <= distance

}