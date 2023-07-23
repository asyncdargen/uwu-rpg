package ru.dargen.rpg.creature

import com.google.common.cache.CacheBuilder
import net.minecraft.server.v1_12_R1.EntityInsentient
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityTargetEvent
import ru.dargen.rpg.Tasks
import ru.dargen.rpg.creature.disguise.RpgDisguiseData
import ru.dargen.rpg.creature.minecraft.bridge.RpgEntityBridge
import ru.dargen.rpg.creature.preset.RpgCreaturePreset
import ru.dargen.rpg.creature.types.RpgCreatureStatistic
import ru.dargen.rpg.creature.types.RpgCreatureType
import ru.dargen.rpg.entity.RpgEntity
import ru.dargen.rpg.entity.RpgEntityRegistry
import ru.dargen.rpg.entity.RpgEntityStatistic
import ru.dargen.rpg.entity.asRpg
import ru.dargen.rpg.entity.combat.RpgEntityDamageEvent
import ru.dargen.rpg.item.dropItems
import ru.dargen.rpg.player.RpgPlayer
import ru.dargen.rpg.player.asRpgPlayer
import ru.dargen.rpg.util.distanceTo
import ru.dargen.rpg.util.formatLevel
import ru.dargen.rpg.util.random.asColoredPercent
import ru.dargen.rpg.util.random.percentCeilTo
import ru.dargen.rpg.util.random.percentTo
import ru.starfarm.core.ApiManager
import ru.starfarm.core.hologram.Hologram
import ru.starfarm.core.util.cast
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class RpgCreature(val preset: RpgCreaturePreset, handle: LivingEntity) : RpgEntity<LivingEntity>(handle, preset.newStatistics) {

    var owner: RpgEntity<*>? = null
    var disguise: RpgDisguiseData? = null

    lateinit var hologram: Hologram
    val hologramLocation get() = location.add(.0, entity.headHeight + .48, .0)

    val bridge: RpgEntityBridge get() = entity.cast()
    override val entity: EntityInsentient get() = super.entity.cast()
    override val entityType
        get() = if (disguise != null) EntityType.PLAYER else preset.entityType

    val threatEntities = CacheBuilder.newBuilder()
        .expireAfterAccess(13, TimeUnit.SECONDS)
        .build<RpgEntity<*>, Int>()
    val damagerEntities = CacheBuilder.newBuilder()
        .expireAfterAccess(16, TimeUnit.SECONDS)
        .build<RpgEntity<*>, Int>()
    var target: RpgEntity<*>?
        get() = entity.cast<EntityInsentient>().goalTarget?.bukkitEntity?.asRpg
        set(value) {
            entity.cast<EntityInsentient>().setGoalTarget(value?.entity, EntityTargetEvent.TargetReason.RANDOM_TARGET, true)
        }

    override fun tick(tick: Int) {
        preset.scripts.forEach { it.tick(this, tick) }

        hologram.teleport(hologramLocation)

        if (tick % 20 == 0) {
            mana += RpgEntityStatistic.MANA_REGENERATION.get()
            if (!isCombat) {
                target = null
                threatEntities.invalidateAll()
                if (health != maxHealth) health += RpgEntityStatistic.REGENERATION.get()
            }
        }
    }

    override fun tickAsync(tick: Int) {
        preset.scripts.forEach { it.tickAsync(this, tick) }
        if (tick % 3 == 0) {
            val nameBase = "${preset.type.color}${preset.name} ${
                if (health == maxHealth) "§c$health❤"
                else "${health.percentCeilTo(maxHealth).toInt().asColoredPercent}%"
            }"

            hologram.players
                .mapNotNull(Player::asRpgPlayer)
                .forEach { hologram.getTextLine(0)?.entity?.setCustomName("§7[${level.formatLevel(it)}§7] $nameBase", it.handle) }
        }
    }

    fun interact(player: RpgPlayer) {
        preset.scripts.forEach { it.interact(this, player) }
    }

    fun wipe() {
        preset.scripts.forEach { it.wipe(this) }

        health = maxHealth
        mana = maxMana
        target = null
        combatState.reset()
        sendMessage("§cВы отвели моба слишком далеко от спавна, возвращаем его назад...")
        threatEntities.invalidateAll()
    }

    override fun onSpawn() {
        preset.scripts.forEach { it.onSpawn(this) }

        hologram = ApiManager.createHologram(hologramLocation).apply { textLine("") }
    }

    override fun onRemove() {
        preset.scripts.forEach { it.onRemove(this) }

        hologram.remove()

        owner?.supports?.remove(this)
        supports.forEach { it.remove() }
    }

    override fun damageReceive(event: RpgEntityDamageEvent) {
        if (event.damager != null && event.damager.location.distanceTo(location) > preset.statistics[RpgCreatureStatistic.DETECT_DISTANCE].cast<Double>() * .98) {
            event.damager.takeIf { it is RpgPlayer }.cast<RpgPlayer>().sendMessage("§cВы слишком далеко от моба, атака не засчитана.")
            event.cancel()
        } else if (event.damager === owner && owner != null) event.cancel()
        if (event.isCancelled) return


        preset.scripts.forEach { it.damageReceive(this, event) }
        if (event.isCancelled) return

        event.damager?.let {
            val damage = event.damage + event.magicDamage
            var points = damage.percentTo(health) * 100
            if (health == maxHealth) points *= 1.5
            addThreat(it, max(1, points.roundToInt()))
            damagerEntities.asMap().merge(it, min(health, damage), Int::plus)
        }
    }

    override fun damageDeal(event: RpgEntityDamageEvent) {
        if (event.damaged === owner) event.cancel()
        if (event.isCancelled) return

        preset.scripts.forEach { it.damageDeal(this, event) }
        if (event.isCancelled) return

        addThreat(event.damaged)
    }

    override fun onKill(event: RpgEntityDamageEvent) {
        preset.scripts.forEach { it.onKill(this, event) }
        if (event.isCancelled) return

    }

    override fun death(event: RpgEntityDamageEvent?) {
        if (handle.isDead) return
        preset.scripts.forEach { it.death(this, event) }
        if (event?.isCancelled == true) return

        playDamage()
        playDeathSound()

        val fullDamage = damagerEntities.asMap().values.sum()
        val damagers = damagerEntities.asMap()
            .filter { (_, damage) -> damage.percentTo(fullDamage) >= .025 }.keys
            .filterIsInstance<RpgPlayer>()
            .filter { it.handle.isOnline }

        if (preset.type != RpgCreatureType.SUPPORT) event?.takeIf { damagers.isNotEmpty() }.let {
            val exp = if (this.exp == 0) 0 else (this.exp / damagers.size.toDouble()).roundToInt()
            damagers.forEach { it.exp += exp }
            ApiManager.createHologram(location.add(.0, 1.0, .0)).apply {
                textLine("§6+ $exp опыта")
                textLine("§7" + (if (damagers.size == 1) damagers.first().handle.name else "каждому из ${damagers.size}"))
                Tasks.asyncAfter(55) { remove() }
            }
            location.dropItems(preset.drops.takeRandomCount().associate { it.item to it.fixedCount }, preset.type.dropGlow)
        }

        remove()
    }

    fun remove() {
        RpgEntityRegistry.removeRpgEntity(this)
        disguise?.disable()
        handle.remove()
    }

    fun addThreat(entity: RpgEntity<*>, threat: Int = 0) = threatEntities.asMap().merge(entity, threat, Int::plus)

    fun sendMessage(message: String) = damagerEntities.asMap().keys
        .filterIsInstance<RpgPlayer>()
        .forEach { it.sendMessage(message) }

    fun onTarget(target: RpgEntity<*>) {
        preset.scripts.forEach { it.onTarget(this, target) }
    }

}