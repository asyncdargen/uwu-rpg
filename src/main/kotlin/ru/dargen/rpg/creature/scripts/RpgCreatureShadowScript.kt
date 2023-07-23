package ru.dargen.rpg.creature.scripts

import ru.dargen.rpg.creature.RpgCreature
import ru.dargen.rpg.creature.disguise.RpgDisguiseData
import ru.dargen.rpg.player.RpgPlayer

object RpgCreatureShadowScript : RpgCreatureScript("shadow") {

    override fun onSpawn(entity: RpgCreature) {
        entity.owner?.takeIf { it is RpgPlayer }?.let {
            entity.disguise = RpgDisguiseData.create(entity.bridge, it.handle.name)
        }
    }

}