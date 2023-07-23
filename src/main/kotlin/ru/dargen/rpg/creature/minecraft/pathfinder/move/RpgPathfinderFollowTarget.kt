package ru.dargen.rpg.creature.minecraft.pathfinder.move

import ru.dargen.rpg.creature.minecraft.clearPath
import ru.dargen.rpg.creature.minecraft.pathfinder.RpgPathfinderBase
import ru.dargen.rpg.entity.combat.CombatTimeout
import ru.dargen.rpg.entity.isTargetable
import ru.dargen.rpg.util.distanceTo
import java.time.Duration
import java.time.Instant

val CombatSpeedUpTimeout = CombatTimeout.minusMillis((CombatTimeout.toMillis() * .35).toLong())!!

class RpgPathfinderFollowTarget(private val distance: Double) : RpgPathfinderBase(2) {

    override fun shouldExecution() =
        handle.goalTarget.isTargetable && (handle.goalTarget!!.bukkitEntity.location.distanceTo(bukkitLocation) > distance * .85 || !handle.entitySenses.a(handle.goalTarget))

    override fun shouldContinue() = shouldExecution()

    override fun reset() = navigation.clearPath()

    override fun navigate() {
        if (shouldContinue()) navigation.a(
            handle.goalTarget,
            (if (Duration.between(asRpg.combatState.lastAttackTimestamp, Instant.now()) >= CombatSpeedUpTimeout) 1.25 else 1.0)
        )
    }

}