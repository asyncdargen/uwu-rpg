package ru.dargen.rpg.spell.release

import ru.dargen.rpg.player.RpgPlayer

interface RpgSpellReleaseHandler {

    fun release(player: RpgPlayer)

    fun isAllowedToUse(player: RpgPlayer): Boolean = true

}