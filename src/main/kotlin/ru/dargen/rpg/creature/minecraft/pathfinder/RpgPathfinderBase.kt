package ru.dargen.rpg.creature.minecraft.pathfinder

import net.minecraft.server.v1_12_R1.EntityInsentient
import net.minecraft.server.v1_12_R1.PathfinderGoal
import ru.dargen.rpg.creature.asRpgCreature

abstract class RpgPathfinderBase(val priority: Int) : PathfinderGoal() {

    constructor(entity: EntityInsentient, priority: Int) : this(priority) {
        this.handle = entity
    }

    lateinit var handle: EntityInsentient
    val hasEntity get() = this::handle.isInitialized

    val asBukkit get() = handle.bukkitEntity
    val asRpg get() = asBukkit.asRpgCreature!!

    val bukkitLocation get() = asBukkit.location

    val navigation get() = handle.navigation!!
    val lookController get() = handle.controllerLook!!
    val jumpController get() = handle.controllerJump!!
    val moveController get() = handle.controllerMove!!

    open fun shouldExecution(): Boolean = true

    open fun shouldContinue(): Boolean = true

    open fun isInterruptible(): Boolean = true

    open fun execute() {
    }

    open fun reset() {}

    open fun navigate() {}

    override fun a() = shouldExecution()

    override fun b() = shouldContinue()

    override fun g() = isInterruptible()

    override fun c() = execute()

    override fun d() = reset()

    override fun e() = navigate()

}