package ru.dargen.rpg.creature.minecraft.bridge

import net.minecraft.server.v1_12_R1.*
import ru.dargen.rpg.util.minecraft.asSoundEffect
import ru.dargen.rpg.util.minecraft.soundOf

class BridgedZombie(world: World) : EntityZombie(world), RpgEntityBridge {

    override fun F() = asRpg.entityType.soundOf("AMBIENT")?.asSoundEffect

    override fun dm() = asRpg.entityType.soundOf("STEP")?.asSoundEffect

    override fun d(source: DamageSource) = null

    override fun cf() = null

}

class BridgedSkeleton(world: World) : EntitySkeleton(world), RpgEntityBridge {

    override fun F() = asRpg.entityType.soundOf("AMBIENT")?.asSoundEffect

//    override fun dm() = asRpg.entityType.soundOf("STEP")?.asSoundEffect

    override fun d(source: DamageSource) = null

    override fun cf() = null

}
class BridgedWitherSkeleton(world: World) : EntitySkeletonWither(world), RpgEntityBridge {

    override fun F() = asRpg.entityType.soundOf("AMBIENT")?.asSoundEffect

//    override fun dm() = asRpg.entityType.soundOf("STEP")?.asSoundEffect

    override fun d(source: DamageSource) = null

    override fun cf() = null

}
class BridgedStraySkeleton(world: World) : EntitySkeletonStray(world), RpgEntityBridge {

    override fun F() = asRpg.entityType.soundOf("AMBIENT")?.asSoundEffect

//    override fun dm() = asRpg.entityType.soundOf("STEP")?.asSoundEffect

    override fun d(source: DamageSource) = null

    override fun cf() = null

}
class BridgedPig(world: World) : EntityPig(world), RpgEntityBridge {

    override fun F() = asRpg.entityType.soundOf("AMBIENT")?.asSoundEffect

//    override fun dm() = asRpg.entityType.soundOf("STEP")?.asSoundEffect

    override fun d(source: DamageSource) = null

    override fun cf() = null

}
class BridgedVillager(world: World) : EntityVillager(world), RpgEntityBridge {

    override fun F() = asRpg.entityType.soundOf("AMBIENT")?.asSoundEffect

//    override fun dm() = asRpg.entityType.soundOf("STEP")?.asSoundEffect

    override fun d(source: DamageSource) = null

    override fun cf() = null

    override fun do_() = true

}
class BridgedSlime(world: World) : EntitySlime(world), RpgEntityBridge {

    init {
        moveController = ControllerMove(this)
    }

    override fun F() = asRpg.entityType.soundOf("AMBIENT")?.asSoundEffect

//    override fun dm() = asRpg.entityType.soundOf("STEP")?.asSoundEffect

    override fun d(source: DamageSource) = null

    override fun cf() = null

    override fun d(human: EntityHuman) {

    }

}