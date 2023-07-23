package ru.dargen.rpg.creature.minecraft.pathfinder.move

import ru.dargen.rpg.creature.minecraft.clearPath
import ru.dargen.rpg.creature.minecraft.pathfinder.RpgPathfinderBase
import ru.dargen.rpg.entity.RpgEntity
import ru.dargen.rpg.util.distanceTo

class RpgPathfinderFollowOwner(private val owner: RpgEntity<*>, private val distance: Double) : RpgPathfinderBase(1) {

    override fun shouldExecution() = owner.location.distanceTo(bukkitLocation) > distance

    override fun shouldContinue() = shouldExecution()

    override fun reset() = navigation.clearPath()

    override fun navigate() {
        if (!shouldContinue() || !owner.handle.isOnGround) return
        if (owner.location.distanceTo(bukkitLocation) > 30 || !navigation.a(owner.entity, 1.0))
            asBukkit.teleport(owner.handle)
    }

}