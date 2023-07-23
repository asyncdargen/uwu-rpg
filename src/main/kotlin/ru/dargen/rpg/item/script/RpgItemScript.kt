package ru.dargen.rpg.item.script

import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import ru.dargen.rpg.entity.combat.RpgEntityDamageEvent
import ru.dargen.rpg.item.RpgItem
import ru.dargen.rpg.player.RpgPlayer
import ru.starfarm.core.util.cast
import java.lang.reflect.Modifier

abstract class RpgItemScript(val name: String) {

    open fun apply(item: RpgItem) {}

    open fun pickUp(item: RpgItem, player: RpgPlayer, event: EntityPickupItemEvent) {}

    open fun interact(item: RpgItem, player: RpgPlayer, event: PlayerInteractEvent) {}

    open fun interactAtEntity(item: RpgItem, player: RpgPlayer, event: PlayerInteractAtEntityEvent) {}

    open fun interactOnInventory(item: RpgItem, clickedItem: RpgItem, player: RpgPlayer, event: InventoryClickEvent) {}

    open fun damageDeal(event: RpgEntityDamageEvent) {}

    companion object {

        fun resolveScript(scriptClass: Class<out RpgItemScript>): RpgItemScript? {
            return (runCatching {
                scriptClass.getDeclaredField("INSTANCE")
                    .takeIf { Modifier.isStatic(it.modifiers) }
                    ?.apply { isAccessible = true }?.get(null)
            }.getOrNull() ?: runCatching(scriptClass::newInstance).getOrNull()).cast()
        }

    }

}