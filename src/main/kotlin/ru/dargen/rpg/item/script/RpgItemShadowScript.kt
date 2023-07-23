package ru.dargen.rpg.item.script

import org.bukkit.event.player.PlayerInteractEvent
import ru.dargen.rpg.creature.RpgCreatureRegistry
import ru.dargen.rpg.item.RpgItem
import ru.dargen.rpg.player.RpgPlayer

object RpgItemShadowScript : RpgItemScript("shadow") {

    override fun interact(item: RpgItem, player: RpgPlayer, event: PlayerInteractEvent) {
        event.isCancelled = true
        RpgCreatureRegistry[3].newEntity(player.location, player)
    }

}