package ru.dargen.rpg.creature.minecraft.pathfinder.move

import ru.dargen.rpg.creature.minecraft.pathfinder.RpgPathfinderBase

class RpgPathfinderFloat : RpgPathfinderBase(-1) {

    private val inWater get() = handle.isInWater || handle.au()

    override fun shouldExecution() = inWater

    override fun shouldContinue() = inWater

    override fun navigate() {
        jumpController.a()
    }

}