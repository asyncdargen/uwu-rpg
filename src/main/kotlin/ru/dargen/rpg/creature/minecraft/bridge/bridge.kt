package ru.dargen.rpg.creature.minecraft.bridge

import net.minecraft.server.v1_12_R1.*
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import ru.dargen.rpg.Logger
import ru.dargen.rpg.creature.asRpgCreature
import ru.dargen.rpg.creature.minecraft.pathfinder.RpgPathfinderBase
import ru.dargen.rpg.creature.minecraft.pathfinder.RpgPathfinderSelector
import ru.starfarm.core.util.cast

interface RpgEntityBridge {

    val self: EntityInsentient get() = cast()
    val asBukkit: LivingEntity get() = self.bukkitEntity.cast()
    val metadata get() = self.dataWatcher!!
    val asRpg get() = self.bukkitEntity.asRpgCreature!!

    val goal: RpgPathfinderSelector get() = self.goalSelector.cast()
    val target: RpgPathfinderSelector get() = self.targetSelector.cast()

    /*pathfinder register*/

    fun injectRpg() {
        self.goalSelector = RpgPathfinderSelector()
        self.targetSelector = RpgPathfinderSelector()
    }

    fun addGoal(priority: Int, pathfinder: PathfinderGoal) = goal.addTask(priority, preparePathfinder(pathfinder))

    fun addGoal(pathfinder: RpgPathfinderBase) = goal.addTask(pathfinder.priority, preparePathfinder(pathfinder))

    fun preparePathfinder(pathfinder: PathfinderGoal) = pathfinder.apply {
        if (this is RpgPathfinderBase && !hasEntity) handle = self
    }

    companion object {

        private val Bridges = HashMap<EntityType, (World) -> RpgEntityBridge>()

        init {
            bridge(::BridgedZombie)
            bridge(::BridgedSkeleton)
            bridge(::BridgedWitherSkeleton)
            bridge(::BridgedStraySkeleton)
            bridge(::BridgedSlime)
            bridge(::BridgedVillager)
            bridge(::BridgedPig)
        }

        fun new(type: EntityType, location: Location) =
            (Bridges[type] ?: throw IllegalStateException("Unknown entity bridge type (${type})"))(location.world.cast<CraftWorld>().handle)
                .apply {
                    injectRpg()
                    self.setPositionRotation(location.x, location.y, location.z, location.yaw, location.pitch)
                }

        private inline fun <reified E> bridge(noinline initializer: (World) -> E) where E : EntityLiving, E : RpgEntityBridge {
            val bridgeClassType = E::class.java
            val originClassType = bridgeClassType.superclass.cast<Class<out Entity>>()

            val entityId = EntityTypes.b.a(originClassType)
            val entityType = EntityType.fromId(entityId)

            Bridges[entityType] = initializer
            EntityTypes.b.a(EntityTypes.b.b(originClassType), originClassType)
            EntityTypes.b.a(entityId, MinecraftKey(bridgeClassType.simpleName.substring(7) + "_bridge"), bridgeClassType)
            Logger.info("Registered entity bridge ${bridgeClassType.simpleName} for $entityType")
        }

    }

}