package ru.dargen.rpg.player

import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import ru.dargen.rpg.Tasks
import ru.dargen.rpg.event.ArmorEquipmentEvent

object RpgPlayerListener {

    @EventHandler
    private fun PlayerDeathEvent.handle() {
        deathMessage = null
        keepInventory = true
        keepLevel = true
        drops.clear()
    }

    @EventHandler
    private fun PlayerJoinEvent.handle() {
        joinMessage = null
    }

    @EventHandler
    private fun PlayerQuitEvent.handle() {
        quitMessage = null
    }

    @EventHandler
    private fun PlayerKickEvent.handle() {
        leaveMessage = null
    }


    @EventHandler
    private fun ArmorEquipmentEvent.handle() {
        if (player.isPvp) {
            player.sendMessage("§cНельзя меня снаряжение во время боя!")
            isCancelled = true
        } else if (armor == null || armor.isAllowedToUse(player)) {
            player.playSound(Sound.ITEM_ARMOR_EQUIP_CHAIN)
            Tasks.after(1) { player.recomputeStatistics() }
        } else {
            player.sendMessage("§cВы не можете использовать этот предмет!")
            isCancelled = true
        }
    }

}