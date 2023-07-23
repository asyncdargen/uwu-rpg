package ru.dargen.rpg.item

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import ru.dargen.rpg.item.types.RpgItemAttribute
import ru.dargen.rpg.player.RpgPlayer
import ru.starfarm.core.ApiManager
import ru.starfarm.core.util.cast
import ru.starfarm.core.util.item.lore
import ru.starfarm.core.util.item.name
import java.util.concurrent.TimeUnit
import kotlin.math.max

object InteractLimiter {

    val AttackCache = CacheBuilder.newBuilder()
        .expireAfterAccess(2, TimeUnit.SECONDS)
        .build<RpgPlayer, Long>()!!
    val UseCache = CacheBuilder.newBuilder()
        .expireAfterAccess(2, TimeUnit.SECONDS)
        .build<RpgPlayer, Long>()!!

    fun canBe(player: RpgPlayer, weapon: RpgItem?, cache: Cache<RpgPlayer, Long>): Boolean {
        val cooldown = 1000.0 / (weapon?.getAttribute<Int>(RpgItemAttribute.ATTACK_SPEED) ?: RpgItemAttribute.ATTACK_SPEED.defaults.cast())
        return if (System.currentTimeMillis() - (cache.getIfPresent(player) ?: 0L) >= cooldown) {
            cache.put(player, System.currentTimeMillis())
            true
        } else false
    }

    fun canUse(player: RpgPlayer, weapon: RpgItem?) = canBe(player, weapon, UseCache)

    fun canAttack(player: RpgPlayer, weapon: RpgItem?) = canBe(player, weapon, AttackCache)

    fun clear(player: RpgPlayer) {
        UseCache.invalidate(player)
        AttackCache.invalidate(player)
    }

}

val Material?.isHelmet
    get() = this == Material.LEATHER_HELMET
            || this == Material.CHAINMAIL_HELMET || this == Material.IRON_HELMET
            || this == Material.GOLD_HELMET || this == Material.DIAMOND_HELMET

val Material?.isChestPlate
    get() = this == Material.LEATHER_CHESTPLATE
            || this == Material.CHAINMAIL_CHESTPLATE || this == Material.IRON_CHESTPLATE
            || this == Material.GOLD_CHESTPLATE || this == Material.DIAMOND_CHESTPLATE

val Material?.isLeggings
    get() = this == Material.LEATHER_LEGGINGS
            || this == Material.CHAINMAIL_LEGGINGS || this == Material.IRON_LEGGINGS
            || this == Material.GOLD_LEGGINGS || this == Material.DIAMOND_LEGGINGS

val Material?.isBoots
    get() = this == Material.LEATHER_BOOTS
            || this == Material.CHAINMAIL_BOOTS || this == Material.IRON_BOOTS
            || this == Material.GOLD_BOOTS || this == Material.DIAMOND_BOOTS

val Material?.isElytra get() = this == Material.ELYTRA

val Material?.isArmor get() = isHelmet || isChestPlate || isLeggings || isBoots || isElytra

val ItemStack?.isArmor get() = this?.type?.isArmor == true

val Material?.isSword
    get() = this == Material.WOOD_SWORD || this == Material.IRON_SWORD
            || this == Material.GOLD_SWORD || this == Material.DIAMOND_SWORD

val Material?.isAxe
    get() = this == Material.WOOD_AXE || this == Material.IRON_AXE
            || this == Material.GOLD_AXE || this == Material.DIAMOND_AXE

val Material?.isHook
    get() = this == Material.FISHING_ROD

val Material?.isBow
    get() = this == Material.BOW

val Material?.isRemoteWeapon get() = isBow || isHook

val Material?.isWeapon
    get() = isSword || isAxe || isHook || isBow

val ItemStack?.isWeapon get() = this?.type?.isWeapon == true

val ItemStack?.isRemoteWeapon get() = this?.type?.isRemoteWeapon == true

val ItemStack?.isAirOrNull get() = this == null || this.type == Material.AIR

var ItemStack.safeAmount
    set(value) {
        amount = max(0, value)
    }
    get() = amount

//val ItemStack.toStringLore get() = lore.joinToString("\n")

val ItemStack.asChatComponent
    get() = ApiManager.buildMessage("$name" + (if (amount > 1) " §fx$amount" else "")) {
        hoverText(name!!, *lore.toTypedArray())
    }.build()

fun Location.dropItems(items: Map<RpgItem, Int>, glow: Boolean = false) = items.forEach { (item, _count) ->
    var count = _count
    val stack = item.baseItemStack.maxStackSize
    val drops = count / stack + (if (count % stack != 0) 1 else 0)
    repeat(drops) {
        val drop = if (count / stack != 0) {
            count -= stack; stack
        } else count
        world.dropItem(this, item.buildBase(drop)).takeIf { glow }?.let(item.quality::addItem)
    }
}