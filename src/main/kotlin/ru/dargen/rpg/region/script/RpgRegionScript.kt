package ru.dargen.rpg.region.script

import ru.dargen.rpg.player.RpgPlayer
import ru.dargen.rpg.region.RpgRegion
import ru.starfarm.core.util.cast
import java.lang.reflect.Modifier

abstract class RpgRegionScript(val name: String) {

    open fun join(region: RpgRegion, oldRegion: RpgRegion, player: RpgPlayer) {}

    open fun quit(region: RpgRegion, newRegion: RpgRegion, player: RpgPlayer) {}

    open fun tick(region: RpgRegion, tick: Int) {}

    companion object {

        fun resolveScript(scriptClass: Class<out RpgRegionScript>): RpgRegionScript? {
            return (runCatching {
                scriptClass.getDeclaredField("INSTANCE")
                    .takeIf { Modifier.isStatic(it.modifiers) }
                    ?.apply { isAccessible = true }?.get(null)
            }.getOrNull() ?: runCatching(scriptClass::newInstance).getOrNull()).cast()
        }

    }

}