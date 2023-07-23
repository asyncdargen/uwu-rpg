package ru.dargen.rpg.creature

import com.comphenix.protocol.ProtocolLibrary
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import ru.dargen.rpg.*
import ru.dargen.rpg.creature.disguise.RpgDisguiseAdapter
import ru.dargen.rpg.creature.minecraft.bridge.RpgEntityBridge
import ru.dargen.rpg.creature.preset.RpgCreatureDrop
import ru.dargen.rpg.creature.preset.RpgCreaturePreset
import ru.dargen.rpg.creature.scripts.RpgCreatureScript
import ru.dargen.rpg.creature.types.RpgCreatureStatistic
import ru.dargen.rpg.creature.types.RpgCreatureType
import ru.dargen.rpg.entity.asRpg
import ru.dargen.rpg.util.colored
import ru.dargen.rpg.util.forEach
import ru.dargen.rpg.util.getMap
import ru.dargen.rpg.util.random.RandomPercentList
import ru.dargen.rpg.util.rpg.RpgLoaderLock
import ru.starfarm.core.database.query.DatabaseRowType
import ru.starfarm.core.database.query.row.TypedQueryRow
import ru.starfarm.core.util.Internals
import ru.starfarm.core.util.cast
import java.sql.ResultSet


val Entity.asRpgCreature get() = asRpg?.takeIf { it is RpgCreature }?.cast<RpgCreature>()

object RpgCreatureRegistry {

    val PresetMap = HashMap<Short, RpgCreaturePreset>()

    private val Scripts: Map<String, RpgCreatureScript> = Internals.findClasses(
        classLoader = Rpg.javaClass.classLoader,
        `package` = "ru.dargen.rpg.creature.script",
        superClass = RpgCreatureScript::class.java
    ).filterIsInstance<Class<out RpgCreatureScript>>()
        .mapNotNull(RpgCreatureScript::resolveScript)
        .associateBy(RpgCreatureScript::name)

    init {
        RpgEntityBridge
        ProtocolLibrary.getProtocolManager().addPacketListener(RpgDisguiseAdapter)

        Events.onListeners(RpgCreatureListener)

        DatabaseConnection.newDatabaseQuery("rpg_entities").createTableQuery().apply {
            queryRow(
                TypedQueryRow("id", DatabaseRowType.SMALL_INT)
                    .index(TypedQueryRow.IndexType.PRIMARY)
                    .index(TypedQueryRow.IndexType.AUTO_INCREMENT)
            )
            queryRow(TypedQueryRow("name", DatabaseRowType.VAR_CHAR))
            queryRow(TypedQueryRow("disguise", DatabaseRowType.VAR_CHAR))
            queryRow(TypedQueryRow("entity_type", DatabaseRowType.VAR_CHAR).index(TypedQueryRow.IndexType.NOT_NULL))
            queryRow(TypedQueryRow("type", DatabaseRowType.VAR_CHAR))

            enumValues<RpgCreatureStatistic>().forEach {
                queryRow(
                    TypedQueryRow(
                        it.name.lowercase(),
                        when (it.defaults) {
                            is Int -> DatabaseRowType.INT
                            is Double -> DatabaseRowType.DOUBLE
                            is Boolean -> DatabaseRowType.TINY_INT
                            else -> DatabaseRowType.VAR_CHAR
                        }
                    )
                )
            }

            queryRow(TypedQueryRow("drop", DatabaseRowType.TEXT))
            queryRow(TypedQueryRow("targets", DatabaseRowType.TEXT))
            queryRow(TypedQueryRow("scripts", DatabaseRowType.VAR_CHAR))
            queryRow(TypedQueryRow("metadata", DatabaseRowType.VAR_CHAR))
        }.executeSync(DatabaseConnection)

        RpgLoaderLock + Database.query("SELECT * FROM `rpg_entities`;")
            .thenAccept { it.forEach { it.load() } }
            .thenAccept {
                Logger.info("Loaded ${PresetMap.size} creature presets")
                Logger.info("Loaded ${Scripts.size} creature scripts")
            }
    }

    private fun ResultSet.load() {
        val id = getShort("id")
        val name = getString("name")?.colored ?: "$id"
        val disguise = getString("disguise")
        val entityType = enumValueOf<EntityType>(getString("entity_type"))
        val type = getString("type")?.let(RpgCreatureType::valueOf) ?: RpgCreatureType.DEFAULT
        val statistics = RpgCreatureStatistic.getCreatureStatistics(this)
        val targets = getString("targets")
            ?.takeIf(String::isNotBlank)
            ?.split(",")
            ?.map(String::trim)
            ?.map(EntityType::valueOf)
            ?.toList() ?: emptyList()
        val scripts = getString("scripts")
            ?.takeIf(String::isNotBlank)
            ?.split(",")
            ?.map(String::trim)
            ?.map(RpgCreatureRegistry::getScript)
            ?.toSet() ?: emptySet()
        val drop = (getString("drop")
            ?.takeIf(String::isNotBlank)
            ?.split(",")
            ?.map(String::trim)
            ?.map(RpgCreatureDrop::valueOf)
            ?.toList() ?: emptyList()).let { RandomPercentList(RpgCreatureDrop::chance, it) }
        val metadata = getMap("metadata")

        PresetMap[id] = RpgCreaturePreset(id, name, disguise, entityType, type, statistics, drop, targets, scripts, metadata.toMutableMap())
    }

    //Runtime additional loading
    fun pull(vararg ids: Int) =
        Database.query("SELECT * FROM `rpg_entities` WHERE ${ids.joinToString(" OR `id` = ", prefix = "`id` = ")};")
            .thenAccept {
                it.forEach { it.load() }
            }

    fun getScript(name: String) = Scripts[name] ?: throw IllegalStateException("Unknown entity script ($name)")

    operator fun get(id: Short) = PresetMap.takeIf { id in this }?.get(id) ?: throw IllegalStateException("Unknown entity preset (${id})")

    operator fun contains(id: Short) = id in PresetMap


}