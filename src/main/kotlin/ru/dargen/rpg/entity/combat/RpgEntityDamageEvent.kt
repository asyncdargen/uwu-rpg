package ru.dargen.rpg.entity.combat

import ru.dargen.rpg.entity.RpgEntity
import ru.dargen.rpg.item.RpgItem

data class RpgEntityDamageEvent(
    val damaged: RpgEntity<*>,
    val damager: RpgEntity<*>? = null,
    val weapon: RpgItem? = null,
    var damage: Int = 0, var magicDamage: Int = 0
) {

    var isCancelled: Boolean = false

    var damageComputing: Boolean = true
    var armorComputing: Boolean = true
    var critComputing: Boolean = true

    var isCrit: Boolean = false

    var buffComputing: Boolean = true
    var talentComputing: Boolean = true

    fun disableBuffComputation() = apply { buffComputing = false }

    fun disableTalentComputation() = apply { talentComputing = false }

    fun disableCrit() = apply { critComputing = false }

    fun disableArmor() = apply { armorComputing = false }

    fun disableDamage() = apply { armorComputing = false }

    fun crit() = apply { isCrit = true }

    fun cancel() = apply { isCancelled = true }

}