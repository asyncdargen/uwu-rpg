package ru.dargen.rpg.item

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import org.bukkit.inventory.ItemStack
import ru.dargen.rpg.*
import ru.dargen.rpg.event.listeners.ArmorUpdateListener
import ru.dargen.rpg.item.script.RpgItemScript
import ru.dargen.rpg.item.types.RpgItemAttribute
import ru.dargen.rpg.util.colored
import ru.dargen.rpg.util.forEach
import ru.dargen.rpg.util.getItemStack
import ru.dargen.rpg.util.getMap
import ru.dargen.rpg.util.rpg.RpgLoaderLock
import ru.starfarm.core.database.query.DatabaseRowType
import ru.starfarm.core.database.query.row.TypedQueryRow
import ru.starfarm.core.util.Internals
import ru.starfarm.core.util.item.allFlags
import ru.starfarm.core.util.item.tagValue
import ru.starfarm.core.util.item.unbreakable
import java.sql.ResultSet
import java.util.concurrent.TimeUnit

val ItemStack?.asRpg
    get() = this?.tagValue<Long>("rpg")
        ?.let(::RpgItemPrototype)
        ?.takeIf { it.id in RpgItemRegistry }
        ?.let(RpgItemRegistry::get)

object RpgItemRegistry {

    val ItemMap: MutableMap<Short, RpgItem> = HashMap()

    private val Scripts: Map<String, RpgItemScript> = Internals.findClasses(
        classLoader = Rpg.javaClass.classLoader,
        `package` = "ru.dargen.rpg.item.script",
        superClass = RpgItemScript::class.java
    ).filterIsInstance<Class<out RpgItemScript>>()
        .mapNotNull(RpgItemScript::resolveScript)
        .associateBy(RpgItemScript::name)

    private val ItemPrototypeCache: LoadingCache<RpgItemPrototype, RpgItem> = CacheBuilder.newBuilder()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .build(object : CacheLoader<RpgItemPrototype, RpgItem>() {
            override fun load(prototype: RpgItemPrototype) = ItemMap[prototype.id]?.run {
                if (prototype.isEmpty) this else RpgItem(
                    prototype,
                    baseItemStack, display,
                    metadata, scripts,
                    RpgItem.recomputeAttributes(attributes.toMutableMap(), prototype)
                )
            }
        })

    //Startup loading
    init {
        Events.onListeners(ArmorUpdateListener, RpgItemListener)

        DatabaseConnection.newDatabaseQuery("rpg_items").createTableQuery().apply {
            queryRow(
                TypedQueryRow("id", DatabaseRowType.SMALL_INT)
                    .index(TypedQueryRow.IndexType.PRIMARY)
                    .index(TypedQueryRow.IndexType.AUTO_INCREMENT)
            )
            queryRow(TypedQueryRow("material", DatabaseRowType.VAR_CHAR).index(TypedQueryRow.IndexType.NOT_NULL))
            queryRow(TypedQueryRow("display", DatabaseRowType.TEXT).index(TypedQueryRow.IndexType.NOT_NULL))

            enumValues<RpgItemAttribute>().forEach {
                queryRow(TypedQueryRow(it.name.lowercase(), if (it.defaults is Number) DatabaseRowType.INT else DatabaseRowType.VAR_CHAR))
            }

            queryRow(TypedQueryRow("scripts", DatabaseRowType.VAR_CHAR))
            queryRow(TypedQueryRow("metadata", DatabaseRowType.VAR_CHAR))
        }.executeSync(DatabaseConnection)

        RpgLoaderLock + Database.query("SELECT * FROM `rpg_items`;")
            .thenAccept { it.forEach { it.load() } }
            .thenAccept {
                Logger.info("Loaded ${ItemMap.size} items")
                Logger.info("Loaded ${Scripts.size} item scripts")
            }
    }

    private fun ResultSet.load() {
        val id = getShort("id")
        val baseItemStack = getItemStack("material").allFlags().unbreakable(true)
        val display = getString("display").colored
            .split("\r\n", "\n", "\r", limit = 2)
            .run { get(0).colored to (getOrNull(1)?.lines()?.map("§7"::plus) ?: emptyList()) }
        val attributes = RpgItemAttribute.getItemAttributes(this).toMutableMap()
        val scripts = getString("scripts")
            ?.takeIf(String::isNotBlank)
            ?.split(",")
            ?.map(String::trim)
            ?.map(RpgItemRegistry::getScript)
            ?.toSet() ?: emptySet()
        val metadata = getMap("metadata")

        ItemMap[id] = RpgItem(itemPrototype(id), baseItemStack, display, metadata.toMutableMap(), scripts, attributes)
    }

    //Runtime additional loading
    fun pull(vararg ids: Int) =
        Database.query("SELECT * FROM `rpg_items` WHERE ${ids.joinToString(" OR `id` = ", prefix = "`id` = ")};")
            .thenAccept {
                ItemPrototypeCache.invalidateAll()
                it.forEach { it.load() }
            }

    fun getScript(name: String) = Scripts[name] ?: throw IllegalStateException("Unknown item script ($name)")

    operator fun get(prototype: RpgItemPrototype) = ItemPrototypeCache.takeIf { prototype.id in this }
        ?.get(prototype) ?: throw IllegalStateException("Unknown item (${prototype.id})")

    operator fun get(id: Short, mark: Byte = 0, enchant: Byte = 0) = get(itemPrototype(id, mark, enchant))

    operator fun contains(id: Short) = id in ItemMap

}
