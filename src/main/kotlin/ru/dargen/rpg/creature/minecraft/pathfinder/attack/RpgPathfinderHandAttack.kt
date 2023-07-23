package ru.dargen.rpg.creature.minecraft.pathfinder.attack

import net.minecraft.server.v1_12_R1.EntityLiving
import net.minecraft.server.v1_12_R1.EnumHand
import net.minecraft.server.v1_12_R1.Items
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import ru.starfarm.core.event.GlobalEventContext

class RpgPathfinderHandAttack(distance: Double, speed: Int) : RpgPathfinderAttackBase(distance, speed) {

    override fun shouldExecution() = super.shouldExecution() && handle.itemInMainHand.item != Items.BOW

    override fun damage(entityLiving: EntityLiving) {
        handle.a(EnumHand.MAIN_HAND)
        GlobalEventContext.post(EntityDamageByEntityEvent(handle.bukkitEntity, entityLiving.bukkitEntity, EntityDamageEvent.DamageCause.ENTITY_ATTACK, .0))
    }

}