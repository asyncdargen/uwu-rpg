package ru.dargen.rpg.item.types

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Item
import org.bukkit.scoreboard.NameTagVisibility
import ru.starfarm.core.util.cast
import java.sql.ResultSet

//Database parsing
private val PairParser: Any.() -> Any = {
    cast<String>().split('-', limit = 2).map(String::toInt).let { it[0] to it[1] }
}

enum class RpgItemAttribute(
    val icon: String, val display: String,
    val defaults: Any, val parser: Any.() -> Any = { this },
    val hide: Boolean = false, val percent: Boolean = false
) {

    LEVEL("§e♛", "Уровень предмета", 1),
    QUALITY("§e♛", "Качество предмета", RpgItemQuality.COMMON, { enumValueOf<RpgItemQuality>(cast()) }),
    DAMAGE("§4⚔", "Урон", 0 to 0, PairParser),
    DAMAGE_MAGIC("§9⚔", "Магический урон", 0 to 0, PairParser),
    CRIT("§6⚡", "Шанс крит. удара", 0, percent = true),
    VAMPIRING("§4♰", "Вампиризм", 0, percent = true),
    HEALTH("§c❤", "Здоровье", 0),
    REGENERATION("§2☩", "Регенерация", 0),
    MANA("§9✯", "Мана", 0),
    REGENERATION_MANA("§3♆", "Регенерация маны", 0),
    ARMOR("§7☬", "Физическая броня", 0),
    ARMOR_MAGIC("§b☬", "Магическая броня", 0),
    RESISTANCE("§6✪", "Сопротивление", 0),
    ATTACK_SPEED("§e☇", "Скорость атаки", 4),
    SPEED("§a⇨", "Скорость", 0, percent = true),

    REQUIRED_LEVEL("", "Требуемый уровень", 0, hide = true) {
        override fun toString(value: Any) = "§7$display: $value"
    },
    BOUND("", "", RpgItemBound.SOUL, { enumValueOf<RpgItemBound>(cast()) }, hide = true) {
        override fun toString(value: Any) = "$value"
    };

    open fun toString(value: Any) =
        "${if (icon.isBlank()) "" else "$icon "}§7$display: ${toStringValue(value)}"

    fun toStringValue(value: Any) = when (value) {
        is Pair<*, *> -> "${value.first}-${value.second}"
        is Number -> if (percent) "$value%" else value.toString()
        else -> value.toString()
    }

    companion object {

        fun getItemAttributes(resultSet: ResultSet): Map<RpgItemAttribute, Any> = enumValues<RpgItemAttribute>().associateWith {
            val value = resultSet.getObject(it.name.lowercase())
            if (value == null || (value is String && value.isBlank()) || value == 0) null
            else it.parser(value)
        }.filterNot { it.value == null }.cast()

    }

}

enum class RpgItemBound(val display: String) {

    SOUL("§4Привязано к персонажу"),
    QUEST("§6Квестовый предмет");

    override fun toString() = display

}

enum class RpgItemMark() {

}

enum class RpgItemQuality(val display: String, val color: ChatColor, val enchantChanceOffset: Int) {

    COMMON("обычное", ChatColor.WHITE, 0),
    UNCOMMON("необычное", ChatColor.DARK_GREEN, 7),
    RARE("редкое", ChatColor.DARK_BLUE, 9),
    EPIC("эпическое", ChatColor.DARK_PURPLE, 12),
    LEGENDARY("легендарное", ChatColor.GOLD, 15),
    MYTHIC("мифическое", ChatColor.RED, 17),
    STAFF("админское", ChatColor.DARK_RED, -1);

    val team by lazy {
        Bukkit.getServer().scoreboardManager.mainScoreboard.registerNewTeam(name).apply {
            color = this@RpgItemQuality.color
            prefix = color.toString()
            nameTagVisibility = NameTagVisibility.NEVER
        }
    }

    fun addItem(item: Item) {
        item.isGlowing = true
        team.addEntry(item.uniqueId.toString())
    }

    fun removeItem(item: Item) = team.removeEntry(item.uniqueId.toString())

    override fun toString() = "$color$display"

}

