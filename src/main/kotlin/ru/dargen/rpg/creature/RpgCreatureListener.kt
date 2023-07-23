package ru.dargen.rpg.creature

import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import ru.dargen.rpg.entity.asRpg
import ru.dargen.rpg.player.asRpgPlayer

object RpgCreatureListener {

    @EventHandler
    private fun PlayerInteractAtEntityEvent.handle() {
        val player = player.asRpgPlayer ?: return
        val entity = rightClicked?.asRpgCreature ?: return

        entity.interact(player)
    }

    @EventHandler
    private fun EntityTargetLivingEntityEvent.handle() {
        val entity = entity.asRpgCreature ?: return
        val target = target?.asRpg ?: return

        entity.onTarget(target)
    }



}