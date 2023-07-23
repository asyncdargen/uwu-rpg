package ru.dargen.rpg.event.listeners

import org.bukkit.entity.EntityType
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.*
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import ru.dargen.rpg.event.ArmorEquipmentEvent
import ru.dargen.rpg.item.*
import ru.dargen.rpg.player.RpgPlayer
import ru.dargen.rpg.player.asRpgPlayer
import ru.starfarm.core.event.GlobalEventContext

object ArmorUpdateListener {

    @EventHandler
    private fun InventoryClickEvent.handle() {
        if (isCancelled) return

        val player = whoClicked.asRpgPlayer ?: return
        val shift = click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT
        val numberKey = click == ClickType.NUMBER_KEY
        if (action == InventoryAction.NOTHING) return
        if (slotType != InventoryType.SlotType.ARMOR && slotType != InventoryType.SlotType.QUICKBAR && slotType != InventoryType.SlotType.CONTAINER) return
        if (clickedInventory != null && clickedInventory.type != InventoryType.PLAYER) return
        if (inventory.type != InventoryType.CRAFTING && inventory.type != InventoryType.PLAYER) return
        if (whoClicked.type != EntityType.PLAYER) return
        var newArmorType = ArmorType.getItemStackArmorType(if (shift) currentItem else cursor)
        if (!shift && newArmorType != null && rawSlot != newArmorType.slot) return
        if (shift) {
            newArmorType = ArmorType.getItemStackArmorType(currentItem)
            if (newArmorType != null) {
                val equipping = rawSlot != newArmorType.slot
                if (newArmorType == ArmorType.HELMET && equipping == whoClicked.inventory.helmet.isAirOrNull
                    || newArmorType == ArmorType.CHEST_PLATE && equipping == whoClicked.inventory.chestplate.isAirOrNull
                    || newArmorType == ArmorType.LEGGINGS && equipping == whoClicked.inventory.leggings.isAirOrNull
                    || newArmorType == ArmorType.BOOTS && equipping == whoClicked.inventory.boots.isAirOrNull
                ) {
                    if (!call(player, if (equipping) null else currentItem?.asRpg, if (equipping) currentItem?.asRpg else null))
                        isCancelled = true
                }
            }
        } else {
            var newArmorPiece: ItemStack? = cursor
            var oldArmorPiece = currentItem
            if (numberKey) {
                if (clickedInventory.type == InventoryType.PLAYER) {
                    val hotbarItem: ItemStack? = clickedInventory.getItem(hotbarButton)
                    if (hotbarItem?.isAirOrNull != true) {
                        newArmorType = ArmorType.getItemStackArmorType(hotbarItem)
                        newArmorPiece = hotbarItem
                        oldArmorPiece = clickedInventory.getItem(slot)
                    } else newArmorType = ArmorType.getItemStackArmorType(if (!currentItem.isAirOrNull) currentItem else cursor)
                }
            } else if (cursor.isAirOrNull && !currentItem.isAirOrNull)
                newArmorType = ArmorType.getItemStackArmorType(currentItem)
            if (newArmorType != null && rawSlot == newArmorType.slot) {
                if (!call(player, oldArmorPiece?.asRpg, newArmorPiece?.asRpg))
                    isCancelled = true
            }
        }
    }

    @EventHandler
    private fun PlayerInteractEvent.handle() {
        val player = player.asRpgPlayer ?: return
        if (item == null || action == Action.PHYSICAL || hand == EquipmentSlot.OFF_HAND) return
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            val armorType = ArmorType.getItemStackArmorType(item) ?: return
            val oldArmor = player.handle.inventory.getItem(armorType.inventorySlot)
            val newArmor = item
            if (call(player, oldArmor.asRpg, newArmor.asRpg)) {
                player.handle.inventory.setItem(armorType.inventorySlot, newArmor)
                player.handle.inventory.itemInMainHand = oldArmor
            } else {
                isCancelled = true
                player.handle.updateInventory()
            }
        }
    }

    @EventHandler
    fun InventoryDragEvent.onInventoryDrag() {
        val player = whoClicked?.asRpgPlayer ?: return
        val type = ArmorType.getItemStackArmorType(oldCursor)
        if (rawSlots.isEmpty()) return
        if (type != null && type.slot == (rawSlots.firstOrNull() ?: 0) && !call(player, null, oldCursor?.asRpg)) {
            result = Event.Result.DENY
            isCancelled = true
        }
    }

    private fun call(player: RpgPlayer, item: RpgItem?, oldItem: RpgItem?) = !GlobalEventContext.post(
        ArmorEquipmentEvent(player, item, oldItem)
    ).isCancelled

    enum class ArmorType(val slot: Int, val inventorySlot: Int) {

        HELMET(5, 39),
        CHEST_PLATE(6, 38),
        LEGGINGS(7, 37),
        BOOTS(8, 36);

        companion object {

            fun getItemStackArmorType(itemStack: ItemStack?): ArmorType? {
                val type = itemStack?.type
                return when {
                    type.isHelmet -> HELMET
                    type.isChestPlate || type.isElytra -> CHEST_PLATE
                    type.isLeggings -> LEGGINGS
                    type.isBoots -> BOOTS
                    else -> null
                }
            }

        }
    }

}