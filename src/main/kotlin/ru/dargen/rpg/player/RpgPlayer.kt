package ru.dargen.rpg.player

import org.bukkit.GameMode
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import ru.dargen.rpg.Tasks
import ru.dargen.rpg.entity.RpgEntity
import ru.dargen.rpg.entity.RpgEntityStatistic
import ru.dargen.rpg.entity.combat.RpgEntityCombatState
import ru.dargen.rpg.entity.combat.RpgEntityDamageEvent
import ru.dargen.rpg.item.asRpg
import ru.dargen.rpg.item.dropItems
import ru.dargen.rpg.player.combat.RpgPlayerCombatState
import ru.dargen.rpg.region.RpgRegionRegistry
import ru.dargen.rpg.region.types.RpgRegionFlag
import ru.dargen.rpg.spell.RpgSpellCombination
import ru.dargen.rpg.spell.type.RpgSpell
import ru.dargen.rpg.util.*
import ru.dargen.rpg.util.random.percentTo
import ru.dargen.rpg.util.random.randomDouble
import ru.dargen.rpg.util.random.randomInt
import ru.dargen.rpg.util.random.withRandomSign
import ru.starfarm.core.util.cast
import ru.starfarm.core.util.unit
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

class RpgPlayer(
    player: Player, statistic: MutableMap<RpgEntityStatistic, Int>
) : RpgEntity<CraftPlayer>(player.cast(), statistic, RpgPlayerCombatState()) {

    var regionId: Short = 0
    val region get() = RpgRegionRegistry[regionId]

    override val combatState get() = super.combatState.cast<RpgEntityCombatState>()
    val isPvp get() = combatState.cast<RpgPlayerCombatState>().isPvp

    private var healthOffset: Int = 0
    private var manaOffset: Int = 0

    override var health: Int
        set(value) {
            healthOffset += min(value, maxHealth) - health
            super.health = value
            updateHandleHealth()
        }
        get() = super.health
    override var mana: Int
        set(value) {
            manaOffset += min(value, maxMana) - mana
            super.mana = value
        }
        get() = super.mana

    override var level: Int
        get() = super.level
        set(value) = RpgEntityStatistic.LEVEL.set(value).unit(this::recomputeStatistics)
    val isMaxLevel get() = needExp == -1
    override var exp: Int
        get() = super.exp
        set(value) {
            if (!isMaxLevel && value - exp > 0) sendMessage("§6+ ${value - exp} опыта")
            RpgEntityStatistic.EXP.set(if (isMaxLevel) 0 else value)
            if (!isMaxLevel && value >= needExp) {
                mergeStatistic(RpgEntityStatistic.EXP, needExp, Int::minus)
                level++
                sendMessage("§aУровень персонажа увеличен: §e${level}§a!")
                sendTitle("§aУровень увеличен", "§e$level")
            }
        }
    val needExp: Int get() = RpgPlayerRegistry.getNextLevelExp(level)

    var balance: Int = 0
    val spellCombinations: MutableMap<RpgSpellCombination, RpgSpell> = HashMap()

    override fun tickAsync(tick: Int) {
        if (tick % 2 == 0) {
            //information
            handle.level = level
            handle.exp = if (isMaxLevel) 1f else (exp percentTo needExp).toFloat()
            sendOverlayMessage(
                "§c§l$health/$maxHealth ${
                    if (healthOffset != 0) "(${if (healthOffset > 0) "+" else ""}$healthOffset)" else ""
                }❤§r         §b§l$mana/$maxMana ${
                    if (manaOffset != 0) "(${if (manaOffset > 0) "+" else ""}$manaOffset)" else ""
                }☪         §r${balance.asMoneyString}"
            )

            //checking pvp
            val (changed, pvp) = combatState.cast<RpgPlayerCombatState>().isPvpState
            if (changed && pvp) sendMessage("§cВы вошли в режим PvP, выход из игры будет расцениваться как смерть!")
            else if (changed) sendMessage("§aВы вышли из режима PvP!")
        }
    }

    override fun tick(tick: Int) {

        if (handle.name.equals("Nollen_")) {
//            entity.yaw += 7f
//            entity.lastYaw = entity.yaw
//            entity.headRotation = entity.yaw
            handle.teleport(location.apply { yaw += 7 })
        }
        if (tick % 5 == 0) {
            //checking region
            val region = RpgRegionRegistry.getRegion(this)
            if (region.id != regionId) {
                RpgRegionRegistry.onRegionChanged(region, this.region, this)
                regionId = region.id
            }

            //checking void
            if (location.y < 0) death()
        }
        if (tick % 20 == 0) {
            manaOffset = 0
            healthOffset = 0

            if (mana != maxMana) mana += RpgEntityStatistic.MANA_REGENERATION.get()
            if (!isCombat) {
                if (health != maxHealth) {
                    health += RpgEntityStatistic.REGENERATION.get()
//                    drawSpiral(location.add(.0, .2, .0), listOf(Particle.FLAME), 1.2, 10f, .0)
                }
                if (handle.foodLevel != 20 && RpgRegionFlag.FOOD in region) handle.foodLevel++
            }
        }
    }

    override fun onSpawn() {
        recomputeStatistics()
        sendMessage("§aПрофиль успешно загружен!")
    }

    override fun onRemove() {
        supports.forEach { it.remove() }

        if (isPvp) death()
    }

    override fun damageReceive(event: RpgEntityDamageEvent) {
        if (event.talentComputing) {
            /*TODO: buff computing*/
            if (event.isCancelled) return
        }

    }


    override fun damageDeal(event: RpgEntityDamageEvent) {
        if (event.damaged is RpgPlayer && (RpgRegionFlag.PVP !in event.damaged.region || RpgRegionFlag.PVP !in region)) {
            sendMessage("§cВ мирных зонах запрещено PvP!")
            event.cancel()
        } else if ((event.damaged is RpgPlayer && (event.damaged.level - level).absoluteValue >= 5)) {
            sendMessage("§cВы не можете атаковать игроков чей уровень отличается от вашего более чем на 5!")
            event.cancel()
        } else if (event.talentComputing) {
            /*TODO: buff computing*/
        }

        if (event.isCancelled) return

        if (event.weapon == null || !event.weapon.isRemoteWeapon) {
            val fall = handle.fallDistance > 0
            val distance = location.distanceTo(event.damaged.location)
            drawArc(
                handle.eyeLocation,
                listOf(Particle.CRIT, Particle.CRIT_MAGIC),
                min(distance, 2.0),
                (if (fall) randomInt(20, 40) else randomInt(80, 120)).toFloat().withRandomSign(),
                (if (fall) 100 + randomDouble() * 20 else randomDouble() * 75).toFloat().withRandomSign()
            )
        }
    }

    override fun onKill(event: RpgEntityDamageEvent) {

    }

    override fun death(event: RpgEntityDamageEvent?) {
        if (event?.talentComputing == true) {
            /*TODO: buff computing*/
            if (event.isCancelled) return
        }

        if (handle.gameMode == GameMode.CREATIVE || handle.gameMode == GameMode.SPECTATOR) return

        playDamage()
        playDeathSound()

        if (exp * .01 >= 1) {
            sendMessage("§cВы умерли и потеряли 1% опыта!")
            exp -= (exp * .01).toInt()
        } else sendMessage("§cВы умерли!")

        if (isPvp && balance * .2 >= 1) {
            val withdraw = (balance * .2).toInt()
            balance -= withdraw
            location.dropItems(withdraw.asMoney.mapKeys { it.key.item })
        }

        sendTitle("§4§l....")
        handle.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 45, 0, true, false))
        handle.gameMode = GameMode.SPECTATOR
        handle.flySpeed = 0f
        handle.teleportWithRotation((region.grave ?: RpgRegionRegistry.getNearGrave(location)))
        handle.velocity = Vector(.0, 1.4, .0)
        combatState.reset()

        Tasks.after(25) {
            handle.fireTicks = 0
            handle.fallDistance = 0f
            handle.foodLevel = 20
            handle.flySpeed = .2f;

            health = maxHealth

            handle.gameMode = GameMode.SURVIVAL
        }
    }

    fun recomputeStatistics() {
        val statistics = enumValues<RpgEntityStatistic>()
            .filter(RpgEntityStatistic::isMergeable)
            .associateWith(RpgEntityStatistic::defaults)
            .toMutableMap()

        statistics.merge(RpgEntityStatistic.HEALTH_MAX, (level - 1) * 2, Int::plus)

        handle.inventory.armorContents
            .mapNotNull(ItemStack::asRpg)
            .filter { it.isAllowedToUse(this) }
            .forEach { item -> statistics.forEach { (key, value) -> statistics[key] = value + key.merger!!(item) } }

        statistics.forEach(this.statistics::put)
        updateHandleHealth()
        handle.walkSpeed = (.2 * ((RpgEntityStatistic.SPEED.get() + 100) / 100.0)).toFloat()
    }

    private fun updateHandleHealth() {
        handle.health = min(20.0, max(.5, 20.0 * (health.toDouble() / maxHealth.toDouble())))
    }

    fun sendMessage(message: String) = handle.sendRpgMessage(message)

    fun sendOverlayMessage(message: String) = handle.sendOverlayMessage(message)

    fun sendTitle(title: String = "", subTitle: String? = "", `in`: Int = 0, stay: Int = 25, `out`: Int = 0) =
        handle.sendTitle(title, subTitle, `in`, stay, `out`)

    fun playSound(sound: Sound) = handle.playSound(handle.location, sound, 1f, 1f)

}

