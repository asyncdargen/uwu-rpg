package ru.dargen.rpg.region

import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.*
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityCombustEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.material.Directional
import org.bukkit.material.Openable
import java.util.*

object RpgRegionListener {

    private val ForbiddenUsableItems = setOf(
        Material.SNOW_BALL, Material.EXP_BOTTLE, Material.POTION,
        Material.ENDER_PEARL, Material.EYE_OF_ENDER, Material.GLASS_BOTTLE,
        Material.BOAT, Material.WRITTEN_BOOK
    )
    val BuilderList: MutableSet<UUID> = hashSetOf(UUID.fromString("6214a82f-081d-3fee-9297-8842bb6952de"))

    @EventHandler(priority = EventPriority.LOWEST)
    private fun PlayerInteractEvent.handle() {
        if (clickedBlock?.getRelative(BlockFace.UP)?.type == Material.FIRE
            || clickedBlock?.type?.data?.let { it is Directional && it !is Openable } == true
            || item?.type in ForbiddenUsableItems
        ) isCancelled = true
        if (action == Action.PHYSICAL && clickedBlock?.type == Material.SOIL)
            isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun PlayerInteractAtEntityEvent.handle() {
        if ((player.inventory.itemInMainHand.takeIf { hand == EquipmentSlot.HAND }
                ?: player.inventory.itemInOffHand)?.type in ForbiddenUsableItems)
            isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun BlockBreakEvent.handle() {
        if (player.uniqueId !in BuilderList) isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun BlockPlaceEvent.handle() {
        if (player.uniqueId !in BuilderList) isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun LeavesDecayEvent.handle() {
        isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun BlockFadeEvent.handle() {
        if (newState.block.type == Material.SOIL || newState.block.type == Material.ICE)
            isCancelled = true;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun HangingBreakByEntityEvent.handle() {
        isCancelled = true;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun BlockIgniteEvent.handle() {
        isCancelled = true;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun EntityCombustEvent.handle() {
        if (entityType != EntityType.PLAYER) isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun CraftItemEvent.handle() {
        isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun CreatureSpawnEvent.handle() {
        if (spawnReason != CreatureSpawnEvent.SpawnReason.CUSTOM)
            isCancelled = true
    }

}
