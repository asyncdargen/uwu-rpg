package ru.dargen.rpg.player.combat

import ru.dargen.rpg.entity.RpgEntity
import ru.dargen.rpg.entity.combat.CombatTimeout
import ru.dargen.rpg.entity.combat.RpgEntityCombatState
import ru.dargen.rpg.player.RpgPlayer
import java.time.Duration
import java.time.Instant

class RpgPlayerCombatState : RpgEntityCombatState() {

    private var previousPvpState: Boolean = false
    var lastPvpTimestamp: Instant = Instant.MIN

    override var lastAttackedEntity: RpgEntity<*>?
        get() = super.lastAttackedEntity
        set(value) {
            super.lastAttackedEntity = value
            updatePvp(value)
        }
    override var lastDamagerEntity: RpgEntity<*>?
        get() = super.lastDamagerEntity
        set(value) {
            super.lastDamagerEntity = value
            updatePvp(value)
        }

    val isPvpState: Pair<Boolean, Boolean>
        get() {
            try {
                return (previousPvpState != isPvp) to isPvp
            } finally {
                previousPvpState = isPvp
            }
        }

    fun updatePvp(damager: RpgEntity<*>?) {
        if (damager is RpgPlayer) lastPvpTimestamp = Instant.now()
        else if (damager == null) lastPvpTimestamp = Instant.MIN
    }

    val isPvp: Boolean get() = Duration.between(lastPvpTimestamp, Instant.now()) <= CombatTimeout

}