package ru.dargen.rpg.region

import net.minecraft.server.v1_12_R1.MinecraftServer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import ru.dargen.rpg.*
import ru.dargen.rpg.entity.RpgEntity
import ru.dargen.rpg.item.script.RpgItemScript
import ru.dargen.rpg.player.RpgPlayer
import ru.dargen.rpg.region.script.RpgRegionScript
import ru.dargen.rpg.region.types.RpgRegionFlag
import ru.dargen.rpg.util.*
import ru.dargen.rpg.util.rpg.RpgLoaderLock
import ru.starfarm.core.database.query.DatabaseRowType
import ru.starfarm.core.database.query.row.TypedQueryRow
import ru.starfarm.core.util.Internals
import ru.starfarm.core.util.bukkit.LocationUtil
import ru.starfarm.core.util.math.Cuboid
import java.sql.ResultSet

object RpgRegionRegistry {

    val RegionMap: MutableMap<Short, RpgRegion> = HashMap()

    private val Scripts: Map<String, RpgRegionScript> = Internals.findClasses(
        classLoader = Rpg.javaClass.classLoader,
        `package` = "ru.dargen.rpg.item.region",
        superClass = RpgItemScript::class.java
    ).filterIsInstance<Class<out RpgRegionScript>>()
        .mapNotNull(RpgRegionScript::resolveScript)
        .associateBy(RpgRegionScript::name)

    init {
        Tasks.every(1, 1) {
            val tick = MinecraftServer.currentTick
            RegionMap.values.forEach { region -> region.scripts.forEach { it.tick(region, tick) } }
        }
        Events.onListeners(RpgRegionListener)

        DatabaseConnection.newDatabaseQuery("rpg_regions").createTableQuery().apply {
            queryRow(
                TypedQueryRow("id", DatabaseRowType.SMALL_INT)
                    .index(TypedQueryRow.IndexType.PRIMARY)
                    .index(TypedQueryRow.IndexType.AUTO_INCREMENT)
            )
            queryRow(TypedQueryRow("name", DatabaseRowType.VAR_CHAR))
            queryRow(TypedQueryRow("levels", DatabaseRowType.VAR_CHAR))
            queryRow(TypedQueryRow("cuboid", DatabaseRowType.VAR_CHAR).index(TypedQueryRow.IndexType.NOT_NULL))
            queryRow(TypedQueryRow("grave", DatabaseRowType.VAR_CHAR))
            queryRow(TypedQueryRow("flags", DatabaseRowType.VAR_CHAR))
            queryRow(TypedQueryRow("scripts", DatabaseRowType.VAR_CHAR))
            queryRow(TypedQueryRow("metadata", DatabaseRowType.VAR_CHAR))
        }.executeSync(DatabaseConnection)

        RpgLoaderLock + Database.query("SELECT * FROM `rpg_regions`;")
            .thenAccept { it.forEach { it.load() } }
            .thenAccept {
                Logger.info("Loaded ${Scripts.size} region scripts")
                Logger.info("Loaded ${RegionMap.size} regions")
            }
    }

    private fun ResultSet.load() {
        val id = getShort("id")
        val name = getString("name")?.colored ?: "§b$id"
        val levels = getString("levels")
            ?.split('-', limit = 2)
            ?.let { it[0].toInt()..it[1].toInt() } ?: 0..0
        val cuboid = getString("cuboid")
            .split(' ')
            .run {
                Cuboid.atCoordinates(
                    Bukkit.getWorld(get(0)),

                    getOrNull(1)?.toDouble() ?: Int.MIN_VALUE.toDouble(),
                    Int.MIN_VALUE.toDouble(),
                    getOrNull(2)?.toDouble() ?: Int.MIN_VALUE.toDouble(),

                    getOrNull(3)?.toDouble() ?: Int.MAX_VALUE.toDouble(),
                    Int.MAX_VALUE.toDouble(),
                    getOrNull(4)?.toDouble() ?: Int.MAX_VALUE.toDouble(),
                )
            }
        val flags = getString("flags")
            ?.split(',')
            ?.map(String::trim)
            ?.map(RpgRegionFlag::valueOf)
            ?.toSet() ?: emptySet()
        val scripts = getString("scripts")
            ?.takeIf(String::isNotBlank)
            ?.split(",")
            ?.map(String::trim)
            ?.map(RpgRegionRegistry::getScript)
            ?.toSet() ?: emptySet()
        val metadata = getMap("metadata")
        val spawn = getString("grave")?.let(LocationUtil::fromString)

        RegionMap[id] = RpgRegion(id, name, levels, cuboid, flags, spawn, metadata.toMutableMap(), scripts)
    }

    fun onRegionChanged(region: RpgRegion, oldRegion: RpgRegion, player: RpgPlayer) {
        if (oldRegion.id == region.id) return
        region.scripts.forEach { it.join(region, oldRegion, player) }
        oldRegion.scripts.forEach { it.quit(oldRegion, region, player) }

        if (region.id != 0.toShort() && oldRegion.name != region.name)
            player.sendMessage("§aВы вошли в ${region.name} ${region.minLevel.formatLevel(player)}§7-${region.maxLevel.formatLevel(player)}")

        if (RpgRegionFlag.PVP in region != RpgRegionFlag.PVP in oldRegion) {
            if (RpgRegionFlag.PVP in region) player.sendMessage("§cВы §lпокинули §cбезопасную зону!")
            else player.sendMessage("§aВы §lвошли §aв безопасную зону.")
        }
    }

    fun pull(vararg ids: Int) =
        Database.query("SELECT * FROM `rpg_regions` WHERE ${ids.joinToString(" OR `id` = ", prefix = "`id` = ")};")
            .thenAccept { it.forEach { it.load() } }

    operator fun get(id: Short) = RegionMap[id.toShort()] ?: throw java.lang.IllegalStateException("Unknown region ($id) - ${RegionMap}")

    fun getScript(name: String) = Scripts[name] ?: throw IllegalStateException("Unknown region script ($name)")

    fun getRegion(location: Location) = RegionMap.values.last { location in it }

    fun getRegion(entity: Entity) = RegionMap.values.last { entity in it }

    fun getRegion(block: Block) = RegionMap.values.last { block in it }

    fun getRegion(entity: RpgEntity<*>) = RegionMap.values.last { entity in it }

    fun getNearGrave(location: Location) = RegionMap.values
        .filterNot { it.grave == null }
        .minByOrNull { it.grave!!.distanceTo(location) }
        ?.grave ?: RegionMap.values.first { it.grave != null }.grave!!

}