package ru.dargen.rpg.creature.minecraft.pathfinder.move.prototype

import ru.dargen.rpg.creature.minecraft.noPath
import ru.dargen.rpg.creature.minecraft.pathfinder.RpgPathfinderBase

class RpgPathfinderSlimeJump : RpgPathfinderBase(-1) {

    private var outTicks = 0

    override fun shouldExecution() = !navigation.noPath()

    override fun shouldContinue() = shouldExecution()

    override fun navigate() {
        if (handle.onGround && outTicks-- <= 0) {
            jumpController.a()
            outTicks = 2
        }
    }

}