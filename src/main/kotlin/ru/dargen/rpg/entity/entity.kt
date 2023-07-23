package ru.dargen.rpg.entity

import io.netty.util.internal.ConcurrentSet
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import ru.dargen.rpg.creature.RpgCreature
import ru.dargen.rpg.entity.combat.RpgEntityCombatState
import ru.dargen.rpg.entity.combat.RpgEntityDamageEvent
import ru.dargen.rpg.item.RpgItem
import ru.dargen.rpg.util.*
import ru.dargen.rpg.util.minecraft.soundOf
import ru.dargen.rpg.util.random.percentRandomSuccess
import ru.dargen.rpg.util.random.randomInt
import kotlin.math.max
import kotlin.math.min

abstract class RpgEntity<H : LivingEntity>(
    open val handle: H,
    open val statistics: MutableMap<RpgEntityStatistic, Int>,
    open val combatState: RpgEntityCombatState = RpgEntityCombatState()
) {

    open val entityId get() = handle.entityId
    open val entityType get() = handle.type
    open val entity get() = handle.minecraftEntity

    open val isCombat get() = combatState.isCombat
    open val location get() = handle.location
    open val supports: MutableSet<RpgCreature> = ConcurrentSet()

    open val level: Int get() = RpgEntityStatistic.LEVEL.get()
    open val exp: Int get() = RpgEntityStatistic.EXP.get()

    open val maxHealth: Int get() = RpgEntityStatistic.HEALTH_MAX.get()
    open var health: Int
        get() = RpgEntityStatistic.HEALTH.get()
        set(value) {
            val newHealth = min(max(0, value), maxHealth)
            RpgEntityStatistic.HEALTH.set(newHealth)
            if (newHealth == 0) {
                val event = combatState.lastDamageEvent?.takeIf { combatState.isCombat }
                death(event)
                event?.damager?.onKill(event)
            }
        }

    open val maxMana: Int get() = RpgEntityStatistic.MANA_MAX.get()
    open var mana: Int
        get() = RpgEntityStatistic.MANA.get()
        set(value) {
            val newMana = min(max(0, value), maxMana)
            RpgEntityStatistic.MANA.set(newMana)
        }

    open val armor: Int get() = RpgEntityStatistic.ARMOR.get()
    open val magicArmor: Int get() = RpgEntityStatistic.ARMOR_MAGIC.get()

    open val damage: Int get() = randomInt(RpgEntityStatistic.DAMAGE.get(), RpgEntityStatistic.DAMAGE_MAX.get())
    open val magicDamage: Int get() = randomInt(RpgEntityStatistic.DAMAGE_MAGIC.get(), RpgEntityStatistic.DAMAGE_MAGIC_MAX.get())
    open val crit: Int get() = RpgEntityStatistic.CRIT.get()
    open val vampiring: Int get() = RpgEntityStatistic.VAMPIRING.get()

    fun damageHealth(damage: Int, event: RpgEntityDamageEvent? = null) {
//        if (damage == 0) return
        combatState.lastDamageEvent = event
        health -= (damage * if (event?.isCrit == true) 2 else 1)

        playDamage()
        playDamageSound()
        if (event?.isCrit == true) playCritSound()

        location.damageHologram(damage * if (event?.isCrit == true) 2 else 1, event?.isCrit ?: false, 45)
    }

    fun damage(
        damager: RpgEntity<*>? = null, weapon: RpgItem? = null,
        damage: Int = 0, magicDamage: Int = 0,
        editor: ((RpgEntityDamageEvent) -> Unit)? = null
    ) {
        if (damager === this) return

        val event = RpgEntityDamageEvent(this, damager, weapon, damage, magicDamage).apply { editor?.invoke(this) }

        if (event.damageComputing) damager?.let {
            event.damage += it.damage
            event.magicDamage += it.magicDamage
        }

        weapon?.run {
            event.damage += this.damage
            event.magicDamage += this.magicDamage
            scripts.forEach { it.damageDeal(event) }
            if (event.isCancelled) return
        }

        if (event.critComputing && ((damager?.crit ?: -1) + (weapon?.crit ?: -1)).percentRandomSuccess)
            event.isCrit = true

        if (event.armorComputing) {
            event.damage = (event.damage * (1.0 - 0.5 * min(1.0, armor / (level * 20.0)))).toInt()
            event.magicDamage = (event.magicDamage * (1.0 - 0.5 * min(1.0, magicArmor / (level * 20.0)))).toInt()
        }

        damager?.damageDeal(event)
        if (event.isCancelled) return

        if (event.buffComputing) {
            /*TODO: buff computing*/
            if (event.isCancelled) return
        }

        damageReceive(event)
        if (event.isCancelled) return

        val finalDamage = event.damage + event.magicDamage
        val vampired = (((damager?.vampiring ?: 0) + (weapon?.vampiring ?: 0)) / 100.0 * finalDamage).toInt()
        damager?.health = (damager?.health ?: 0) + vampired

        damageHealth(finalDamage, event)
        damager?.combatState?.lastAttackedEntity = this

        event.damager?.let {
            addSupportThreat(it, randomInt(1, 3))
            it.addSupportThreat(this, randomInt(1, 3))
        }
    }

    abstract fun onSpawn()

    abstract fun onRemove()

    abstract fun tick(tick: Int)

    abstract fun tickAsync(tick: Int)

    abstract fun damageReceive(event: RpgEntityDamageEvent)

    abstract fun damageDeal(event: RpgEntityDamageEvent)

    abstract fun onKill(event: RpgEntityDamageEvent)

    abstract fun death(event: RpgEntityDamageEvent? = null)

    fun addSupportThreat(threat: RpgEntity<*>, points: Int) = supports.forEach { it.addThreat(threat, points) }

    fun playNearSound(sound: Sound) =
        handle.location.getNearPlayers(45).forEach { it.playSound(handle.location, sound, 1f, 1f) }

    fun playDamageSound() = playNatureSound("HURT")

    fun playCritSound() = playNearSound(Sound.ENTITY_PLAYER_ATTACK_CRIT)

    fun playDeathSound() = playNatureSound("DEATH")

    fun playNatureSound(type: String) = entityType.soundOf(type)?.let(this::playNearSound)

    fun playDamage() = handle.playDamage(45)

    open fun hasStatistic(statistic: RpgEntityStatistic) = statistic in statistics && statistic.get() > 0

    open fun getStatistic(statistic: RpgEntityStatistic) = (statistics[statistic] ?: statistic.defaults)

    open fun mergeStatistic(statistic: RpgEntityStatistic, value: Int, merger: (old: Int, new: Int) -> Int) =
        statistics.merge(statistic, value) { old, new -> merger(old, new) }

    open fun setStatistic(statistic: RpgEntityStatistic, value: Int) = statistics.put(statistic, value)

    protected val RpgEntityStatistic.exists get() = hasStatistic(this)

    protected fun RpgEntityStatistic.get() = getStatistic(this)

    protected fun RpgEntityStatistic.set(value: Int) = setStatistic(this, value)

}


