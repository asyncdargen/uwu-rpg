package ru.dargen.rpg.entity

import org.bukkit.inventory.EntityEquipment
import org.bukkit.inventory.ItemStack
import ru.dargen.rpg.item.RpgItem
import ru.dargen.rpg.item.types.RpgItemAttribute
import ru.dargen.rpg.util.max
import ru.dargen.rpg.util.min

enum class RpgEntityStatistic(
    val defaults: Int,
    val merger: (RpgItem.() -> Int)? = null
) {

    LEVEL(1), EXP(0),

    HEALTH(20), HEALTH_MAX(20, { getAttribute(RpgItemAttribute.HEALTH) }),
    REGENERATION(2, { getAttribute(RpgItemAttribute.REGENERATION) }),

    MANA(20), MANA_MAX(20, { getAttribute(RpgItemAttribute.MANA) }),
    MANA_REGENERATION(1, { getAttribute(RpgItemAttribute.REGENERATION_MANA) }),

    RESISTANCE(0, { getAttribute(RpgItemAttribute.RESISTANCE) }),
    ARMOR(0, { getAttribute(RpgItemAttribute.ARMOR) }),
    ARMOR_MAGIC(0, { getAttribute(RpgItemAttribute.ARMOR_MAGIC) }),

    DAMAGE(1, { getAttribute<Pair<Int, Int>>(RpgItemAttribute.DAMAGE).min }),
    DAMAGE_MAX(1, { getAttribute<Pair<Int, Int>>(RpgItemAttribute.DAMAGE).max }),

    DAMAGE_MAGIC(0, { getAttribute<Pair<Int, Int>>(RpgItemAttribute.DAMAGE_MAGIC).min }),
    DAMAGE_MAGIC_MAX(0, { getAttribute<Pair<Int, Int>>(RpgItemAttribute.DAMAGE_MAGIC).max }),

    SPEED(0, { getAttribute<Int>(RpgItemAttribute.SPEED) }),
    CRIT(0, { getAttribute(RpgItemAttribute.CRIT) }),
    VAMPIRING(0, { getAttribute(RpgItemAttribute.VAMPIRING) });

    val isMergeable get() = merger != null

}

enum class RpgCreatureEquipment(val applier: EntityEquipment.(ItemStack) -> Unit) {

    HAND(EntityEquipment::setItemInMainHand),
    OFF_HAND(EntityEquipment::setItemInOffHand),
    HELMET(EntityEquipment::setHelmet),
    CHEST_PLATE(EntityEquipment::setChestplate),
    LEGGINS(EntityEquipment::setLeggings),
    BOOTS(EntityEquipment::setBoots)

}