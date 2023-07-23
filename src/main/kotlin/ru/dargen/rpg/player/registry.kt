package ru.dargen.rpg.player

import org.bukkit.entity.Entity
import org.bukkit.event.player.PlayerJoinEvent
import ru.dargen.rpg.Database
import ru.dargen.rpg.DatabaseConnection
import ru.dargen.rpg.Events
import ru.dargen.rpg.Logger
import ru.dargen.rpg.entity.RpgEntityRegistry
import ru.dargen.rpg.entity.RpgEntityStatistic
import ru.dargen.rpg.entity.asRpg
import ru.dargen.rpg.spell.RpgSpellCombination
import ru.dargen.rpg.spell.RpgSpellListener
import ru.dargen.rpg.spell.type.RpgSpell
import ru.dargen.rpg.spell.type.RpgSpellKey
import ru.dargen.rpg.util.forEach
import ru.dargen.rpg.util.rpg.RpgLoaderLock
import ru.starfarm.core.database.query.DatabaseRowType
import ru.starfarm.core.database.query.row.TypedQueryRow
import ru.starfarm.core.event.GlobalEventContext.on
import ru.starfarm.core.util.cast
import java.util.*

val Entity.asRpgPlayer get() = asRpg?.takeIf { it is RpgPlayer }?.cast<RpgPlayer>()

object RpgPlayerRegistry {

    val LevelMap = HashMap<Int/*level*/, Int/*exp*/>()

    init {
        Events.onListeners(RpgPlayerListener, RpgSpellListener)

        DatabaseConnection.newDatabaseQuery("rpg_player_levels").createTableQuery().apply {
            queryRow(
                TypedQueryRow("level", DatabaseRowType.INT)
                    .index(TypedQueryRow.IndexType.PRIMARY)
                    .index(TypedQueryRow.IndexType.AUTO_INCREMENT)
            )
            queryRow(TypedQueryRow("exp", DatabaseRowType.INT).index(TypedQueryRow.IndexType.NOT_NULL))
        }.executeSync(DatabaseConnection)

        RpgLoaderLock + Database.query("SELECT * FROM `rpg_player_levels`;")
            .thenAccept { it.forEach { LevelMap[getInt("level")] = getInt("exp") } }
            .thenAccept { Logger.info("Loaded ${LevelMap.size} player levels")}

        Events.on<PlayerJoinEvent> {
            RpgEntityRegistry.addRpgEntity(RpgPlayer(player, EnumMap(RpgEntityStatistic::class.java))
                .apply {
                    spellCombinations[RpgSpellCombination().withKey(RpgSpellKey.F)] = RpgSpell.JUMP
                })
        }
    }

    fun getNextLevelExp(level: Int) = LevelMap[level + 1] ?: -1

}