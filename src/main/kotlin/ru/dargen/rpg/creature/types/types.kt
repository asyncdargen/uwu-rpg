package ru.dargen.rpg.creature.types

import ru.dargen.rpg.entity.RpgEntityStatistic
import ru.dargen.rpg.util.max
import ru.dargen.rpg.util.min
import ru.starfarm.core.util.cast
import java.sql.ResultSet

enum class RpgCreatureType(val color: String, val dropGlow: Boolean = false) {

    DEFAULT("§f"), SUPPORT("§a"), NPC("§a"), BOSS("§c", true);

}

enum class RpgCreatureStatistic(val defaults: Any, var mapper: MutableMap<RpgEntityStatistic, Int>.(Any) -> Unit) {

    LEVEL(1, { put(RpgEntityStatistic.LEVEL, it.cast()) }),
    EXP(0, { put(RpgEntityStatistic.EXP, it.cast()) }),
    HEALTH(20, {
        put(RpgEntityStatistic.HEALTH, it.cast())
        put(RpgEntityStatistic.HEALTH_MAX, it.cast())
    }),
    MANA(20, {
        put(RpgEntityStatistic.MANA, it.cast())
        put(RpgEntityStatistic.MANA_MAX, it.cast())
    }),
    DAMAGE(0 to 0, {
        put(RpgEntityStatistic.DAMAGE, it.cast<Pair<Int, Int>>().min)
        put(RpgEntityStatistic.DAMAGE_MAX, it.cast<Pair<Int, Int>>().max)
    }),
    DAMAGE_MAGIC(0 to 0, {
        put(RpgEntityStatistic.DAMAGE_MAGIC, it.cast<Pair<Int, Int>>().min)
        put(RpgEntityStatistic.DAMAGE_MAGIC_MAX, it.cast<Pair<Int, Int>>().max)
    }),
    REGENERATION(2, { put(RpgEntityStatistic.REGENERATION, it.cast()) }),
    MANA_REGENERATION(1, { put(RpgEntityStatistic.MANA_REGENERATION, it.cast()) }),
    RESISTANCE(0, { put(RpgEntityStatistic.RESISTANCE, it.cast()) }),
    ARMOR(0, { put(RpgEntityStatistic.ARMOR, it.cast()) }),
    ARMOR_MAGIC(0, { put(RpgEntityStatistic.ARMOR_MAGIC, it.cast()) }),
    SPEED(1.0, {  }),
    CRIT(0, { put(RpgEntityStatistic.CRIT, it.cast()) }),
    VAMPIRING(0, { put(RpgEntityStatistic.VAMPIRING, it.cast()) }),

    SMALL(false, {}),
    ATTACK_SPEED(3.0, {}),
    FREE_DISTANCE(5.0, {}), //дистанция ходьбы от спавна \ овнера
    STRICT_DISTANCE(35.0, {}), //дистанция за которой моба тепнет назад
    ATTACK_DISTANCE(3.0, {}), //дистанция за которой моб атакует
    DETECT_DISTANCE(20.0, {}), //дистанция на которой видно игрока\дамагера
    ;

    companion object {

        fun getCreatureStatistics(resultSet: ResultSet): Map<RpgCreatureStatistic, Any> = enumValues<RpgCreatureStatistic>().associateWith {
            val value = resultSet.getObject(it.name.lowercase())
            when {
                value == null || value is String && value.isBlank() -> it.defaults
                value is String -> value.cast<String>().split('-', limit = 2).map(String::toInt).let { it[0] to it[1] }
                it.defaults is Boolean -> value == true || value == 1
                else -> value
            }
        }.filterNot { it.value == null }.cast()

    }

}
