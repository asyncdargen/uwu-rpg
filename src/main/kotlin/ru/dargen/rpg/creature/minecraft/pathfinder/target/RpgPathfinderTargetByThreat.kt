package ru.dargen.rpg.creature.minecraft.pathfinder.target

import net.minecraft.server.v1_12_R1.EntityLiving
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityTargetEvent
import ru.dargen.rpg.creature.minecraft.pathfinder.RpgPathfinderBase
import ru.dargen.rpg.entity.asRpg
import ru.dargen.rpg.entity.isTargetable
import ru.dargen.rpg.util.distanceTo
import ru.dargen.rpg.util.getNearEntities

class RpgPathfinderTargetByThreat(
    private val distance: Double,
    private val agressiveTargetTypes: List<EntityType>
) : RpgPathfinderBase(-1) {

    private var outTicks = 5
    private var target: EntityLiving? = null

    override fun shouldExecution(): Boolean {
        val force = !handle.goalTarget.isValidTarget || !handle.goalTarget!!.isInArea

        if (force || outTicks-- <= 0) {
            outTicks = 5
            val concurrentPoints = (target?.bukkitEntity?.asRpg?.let { asRpg.threatEntities.getIfPresent(it) } ?: 0) * 1.25

            target = asRpg.threatEntities.asMap().entries
                .filter { (entity, _) -> entity.entity.isValidTarget && entity.entity.isInArea }
                .maxByOrNull { (_, points) -> points }
                ?.takeIf { (_, points) -> force || asRpg.threatEntities.size() == 1L || points >= concurrentPoints }
                ?.key
                ?.entity

            if (target != null) return true
            if (!force) return false

            if (agressiveTargetTypes.isNotEmpty()) target = bukkitLocation.getNearEntities(distance)
                .mapNotNull(LivingEntity::asRpg)
                .firstOrNull {
                    it.entity.isValidTarget
                            && it.location.distanceTo(bukkitLocation) <= distance * .65
                            && it.entityType in agressiveTargetTypes
                            && it.entity !== handle
                            && handle.entitySenses.a(it.entity)
                            && this@RpgPathfinderTargetByThreat.navigation.a(it.entity) != null
                }
                ?.also { asRpg.combatState.lastAttackedEntity = it }
                ?.entity

            return true
        } else return false
    }

    override fun shouldContinue() = false

    override fun execute() {
        handle.setGoalTarget(target, EntityTargetEvent.TargetReason.RANDOM_TARGET, true)
    }

    private val EntityLiving?.isValidTarget
        get() = isTargetable

    private val EntityLiving.isInArea get() = bukkitEntity.location.distanceTo(bukkitLocation) <= distance

}