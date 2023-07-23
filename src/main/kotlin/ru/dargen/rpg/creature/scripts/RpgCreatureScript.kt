package ru.dargen.rpg.creature.scripts

import ru.dargen.rpg.creature.RpgCreature
import ru.dargen.rpg.entity.RpgEntity
import ru.dargen.rpg.entity.combat.RpgEntityDamageEvent
import ru.dargen.rpg.player.RpgPlayer
import ru.starfarm.core.util.cast
import java.lang.reflect.Modifier

abstract class RpgCreatureScript(val name: String) {

    open fun onRemove(entity: RpgCreature) {}

    open fun onSpawn(entity: RpgCreature) {}

    open fun onTarget(entity: RpgCreature, target: RpgEntity<*>) {}

    open fun wipe(entity: RpgCreature) {}

    open fun interact(entity: RpgCreature, player: RpgPlayer) {}

    open fun tick(entity: RpgCreature, tick: Int) {}

    open fun tickAsync(entity: RpgCreature, tick: Int) {}

    open fun damageDeal(entity: RpgCreature, event: RpgEntityDamageEvent) {}

    open fun damageReceive(entity: RpgCreature, event: RpgEntityDamageEvent) {}

    open fun onKill(entity: RpgCreature, event: RpgEntityDamageEvent) {}

    open fun death(entity: RpgCreature, event: RpgEntityDamageEvent? = null) {}

    companion object {

        fun resolveScript(scriptClass: Class<out RpgCreatureScript>): RpgCreatureScript? {
            return (runCatching {
                scriptClass.getDeclaredField("INSTANCE")
                    .takeIf { Modifier.isStatic(it.modifiers) }
                    ?.apply { isAccessible = true }?.get(null)
            }.getOrNull() ?: runCatching(scriptClass::newInstance).getOrNull()).cast()
        }

    }

}