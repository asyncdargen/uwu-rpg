package ru.dargen.rpg.item.script

import org.bukkit.Sound
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import ru.dargen.rpg.creature.RpgCreatureRegistry
import ru.dargen.rpg.item.RpgItem
import ru.dargen.rpg.item.safeAmount
import ru.dargen.rpg.player.RpgPlayer
import ru.dargen.rpg.util.random.percentRandomSuccess
import ru.dargen.rpg.util.rpg.getMetaValue
import kotlin.math.max

object RpgItemEnchantScript : RpgItemScript("enchant") {

    fun RpgItem.canEnchant(item: RpgItem) =
        item.level >= getMetaValue("enchant_min_level", String::toInt)!!
                && item.level <= getMetaValue("enchant_max_level", String::toInt)!!
                && (item.isArmor || item.isWeapon || item.hasMetaValue("enchant_enable"))
                && !item.hasMetaValue("enchant_disable")

    override fun interact(item: RpgItem, player: RpgPlayer, event: PlayerInteractEvent) {
        RpgCreatureRegistry.PresetMap.values.first().newEntity(player.location, player)
    }

    override fun interactOnInventory(item: RpgItem, clickedItem: RpgItem, player: RpgPlayer, event: InventoryClickEvent) {
        event.isCancelled = true
        if (clickedItem.enchant < 10) {
            if (event.currentItem.amount == 1 && item.canEnchant(clickedItem)) {
                event.cursor.safeAmount -= 1
                if (event.cursor.safeAmount == 0) event.cursor = null

                val enchant = clickedItem.enchant + 1
                val chance = (100 - 8 * enchant) - clickedItem.quality.enchantChanceOffset
                if (chance.percentRandomSuccess) {
                    event.clickedInventory.setItem(event.slot, clickedItem.withPrototype(1).buildItem(player))
                    player.sendMessage("§aТы успешно заточил предмет на §c+$enchant§a с шансом §e$chance%!")
                    player.playEnchantSound(enchant)
                } else item.getMetaValue("enchant_fail_type", FailEnchantResult::valueOf)!!.handle(clickedItem, player, event)

                player.handle.updateInventory()
            } else {
                player.sendMessage("§cЭта заточка не работает на данном предмете!")
                player.playSound(Sound.BLOCK_NOTE_BASS)
            }
        } else {
            player.sendMessage("§cПредмет максимально заточен!")
            player.playSound(Sound.BLOCK_NOTE_BASS)
        }
    }

    enum class FailEnchantResult(val handle: (item: RpgItem, player: RpgPlayer, event: InventoryClickEvent) -> Unit) {

        BREAK_ITEM({ item, player, event ->
            player.sendMessage("§cТебе не повезло и предмет сломался!")
            player.playSound(Sound.ENTITY_ITEM_BREAK)
            event.clickedInventory.setItem(event.slot, null)
        }),
        DECREASE_OR_BREAK_ITEM({ item, player, event ->
            (if (item.enchant >= 7) BREAK_ITEM else DECREASE_ENCHANT).handle(item, player, event)
        }),
        DECREASE_ENCHANT({ item, player, event ->
            player.sendMessage("§cТебе не повезло и предмет потерял уровень заточки!")
            player.playSound(Sound.BLOCK_FIRE_EXTINGUISH)
            event.clickedInventory.setItem(event.slot, item.withPrototype(enchant = max(0, item.enchant - 1).toByte()).buildItem(player))
        })

    }

    private fun RpgPlayer.playEnchantSound(enchant: Int) = playNearSound(
        when (enchant) {
            in 7..10 -> Sound.BLOCK_END_PORTAL_SPAWN
            else -> Sound.BLOCK_NOTE_BELL
        })

}