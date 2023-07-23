package ru.dargen.rpg.spell

import com.google.common.cache.CacheBuilder
import com.google.common.cache.RemovalCause
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import ru.dargen.rpg.Tasks
import ru.dargen.rpg.item.isRemoteWeapon
import ru.dargen.rpg.player.RpgPlayer
import ru.dargen.rpg.player.asRpgPlayer
import ru.dargen.rpg.spell.type.RpgSpellKey
import java.util.concurrent.TimeUnit

object RpgSpellListener {

    private val spellCache = CacheBuilder.newBuilder()
        .expireAfterWrite(650, TimeUnit.MILLISECONDS)
        .removalListener { if (it.cause == RemovalCause.EXPIRED) callSpell(it.key, it.value) }
        .build<RpgPlayer, RpgSpellCombination>()

    init {
        Tasks.everyAsync(1, 1) { spellCache.cleanUp() }
    }

    private fun appendKey(player: RpgPlayer, key: RpgSpellKey) {

        if (player.spellCombinations.isNotEmpty()) return
        val combination = (spellCache.getIfPresent(player) ?: RpgSpellCombination().takeIf { key.starter } ?: return)
            .withKey(key)

        spellCache.put(player, combination)
        player.sendTitle("", combination.keys.joinToString(" ", transform = RpgSpellKey::display), stay = 20, out = 0)

        (player.spellCombinations.keys.filter { it.startsWith(combination) }.takeIf { it.size == 1 }
            ?.firstOrNull { it == combination }
            ?: combination.takeIf(RpgSpellCombination::isFull))?.let {
            spellCache.invalidate(player)
            callSpell(player, it)
        }
    }

    private fun callSpell(player: RpgPlayer, combination: RpgSpellCombination) {
        player.spellCombinations[combination]?.cast(player) ?: player.sendTitle("", "§cне найдено", stay = 20, out = 0)
    }

    @EventHandler
    private fun PlayerInteractEvent.handle() {
        val player = player.asRpgPlayer ?: return
        if (item?.isRemoteWeapon != true && hand != EquipmentSlot.OFF_HAND) {
            when (action) {
                Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> appendKey(player, RpgSpellKey.MOUSE_R)
                Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> appendKey(player, RpgSpellKey.MOUSE_L)
                else -> return
            }
        }
    }

    @EventHandler
    private fun PlayerSwapHandItemsEvent.handle() {
        isCancelled = true
        val player = player.asRpgPlayer ?: return

        appendKey(player, RpgSpellKey.F)
    }

    @EventHandler
    private fun PlayerDropItemEvent.handle() {
        if (player.openInventory != null) {
            appendKey(player.asRpgPlayer ?: return, RpgSpellKey.Q)
        }
    }

}