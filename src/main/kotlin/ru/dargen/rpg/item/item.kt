package ru.dargen.rpg.item

import org.bukkit.inventory.ItemStack
import ru.dargen.rpg.entity.RpgEntityStatistic
import ru.dargen.rpg.item.script.RpgItemScript
import ru.dargen.rpg.item.types.RpgItemAttribute
import ru.dargen.rpg.item.types.RpgItemBound
import ru.dargen.rpg.item.types.RpgItemMark
import ru.dargen.rpg.item.types.RpgItemQuality
import ru.dargen.rpg.player.RpgPlayer
import ru.dargen.rpg.util.*
import ru.dargen.rpg.util.random.random
import ru.dargen.rpg.util.rpg.RpgMetaObject
import ru.starfarm.core.util.cast
import ru.starfarm.core.util.item.addLore
import ru.starfarm.core.util.item.name
import ru.starfarm.core.util.item.tag

typealias RpgItemAttributes = Map<RpgItemAttribute, Any>

data class RpgItem(
    val prototype: RpgItemPrototype,
    val baseItemStack: ItemStack,
    val display: Pair<String, List<String>>,
    override val metadata: MutableMap<String, Any?>,
    val scripts: Set<RpgItemScript>,
    val attributes: RpgItemAttributes
) : RpgMetaObject {

    val id by prototype::id
    val isOrigin by prototype::isEmpty
    val origin get() = if (isOrigin) this else withPrototype(mark = 0, enchant = 0)

    val enchant by prototype::enchant
    val mark by prototype::enchant
    val markType get() = enumValueOn<RpgItemMark>(mark.toInt())

    val level: Int get() = RpgItemAttribute.LEVEL.get()
    val requiredLevel: Int get() = if (RpgItemAttribute.REQUIRED_LEVEL.exists) 1 else RpgItemAttribute.REQUIRED_LEVEL.get()
    val quality: RpgItemQuality get() = RpgItemAttribute.QUALITY.get()
    val bound: RpgItemBound get() = RpgItemAttribute.BOUND.get()

    val crit: Int get() = RpgItemAttribute.CRIT.get()
    val vampiring: Int get() = RpgItemAttribute.VAMPIRING.get()
    val damage: Int get() = RpgItemAttribute.DAMAGE.get<Pair<Int, Int>>().random()
    val magicDamage: Int get() = RpgItemAttribute.DAMAGE_MAGIC.get<Pair<Int, Int>>().random()

    val isArmor get() = baseItemStack.isArmor
    val isWeapon get() = baseItemStack.isWeapon
    val isRemoteWeapon get() = baseItemStack.isRemoteWeapon || hasMetaValue("remote")

    init {
        scripts.forEach { it.apply(this) }
    }

    fun withPrototype(
        additionalEnchant: Byte = 0,
        mark: Byte = prototype.mark,
        enchant: Byte = (prototype.enchant + additionalEnchant).toByte()
    ) = withPrototype(prototype.copy(additionalEnchant, mark, enchant))

    fun withPrototype(prototype: RpgItemPrototype) = RpgItemRegistry[prototype]

    fun buildBase(amount: Int = 1) = baseItemStack.clone().apply { this.amount = amount }.tag("rpg", prototype.data)

    fun buildItem(player: RpgPlayer, amount: Int = 1) = baseItemStack.clone().apply {
        name = "${quality.color}${display.first} ${if (prototype.enchant > 0) "§c+${prototype.enchant}" else ""}"

        setAmount(amount)

        enumValues<RpgItemAttribute>()
            .filter(this@RpgItem::hasAttribute)
            .filterNot(RpgItemAttribute::hide)
            .forEach { addLore(it.asString); if (it == RpgItemAttribute.QUALITY) addLore("") }
        addLore("")
        if (display.second.isNotEmpty()) {
            addLore(display.second)
            addLore("")
        }

        if (requiredLevel != -1) addLore("${RpgItemAttribute.REQUIRED_LEVEL.asString} ${isAllowedToUse(player).asSymbol}")
        if (RpgItemAttribute.BOUND.exists) addLore(RpgItemAttribute.BOUND.asString)
        addLore("§8" + java.lang.Long.toHexString(prototype.data).uppercase())
    }.tag("rpg", prototype.data)

    fun isAllowedToUse(player: RpgPlayer) = !RpgItemAttribute.REQUIRED_LEVEL.exists
            || RpgItemAttribute.REQUIRED_LEVEL.get<Int>() <= player.getStatistic(RpgEntityStatistic.LEVEL)

    fun <T> getAttribute(attribute: RpgItemAttribute) = (attributes[attribute] ?: attribute.defaults).cast<T>()

    fun hasAttribute(attribute: RpgItemAttribute) = attribute in attributes

    private fun <T> RpgItemAttribute.get() = getAttribute<T>(this)

    private val RpgItemAttribute.exists get() = hasAttribute(this)

    private val RpgItemAttribute.asString get() = toString(get())

    companion object {

        fun recomputeAttributes(attributes: RpgItemAttributes, prototype: RpgItemPrototype): RpgItemAttributes {
            return attributes.mapValues { (type, value) ->
                when (type) {
                    RpgItemAttribute.DAMAGE, RpgItemAttribute.DAMAGE_MAGIC, RpgItemAttribute.HEALTH, RpgItemAttribute.CRIT, RpgItemAttribute.VAMPIRING -> {
                        if (value is Int) (value * (1.0f + 0.09f * prototype.enchant)).toInt()
                        else value.cast<Pair<Int, Int>>().let {
                            (it.min * (1.0f + 0.09f * prototype.enchant)).toInt() to (it.max * (1.0f + 0.09f * prototype.enchant)).toInt()
                        }
                    }

                    RpgItemAttribute.ARMOR, RpgItemAttribute.ARMOR_MAGIC -> (value.cast<Int>() * (1.0f + 0.057f * prototype.enchant)).toInt()
                    RpgItemAttribute.RESISTANCE, RpgItemAttribute.REGENERATION -> (value.cast<Int>() * (1.0f + 0.02f * prototype.enchant)).toInt()
                    RpgItemAttribute.MANA, RpgItemAttribute.SPEED -> (value.cast<Int>() * (1.0f + 0.07f * prototype.enchant)).toInt()
                    else -> value
                }
            }
        }

    }

}

fun itemPrototype(id: Short, mark: Byte = 0, enchant: Byte = 0) =
    RpgItemPrototype((id.toLong() shl 16) or (mark.toLong() shl 8) or enchant.toLong())

@JvmInline
value class RpgItemPrototype(val data: Long) {

    val id get() = ((data shr 16) and 0xFFFF).toShort()

    val mark get() = ((data shr 8) and 0xFF).toByte()

    val enchant get() = (data and 0xFF).toByte()

    val isEmpty get() = mark == 0.toByte() && enchant == 0.toByte()

    fun copy(
        additionalEnchant: Byte = 0,
        mark: Byte = this.mark,
        enchant: Byte = (this.enchant + additionalEnchant).toByte()
    ) = itemPrototype(id, mark, enchant)

    companion object {

        fun fromString(string: String) =
            string.split('-', limit = 3).let { itemPrototype(it[0].toShort(), it[1].toByte(), it[2].toByte()) }

    }

    override fun toString() = "$id-$mark-$enchant"

}
