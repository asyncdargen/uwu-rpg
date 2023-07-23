package ru.dargen.rpg.entity.combat

import ru.dargen.rpg.entity.RpgEntity
import java.time.Duration
import java.time.Instant

val CombatTimeout = Duration.ofSeconds(10)!!

open class RpgEntityCombatState {

    open var lastAttackTimestamp: Instant = Instant.MIN
    open var lastDamageTimestamp: Instant = Instant.MIN

    open var lastAttackedEntity: RpgEntity<*>? = null
        set(value) = value.let {
            field = value
            lastAttackTimestamp = if (it == null) Instant.MIN else Instant.now()
        }
    open var lastDamagerEntity: RpgEntity<*>? = null
        set(value) = value.let {
            field = value
            lastDamageTimestamp = if (it == null) Instant.MIN else Instant.now()
        }

    open var lastDamageEvent: RpgEntityDamageEvent? = null
        set(value) {
            field = value
            if (value != null) value.damager?.let { lastDamagerEntity = it }
        }

    open val isCombat: Boolean
        get() = (lastAttackedEntity != null && Duration.between(lastAttackTimestamp, Instant.now()) <= CombatTimeout)
                || (lastDamagerEntity != null && Duration.between(lastDamageTimestamp, Instant.now()) <= CombatTimeout)

    open fun reset() {
        lastAttackedEntity = null
        lastDamagerEntity = null
    }

}