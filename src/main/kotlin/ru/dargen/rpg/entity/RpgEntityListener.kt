package ru.dargen.rpg.entity

import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Arrow
import org.bukkit.entity.EntityType
import org.bukkit.entity.Fish
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.*
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerInteractEvent
import ru.dargen.rpg.entity.projectile.RpgProjectile
import ru.dargen.rpg.entity.projectile.launchProjectile
import ru.dargen.rpg.item.asRpg
import ru.dargen.rpg.item.isHook
import ru.dargen.rpg.player.RpgPlayer
import ru.dargen.rpg.player.asRpgPlayer
import ru.starfarm.core.event.GlobalEventContext
import ru.starfarm.core.util.cast


object RpgEntityListener {

    @EventHandler
    private fun EntityRegainHealthEvent.handle() {
        isCancelled = true
    }


    @EventHandler
    private fun EntityDamageByEntityEvent.handle() {
        if (isCancelled) return
        isCancelled = true

        val projectile = damager?.takeIf { it is RpgProjectile }?.cast<RpgProjectile>()
        val damager = projectile?.entity ?: getDamager()?.asRpg ?: return
        val weapon = if (projectile == null)
            damager.takeIf { it is RpgPlayer }?.handle?.cast<Player>()?.inventory?.itemInMainHand?.asRpg
        else projectile.weapon
        val damaged = entity.asRpg ?: return

        damaged.damage(damager, weapon)
    }

    @EventHandler
    private fun EntityDamageEvent.handle() {
        val entity = entity.asRpg ?: return

        if (entityType == EntityType.PLAYER && cause.ordinal > 3) {
            isCancelled = true
            if (cause == EntityDamageEvent.DamageCause.VOID) entity.death()
            else entity.damageHealth((entity.maxHealth * damage / 20.0).toInt())
        } else if (cause.ordinal > 3) isCancelled = true
    }


    @EventHandler
    private fun EntityShootBowEvent.handle() {
        isCancelled = true
        val entity = entity.asRpg ?: return
        val item = bow.asRpg

//        val target = entity.location.getNearEntities(15)
//            .filter { it !== entity.handle }
//            .filter { (entity.location.yaw - entity.location.apply { direction = entity.location.direction(it.location) }.yaw).absoluteValue <= 60 }
//            .minByOrNull { (entity.location.yaw - entity.location.apply { direction = entity.location.direction(it.location) }.yaw).absoluteValue }

        val arrow = Arrow::class.launchProjectile(entity, item, projectile.velocity)
//        if (target != null) Tasks.every(2, 2, 5) {
//            if (!arrow.isDead && !target.isDead) {
//                arrow.velocity = target.location.direction(arrow.location).multiply(1.7)
//            } else it.cancel()
//        }
    }

    @EventHandler
    private fun PlayerInteractEvent.handle() {
        if (isCancelled) return

        val player = player.asRpgPlayer ?: return
        val item = item?.asRpg ?: return

        val handle = getPlayer().cast<CraftPlayer>().handle
        if (handle.hookedFish != null && getItem()?.type.isHook) {
            handle.hookedFish?.die()
            isCancelled = true
        }
    }

    @EventHandler
    private fun PlayerFishEvent.handle() {
        when (state) {
            PlayerFishEvent.State.FISHING -> {
                val player = player.asRpgPlayer ?: return
                val item = (getPlayer().inventory.itemInMainHand?.asRpg ?: getPlayer().inventory.itemInOffHand?.asRpg) ?: return

                hook.remove()

                Fish::class.launchProjectile(player, item, hook.velocity.multiply(2))
            }

            PlayerFishEvent.State.IN_GROUND -> {
                hook.remove()
            }

            PlayerFishEvent.State.FAILED_ATTEMPT -> {
                hook.remove()
            }

            else -> {
                isCancelled = true
                hook.remove()
            }
        }
    }

    @EventHandler
    private fun ProjectileHitEvent.handle() {
        entity.remove()
        if (entity is Fish && hitEntity != null)
            GlobalEventContext.post(EntityDamageByEntityEvent(entity, hitEntity, null, .0))
    }

}