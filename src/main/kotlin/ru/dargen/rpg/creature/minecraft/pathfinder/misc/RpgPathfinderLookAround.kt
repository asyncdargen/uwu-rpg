package ru.dargen.rpg.creature.minecraft.pathfinder.misc

import ru.dargen.rpg.creature.minecraft.pathfinder.RpgPathfinderBase
import ru.dargen.rpg.util.random.percentRandomSuccess
import ru.dargen.rpg.util.random.randomDouble
import ru.dargen.rpg.util.random.randomInt
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class RpgPathfinderLookAround : RpgPathfinderBase(0) {

    private var x: Double = .0
    private var z: Double = .0
    private var outTicks: Int = 0

    override fun shouldExecution() = .04.percentRandomSuccess

    override fun shouldContinue() = outTicks > 0

    override fun execute() {
        val look = PI * 2 * randomDouble()
        x = cos(look)
        z = sin(look)
        outTicks = 20 + randomInt(20)
    }

    override fun navigate() {
        outTicks--
        lookController.a(handle.locX + x, handle.locY + handle.headHeight, handle.locZ + z, handle.O().toFloat(), handle.N().toFloat())
    }

}