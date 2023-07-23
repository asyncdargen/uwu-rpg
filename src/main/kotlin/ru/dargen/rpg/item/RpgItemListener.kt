package ru.dargen.rpg.item

import com.google.common.cache.CacheBuilder
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import ru.dargen.rpg.item.types.RpgItemAttribute
import ru.dargen.rpg.item.types.RpgItemBound
import ru.dargen.rpg.player.asRpgPlayer
import java.util.concurrent.TimeUnit

object RpgItemListener {

    private val DropSoulBoundCache = CacheBuilder.newBuilder()
        .expireAfterWrite(15, TimeUnit.SECONDS)
        .build<Player, RpgItemPrototype>()

    @EventHandler(priority = EventPriority.LOWEST)
    private fun PlayerDropItemEvent.handle() {
        val player = player.asRpgPlayer
        val item = itemDrop.itemStack.asRpg

        if (player != null && item != null) {
            if (item.hasAttribute(RpgItemAttribute.BOUND)) {
                if (item.bound == RpgItemBound.QUEST) {
                    player.sendMessage("§cВы не можете выкинуть квестовый предмет!")
                    isCancelled = true
                } else if (DropSoulBoundCache.getIfPresent(player.handle) != item.prototype) {
                    player.sendMessage("§cВы пытаетесь выкинуть предмет привязанный к персонажу, чтобы подтвердить - повторите.")
                    DropSoulBoundCache.put(player.handle, item.prototype)
                    isCancelled = true
                } else DropSoulBoundCache.invalidate(player.handle)
            }
        } else isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun EntityPickupItemEvent.handle() {
        val player = entity.asRpgPlayer
        val item = getItem().itemStack.asRpg

        if (player != null && item != null) {
            item.quality.removeItem(getItem())
            item.scripts.forEach { it.pickUp(item, player, this) }
            getItem().itemStack.itemMeta = item.buildItem(player).itemMeta
        } else isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun EntityDamageByEntityEvent.handle() {
        val player = damager.asRpgPlayer ?: return
        val item = player.handle.inventory.itemInMainHand?.asRpg

        when {
            !InteractLimiter.canAttack(player, item) -> isCancelled = true
            item != null && !item.isAllowedToUse(player) -> {
                player.sendMessage("§cВы не можете использовать этот предмет!")
                isCancelled = true
            }
            item?.isRemoteWeapon == true -> {
                player.sendMessage("§cНельзя наносить урон оружием дальнего действия!")
                isCancelled = true
            }
            item?.isArmor == true -> {
                player.sendMessage("§cНельзя наносить урон броней!")
                isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun PlayerInteractEvent.handle() {
        if (action == Action.PHYSICAL) return

        val player = getPlayer().asRpgPlayer ?: return
        val item = getItem()?.asRpg ?: return

        when {
            !InteractLimiter.canUse(player, item) -> isCancelled = true
            !item.isAllowedToUse(player) -> {
                player.sendMessage("§cВы не можете использовать этот предмет!")
                isCancelled = true
            }
            else -> item.scripts.forEach { it.interact(item, player, this) }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun PlayerInteractAtEntityEvent.handle() {
        if (hand == EquipmentSlot.OFF_HAND) return

        val player = getPlayer().asRpgPlayer
        val item = getPlayer().inventory.itemInMainHand?.asRpg

        when {
            player == null || item == null -> isCancelled = true
            !InteractLimiter.canUse(player, item) -> isCancelled = true
            !item.isAllowedToUse(player) -> {
                player.sendMessage("§cВы не можете использовать этот предмет!")
                isCancelled = true
            }
            else -> item.scripts.forEach { it.interactAtEntity(item, player, this) }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun InventoryClickEvent.handle() {
        if (isCancelled) return

        val player = whoClicked.asRpgPlayer ?: return
        val clickedItem = currentItem?.asRpg ?: return
        val item = cursor?.asRpg ?: return

        if (item.isAllowedToUse(player))
            item.scripts.forEach { it.interactOnInventory(item, clickedItem, player, this) }
    }

}