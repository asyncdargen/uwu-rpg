package ru.dargen.rpg.event

import ru.dargen.rpg.item.RpgItem
import ru.dargen.rpg.player.RpgPlayer
import ru.starfarm.core.event.pattern.CancellableCoreEvent

class ArmorEquipmentEvent(val player: RpgPlayer, val oldArmor: RpgItem?, val armor: RpgItem?) : CancellableCoreEvent()