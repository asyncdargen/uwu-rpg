package ru.dargen.rpg.player

import org.bukkit.Material
import ru.dargen.rpg.item.RpgItem

enum class MoneyGrade(val icon: String, val type: Material, val price: Int) {

    IRON("§7⛀", Material.IRON_NUGGET, 1),
    GOLD("§e⛂", Material.GOLD_INGOT, 64),
    DIAMOND("§9⛁", Material.DIAMOND, 4086),
    EMERALD("§2⛃", Material.EMERALD, 262_144);

    lateinit var item: RpgItem

    companion object {

        fun valueOf(material: Material) = values().firstOrNull { it.type == material }

    }

}

val Int.asMoneyString
    get() = let {
        buildString {
            var self = it
            MoneyGrade.values().reversed().forEach {
                val remain = self / it.price
                if (remain > 0) append("§f$remain${it.icon} ")
                self -= remain * it.price
            }
            if (isBlank()) append("§f$self${MoneyGrade.IRON.icon}")
        }
    }

val Int.asMoney
    get() = let {
        buildMap {
            var self = it
            MoneyGrade.values().reversed().forEach {
                val remain = self / it.price
                if (remain > 0) put(it, remain)
                self -= remain * it.price
            }
        }
    }