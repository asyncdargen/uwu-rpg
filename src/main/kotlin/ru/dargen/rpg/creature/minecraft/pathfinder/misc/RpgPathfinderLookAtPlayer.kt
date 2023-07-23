package ru.dargen.rpg.creature.minecraft.pathfinder.misc

import net.minecraft.server.v1_12_R1.EntityPlayer
import ru.dargen.rpg.creature.minecraft.pathfinder.RpgPathfinderBase
import ru.dargen.rpg.entity.minecraftEntity
import ru.dargen.rpg.util.distanceTo
import ru.dargen.rpg.util.getNearPlayers
import ru.dargen.rpg.util.random.percentRandomSuccess
import ru.dargen.rpg.util.random.randomInt
import ru.starfarm.core.util.cast

class RpgPathfinderLookAtPlayer : RpgPathfinderBase(0) {

    private var target: EntityPlayer? = null
    private var outTicks: Int = 0

    override fun shouldExecution(): Boolean {
        if (!.09.percentRandomSuccess) return false

        target = if (handle.goalTarget is EntityPlayer) handle.goalTarget!!.cast()
        else bukkitLocation.getNearPlayers(7).randomOrNull()?.minecraftEntity?.cast()

        return target != null
    }

    override fun shouldContinue() =
        target != null && target!!.bukkitEntity.location.distanceTo(bukkitLocation) < 8 && outTicks > 0

    override fun execute() {
        outTicks = 40 + randomInt(35)
    }

    override fun navigate() {
        outTicks--
        lookController.a(target!!.locX, target!!.locY + target!!.headHeight, target!!.locZ, handle.O().toFloat(), handle.N().toFloat())
    }

}