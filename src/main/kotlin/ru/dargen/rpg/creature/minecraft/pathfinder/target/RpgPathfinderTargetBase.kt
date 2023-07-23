package ru.dargen.rpg.creature.minecraft.pathfinder.target

//abstract class RpgPathfinderTargetBase(val distance: Double) : RpgPathfinderBase(3) {
//
//    var target: EntityLiving? = null
//
//    override fun shouldExecution(): Boolean {
//        if (handle.goalTarget.isTargetable && isInArea(handle.goalTarget!!)) return false
//        else {
//            if (handle.goalTarget != null && (!handle.goalTarget.isTargetable || !isInArea(handle.goalTarget!!))) {
//                target = null
//                return true
//            }
//
//            target = newTarget
//            return target != null
//        }
//    }
//
//    abstract val newTarget: EntityLiving?
//
//    override fun shouldContinue() = false
//
//    override fun execute() {
//        handle.setGoalTarget(
//            target,
//            if (target == null) EntityTargetEvent.TargetReason.FORGOT_TARGET
//            else EntityTargetEvent.TargetReason.RANDOM_TARGET,
//            true
//        )
//    }
//
//    protected fun isInArea(entityLiving: EntityLiving) = entityLiving.bukkitEntity.location.distanceTo(bukkitLocation) <= distance
//
//}