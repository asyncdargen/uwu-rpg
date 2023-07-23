package ru.dargen.rpg.creature.minecraft.pathfinder.move

import org.bukkit.Location
import ru.dargen.rpg.creature.minecraft.pathfinder.RpgPathfinderBase
import ru.dargen.rpg.util.distanceTo

class RpgPathfinderBack(private val spawn: Location, private val distance: Double) : RpgPathfinderBase(-1) {

    override fun shouldExecution() = bukkitLocation.distanceTo(spawn) >= distance

    override fun shouldContinue() = false

    override fun execute() {
        asBukkit.teleport(spawn)
        asRpg.wipe()
    }

}