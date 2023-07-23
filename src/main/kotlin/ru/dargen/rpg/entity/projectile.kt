package ru.dargen.rpg.entity.projectile

import net.minecraft.server.v1_12_R1.*
import net.minecraft.server.v1_12_R1.Entity
import net.minecraft.server.v1_12_R1.EnumDirection.EnumAxis
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftFish
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_12_R1.potion.CraftPotionUtil
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType
import org.bukkit.util.Vector
import ru.dargen.accessors.Accessors
import ru.dargen.rpg.entity.RpgEntity
import ru.dargen.rpg.entity.minecraftEntity
import ru.dargen.rpg.item.RpgItem
import ru.starfarm.core.util.cast
import kotlin.reflect.KClass

val BukkitEntityAccessor = Accessors.unsafeFieldAccessor<CraftEntity>(Entity::class.java, "bukkitEntity")

fun <P : Projectile> KClass<P>.launchProjectile(launcher: RpgEntity<*>, weapon: RpgItem? = null, velocity: Vector): Projectile {
    val projectile = launcher.handle.launchRpgProjectile(java, velocity.multiply(1.5))

    return projectile.rebaseProjectile(launcher, weapon)
}

private fun <T : Projectile?> LivingEntity.launchRpgProjectile(projectile: Class<out T>, velocity: Vector?): Projectile {
    val world: World = world.cast<CraftWorld>().handle
    var launch: Entity? = null
    if (Snowball::class.java.isAssignableFrom(projectile)) {
        launch = EntitySnowball(world, minecraftEntity)
    } else if (Egg::class.java.isAssignableFrom(projectile)) {
        launch = EntityEgg(world, minecraftEntity)
    } else if (EnderPearl::class.java.isAssignableFrom(projectile)) {
        launch = EntityEnderPearl(world, minecraftEntity)
    } else if (Arrow::class.java.isAssignableFrom(projectile)) {
        if (TippedArrow::class.java.isAssignableFrom(projectile)) {
            launch = EntityTippedArrow(world, minecraftEntity)
            launch.type = CraftPotionUtil.fromBukkit(PotionData(PotionType.WATER, false, false))
        } else if (SpectralArrow::class.java.isAssignableFrom(projectile)) {
            launch = EntitySpectralArrow(world, minecraftEntity)
        } else {
            launch = EntityTippedArrow(world, minecraftEntity)
        }
    } else if (ThrownPotion::class.java.isAssignableFrom(projectile)) {
        launch = if (LingeringPotion::class.java.isAssignableFrom(projectile)) {
            EntityPotion(world, minecraftEntity, CraftItemStack.asNMSCopy(ItemStack(Material.LINGERING_POTION, 1)))
        } else {
            EntityPotion(world, minecraftEntity, CraftItemStack.asNMSCopy(ItemStack(Material.SPLASH_POTION, 1)))
        }
    } else if (ThrownExpBottle::class.java.isAssignableFrom(projectile)) {
        launch = EntityThrownExpBottle(world, minecraftEntity)
    } else if (Fish::class.java.isAssignableFrom(projectile) && minecraftEntity is EntityHuman) {
        launch = EntityFishingHook(world, minecraftEntity as EntityHuman)
    } else {
        val location: Location
        val direction: Vector
        if (Fireball::class.java.isAssignableFrom(projectile)) {
            location = eyeLocation
            direction = location.direction.multiply(10)
            launch = if (SmallFireball::class.java.isAssignableFrom(projectile)) {
                EntitySmallFireball(world, minecraftEntity, direction.x, direction.y, direction.z)
            } else if (WitherSkull::class.java.isAssignableFrom(projectile)) {
                EntityWitherSkull(world, minecraftEntity, direction.x, direction.y, direction.z)
            } else if (DragonFireball::class.java.isAssignableFrom(projectile)) {
                EntityDragonFireball(world, minecraftEntity, direction.x, direction.y, direction.z)
            } else {
                EntityLargeFireball(world, minecraftEntity, direction.x, direction.y, direction.z)
            }
            (launch as EntityFireball).projectileSource = this
        } else if (LlamaSpit::class.java.isAssignableFrom(projectile)) {
            location = this.eyeLocation
            direction = location.direction
            launch = EntityLlamaSpit(world)
            launch.shooter = minecraftEntity
            launch.shoot(direction.x, direction.y, direction.z, 1.5f, 10.0f)
        } else if (ShulkerBullet::class.java.isAssignableFrom(projectile)) {
            location = this.eyeLocation
            launch = EntityShulkerBullet(world, minecraftEntity, null as Entity?, null as EnumAxis?)
        }
    }
    if (velocity != null) (launch!!.bukkitEntity as Projectile).velocity = velocity
    world.addEntity(launch)
    return launch!!.bukkitEntity as Projectile
}

private fun <P : Projectile> P.rebaseProjectile(launcher: RpgEntity<*>, weapon: RpgItem?): Projectile {
    val handle = cast<CraftEntity>().handle.cast<Entity>()
    handle.bukkitEntity

    val projectile = when (this) {
        is Fish -> RpgHookProjectile(this, handle.cast(), launcher, weapon)
        else -> RpgDefaultProjectile(this, handle, launcher, weapon)
    }
    BukkitEntityAccessor.setValue(handle, projectile)

    return projectile
}

interface RpgProjectile {

    val entity: RpgEntity<*>
    val weapon: RpgItem?

}

class RpgDefaultProjectile(origin: Projectile, handle: Entity, override val entity: RpgEntity<*>, override val weapon: RpgItem?) :
    CraftEntity(Bukkit.getServer().cast(), handle), RpgProjectile, Projectile by origin

class RpgHookProjectile(origin: Projectile, handle: EntityFishingHook, override val entity: RpgEntity<*>, override val weapon: RpgItem?) :
    CraftFish(Bukkit.getServer().cast(), handle), RpgProjectile, Projectile by origin

