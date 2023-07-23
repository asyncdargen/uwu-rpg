package ru.dargen.rpg.creature.minecraft.pathfinder

import net.minecraft.server.v1_12_R1.PathfinderGoal
import net.minecraft.server.v1_12_R1.PathfinderGoalSelector
import ru.starfarm.core.util.unit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private const val CHECK_TICK = 3

class RpgPathfinderSelector : PathfinderGoalSelector(null) {

    private val tasksLock: Lock = ReentrantLock()
    private val ticks = AtomicInteger()

    private var cursor = AtomicInteger()

    var tasks: MutableSet<RpgPathfinderTask> = hashSetOf()
    val activeTasks: MutableSet<RpgPathfinderTask> = hashSetOf()

    private fun sortTasks() {
        tasks = tasks.sortedWith(compareBy<RpgPathfinderTask>(
            { if (it.priorityBypass) Int.MAX_VALUE else it.priority },
            { Int.MAX_VALUE - it.secondPriority}
        ).reversed()).toMutableSet()
    }

    fun addTask(priority: Int, pathfinder: PathfinderGoal) = tasksLock.withLock {
        tasks.add(RpgPathfinderTask(priority, pathfinder))
        sortTasks()
    }

    override fun a(priority: Int, pathfinder: PathfinderGoal) = addTask(priority, pathfinder)

    fun removeTasks(priority: Int) = tasksLock.withLock {
        tasks.removeIf { it.priority == priority }
    }

    fun removeTask(pathfinder: PathfinderGoal) = tasksLock.withLock {
        tasks.removeIf { it.pathfinder == pathfinder }
    }

    fun removeTasks(pathfinderClass: Class<out PathfinderGoal>) = tasksLock.withLock {
        tasks.removeIf { pathfinderClass.isAssignableFrom(it.pathfinder.javaClass) }
    }

    override fun a(pathfinder: PathfinderGoal) = removeTask(pathfinder).unit()

    /*tick*/
    override fun a() {
        if (ticks.getAndIncrement() % CHECK_TICK == 0) tasksLock.withLock {
            var priority = -1
            tasks.forEach {
                if (it.using) {
                    if ((!it.priorityBypass && it.priority < priority) || !it.shouldContinue()) {
                        it.reset()
                        activeTasks.remove(it)
                    } else if (!it.priorityBypass) priority = it.priority
                } else if ((it.priorityBypass || it.priority >= priority )&& it.shouldExecution()) {
                    it.execute()
                    activeTasks.add(it)
                    if (!it.priorityBypass) priority = it.priority
                }
            }
        } else activeTasks.forEach { it.navigate() }
    }

    inner class RpgPathfinderTask(
        val priority: Int,
        val pathfinder: PathfinderGoal,
        var using: Boolean = false,
        val secondPriority: Int = cursor.getAndIncrement()
    ) {

        val priorityBypass get() = priority == -1

        fun shouldExecution() = pathfinder.a()

        fun shouldContinue() = pathfinder.b()

        fun execute() {
            using = true
            pathfinder.c()
        }

        fun reset() {
            using = false
            pathfinder.d()
        }

        fun navigate() = pathfinder.e()

    }

}

