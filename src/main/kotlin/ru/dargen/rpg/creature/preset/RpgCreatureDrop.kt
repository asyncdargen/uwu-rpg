package ru.dargen.rpg.creature.preset

import ru.dargen.rpg.item.RpgItem
import ru.dargen.rpg.item.RpgItemRegistry
import ru.dargen.rpg.util.asRange

data class RpgCreatureDrop(val itemId: Short, val count: IntRange, val chance: Double) {

    val item: RpgItem get() = RpgItemRegistry[itemId]
    val fixedCount get() = count.random()

    companion object {
        fun valueOf(text: String) = text.split(":", limit = 3).let { RpgCreatureDrop(it[0].toShort(), it[1].asRange, it[2].toDouble()) }
    }

}