package ru.dargen.rpg.entity

import net.minecraft.server.v1_12_R1.EntityLiving
import net.minecraft.server.v1_12_R1.EntityPlayer
import net.minecraft.server.v1_12_R1.EnumGamemode
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity
import org.bukkit.entity.LivingEntity
import ru.starfarm.core.util.cast

val LivingEntity.minecraftEntity get() = cast<CraftEntity>().handle.cast<EntityLiving>()

val EntityLiving?.isTargetable
    get() = this?.isAlive == true && bukkitEntity?.asRpg != null && (this !is EntityPlayer || (bukkitEntity.isOnline
            && playerInteractManager.gameMode != EnumGamemode.CREATIVE
            && playerInteractManager.gameMode != EnumGamemode.SPECTATOR))

