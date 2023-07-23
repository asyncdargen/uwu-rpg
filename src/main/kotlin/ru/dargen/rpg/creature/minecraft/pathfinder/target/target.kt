package ru.dargen.rpg.creature.minecraft.pathfinder.target

//class RpgPathfinderTargetByHurt(distance: Int) : RpgPathfinderTargetBase(distance) {
//
//    override val newTarget get() = handle.lastDamager?.takeIf { it.isTargetable && isInArea(it) }
//
//}
//
//class RpgPathfinderTargetByHurtOwner(distance: Int, val owner: RpgEntity<*>) : RpgPathfinderTargetBase(distance) {
//
//    override val newTarget get() = owner.combatState.lastDamagerEntity?.entity?.takeIf { it.isTargetable && isInArea(it) }
//
//}
//
//class RpgPathfinderTargetByOwnerHurt(distance: Int, val owner: RpgEntity<*>) : RpgPathfinderTargetBase(distance) {
//
//    override val newTarget get() = owner.combatState.lastAttackedEntity?.entity?.takeIf { it.isTargetable && isInArea(it) }
//
//}


//class RpgPathfinderTargetNear(distance: Double, val types: List<EntityType>) : RpgPathfinderTargetBase(distance) {
//
//    override val newTarget
//        get() = bukkitLocation.getNearEntities(distance)
//            .mapNotNull(Entity::asRpg)
//            .filter { it !== asRpg && it.entityType in types && it.entity.isTargetable && navigation.a(it.entity) != null }
//            .minByOrNull { bukkitLocation.distanceTo(it.location) }
//            ?.handle?.minecraftEntity
//
//}