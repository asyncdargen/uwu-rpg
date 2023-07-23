package ru.dargen.rpg.entity

import net.minecraft.server.v1_12_R1.MinecraftServer
import org.bukkit.entity.Entity
import ru.dargen.rpg.Events
import ru.dargen.rpg.Tasks
import ru.starfarm.core.util.cast
import java.util.concurrent.ConcurrentHashMap

val Entity.asRpg
    get() = RpgEntityRegistry.getRpgEntity<RpgEntity<*>>(this)

val Entity.isRpg
    get() = RpgEntityRegistry.isRpgEntity(this)

object RpgEntityRegistry {

    val Entities: MutableMap<Int, RpgEntity<*>> = ConcurrentHashMap()

    init {
        Events.onListeners(RpgEntityListener)

        Tasks.every(1, 1) {
            val tick = MinecraftServer.currentTick
            Entities.values.forEach { it.tick(tick) }
        }
        Tasks.everyAsync(1, 1) {
            val tick = MinecraftServer.currentTick
            Entities.values.forEach { it.tickAsync(tick) }
        }
    }

    fun addRpgEntity(entity: RpgEntity<*>) = Entities.put(entity.entityId, entity.apply(RpgEntity<*>::onSpawn))

    fun removeRpgEntity(entityId: Int) = Entities.remove(entityId)?.apply(RpgEntity<*>::onRemove)

    fun removeRpgEntity(entity: RpgEntity<*>) = removeRpgEntity(entity.entityId)

    fun <E : RpgEntity<*>> getRpgEntity(entityId: Int) = Entities[entityId]?.cast<E>()

    fun <E : RpgEntity<*>> getRpgEntity(entity: Entity) = getRpgEntity<E>(entity.entityId)

    fun isRpgEntity(entityId: Int) = entityId in Entities

    fun isRpgEntity(entity: Entity) = isRpgEntity(entity.entityId)


}