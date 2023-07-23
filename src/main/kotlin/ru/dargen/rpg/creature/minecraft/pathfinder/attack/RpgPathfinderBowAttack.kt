package ru.dargen.rpg.creature.minecraft.pathfinder.attack

import net.minecraft.server.v1_12_R1.EntityLiving
import net.minecraft.server.v1_12_R1.Items
import org.bukkit.entity.Arrow
import ru.dargen.rpg.entity.projectile.launchProjectile
import ru.dargen.rpg.util.direction

class RpgPathfinderBowAttack(distance: Double, speed: Int) : RpgPathfinderAttackBase(distance, speed) {

    override fun shouldExecution() = super.shouldExecution() && handle.itemInMainHand?.item == Items.BOW && handle.entitySenses.a(handle.goalTarget)

    override fun damage(entityLiving: EntityLiving) {
        Arrow::class.launchProjectile(asRpg, null, bukkitLocation.direction(entityLiving.bukkitEntity.location))
    }

}