package ru.dargen.rpg.creature.preset

import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld
import org.bukkit.entity.*
import org.bukkit.event.entity.CreatureSpawnEvent
import ru.dargen.rpg.creature.RpgCreature
import ru.dargen.rpg.creature.disguise.RpgDisguiseData
import ru.dargen.rpg.creature.minecraft.bridge.RpgEntityBridge
import ru.dargen.rpg.creature.minecraft.pathfinder.attack.RpgPathfinderBowAttack
import ru.dargen.rpg.creature.minecraft.pathfinder.attack.RpgPathfinderHandAttack
import ru.dargen.rpg.creature.minecraft.pathfinder.misc.RpgPathfinderLookAround
import ru.dargen.rpg.creature.minecraft.pathfinder.misc.RpgPathfinderLookAtPlayer
import ru.dargen.rpg.creature.minecraft.pathfinder.move.*
import ru.dargen.rpg.creature.minecraft.pathfinder.move.prototype.RpgPathfinderSlimeJump
import ru.dargen.rpg.creature.minecraft.pathfinder.target.RpgPathfinderTargetByThreat
import ru.dargen.rpg.creature.scripts.RpgCreatureScript
import ru.dargen.rpg.creature.types.RpgCreatureStatistic
import ru.dargen.rpg.creature.types.RpgCreatureType
import ru.dargen.rpg.entity.RpgCreatureEquipment
import ru.dargen.rpg.entity.RpgEntity
import ru.dargen.rpg.entity.RpgEntityRegistry
import ru.dargen.rpg.util.asItemStack
import ru.dargen.rpg.util.random.RandomList
import ru.dargen.rpg.util.rpg.RpgMetaObject
import ru.dargen.rpg.util.rpg.getMetaValue
import ru.starfarm.core.util.cast

data class RpgCreaturePreset(
    val id: Short, val name: String, val disguise: String?,
    val entityType: EntityType, val type: RpgCreatureType,
    val statistics: Map<RpgCreatureStatistic, Any>,
    val drops: RandomList<RpgCreatureDrop>,
    val targets: List<EntityType>,
    val scripts: Set<RpgCreatureScript>,
    override val metadata: MutableMap<String, Any?>
) : RpgMetaObject {

    val newStatistics
        get() = buildMap { statistics.forEach { (statistic, value) -> statistic.mapper(this, value) } }
            .toMutableMap()

    fun newEntity(spawn: Location, caster: RpgEntity<*>? = null): RpgCreature {
        val entity = RpgEntityBridge.new(entityType, spawn)

        entity.applyLogic(spawn, caster)

        val creature = RpgCreature(this, entity.asBukkit)
        if (type == RpgCreatureType.SUPPORT) {
            caster!!.supports.add(creature)
            creature.owner = caster
        }

        if (disguise != null)
            creature.disguise = RpgDisguiseData.create(entity, disguise)

        if (RpgCreatureStatistic.SMALL.get())
            creature.handle.toBaby()

        creature.handle.prepare()
        creature.handle.prepareBukkit()

        RpgEntityRegistry.addRpgEntity(creature)
        spawn.world.cast<CraftWorld>().handle.addEntity(creature.entity, CreatureSpawnEvent.SpawnReason.CUSTOM)

        return creature
    }

    private fun RpgEntityBridge.applyLogic(spawn: Location, caster: RpgEntity<*>?) {
        val water = entityType == EntityType.GUARDIAN || entityType == EntityType.ELDER_GUARDIAN

        if (!water)
            addGoal(RpgPathfinderFloat())
        if (entityType == EntityType.SLIME)
            addGoal(RpgPathfinderSlimeJump())

        addGoal(RpgPathfinderLookAtPlayer())
        addGoal(RpgPathfinderLookAround())
        addGoal(RpgPathfinderTargetByThreat(RpgCreatureStatistic.DETECT_DISTANCE.get(), targets))
//        entity.addGoal(RpgPathfinderMoveAvoidTarget(RpgCreatureStatistic.ATTACK_DISTANCE.get()))

        if (type == RpgCreatureType.SUPPORT)
            addGoal(RpgPathfinderFollowOwner(caster!!, RpgCreatureStatistic.FREE_DISTANCE.get()))
        else {
            addGoal(RpgPathfinderBack(spawn, RpgCreatureStatistic.STRICT_DISTANCE.get()))
            addGoal(RpgPathfinderStrollAround(spawn, RpgCreatureStatistic.FREE_DISTANCE.get(), water))
        }

        addGoal(RpgPathfinderFollowTarget(RpgCreatureStatistic.ATTACK_DISTANCE.get()))

        addGoal(RpgPathfinderBowAttack(RpgCreatureStatistic.ATTACK_DISTANCE.get(), RpgCreatureStatistic.ATTACK_SPEED.get()))
        addGoal(RpgPathfinderHandAttack(RpgCreatureStatistic.ATTACK_DISTANCE.get(), RpgCreatureStatistic.ATTACK_SPEED.get()))
    }

    private fun LivingEntity.prepareBukkit() {
        getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).baseValue = .2 * RpgCreatureStatistic.SPEED.get<Double>()

        isCustomNameVisible = false
        removeWhenFarAway = false
        canPickupItems = false

        equipment.itemInMainHandDropChance = .0f
        equipment.itemInOffHandDropChance = .0f
        equipment.helmetDropChance = .0f
        equipment.chestplateDropChance = .0f
        equipment.leggingsDropChance = .0f
        equipment.bootsDropChance = .0f

        RpgCreatureEquipment.values().forEach {
            getMetaValue("equip_${it.name}", String::asItemStack)
                ?.run {it.applier(equipment, this) }
        }
    }

    private fun LivingEntity.toBaby() {
        when (this) {
            is Ageable -> setBaby()
            is Zombie -> isBaby = true
        }
    }

    private fun LivingEntity.prepare() {
        when (this) {
            is Slime -> size = getMetaValue("slime_size", String::toInt)!!
        }
    }

    private fun <T> RpgCreatureStatistic.get() = statistics[this].cast<T>()

}
