package ru.dargen.rpg.spell

import ru.dargen.rpg.spell.type.RpgSpellKey
import ru.dargen.rpg.util.enumValueOn

@JvmInline
value class RpgSpellCombination(val combination: Int = 0) {

    val isFull get() = length == 4
    val isEmpty get() = length == 0

    val length get() = (0..3).firstOrNull { (combination shr (it * 2)) and 0x3 != 0 }?.let { 4 - it } ?: 0
    val available get() = 4 - length

    val keys
        get() = (0 until length).map { enumValueOn<RpgSpellKey>(((combination shr ((3 - it) * 2)) and 0x3) - 1) }

    fun withKey(key: RpgSpellKey): RpgSpellCombination {
        if (isFull) return this
        return RpgSpellCombination((combination or (((key.ordinal + 1) and 0x3) shl ((available - 1) * 2))))
    }

    fun startsWith(combination: RpgSpellCombination): Boolean {
        val bits = combination.available * 2

        return (this.combination shr bits) xor (combination.combination shr bits) == 0
    }

}
