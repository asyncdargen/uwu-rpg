package ru.dargen.rpg.spell.release

import org.bukkit.Material
import ru.dargen.rpg.player.RpgPlayer

object RpgSpellJump : RpgSpellReleaseHandler {

    override fun release(player: RpgPlayer) {
        player.handle.velocity = player.location.direction.multiply(1.6).setY(.6)
    }

    override fun isAllowedToUse(player: RpgPlayer): Boolean {
        if (!player.handle.isOnGround
            && !player.handle.isGliding
            && !player.location.block.type.let { it == Material.WATER || it == Material.STATIONARY_WATER }
        ) {
            player.sendMessage("§cМожно прыгнуть только с земли, на крыльях или из воды!")
            return false
        }
        return true
    }

}