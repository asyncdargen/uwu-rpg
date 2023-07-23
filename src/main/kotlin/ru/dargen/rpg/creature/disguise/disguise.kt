package ru.dargen.rpg.creature.disguise

import com.comphenix.protocol.PacketType.Play.Server
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.*
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scoreboard.NameTagVisibility
import ru.dargen.rpg.Rpg
import ru.dargen.rpg.Tasks
import ru.dargen.rpg.creature.RpgCreature
import ru.dargen.rpg.creature.minecraft.bridge.RpgEntityBridge
import ru.dargen.rpg.entity.RpgEntityRegistry
import ru.starfarm.adapter.entitymeta.MetadataType
import ru.starfarm.core.protocol.entity.PacketEntityMetadataWrapper
import ru.starfarm.core.protocol.entity.PacketEntityNamedSpawnWrapper
import ru.starfarm.core.protocol.info.PacketPlayerInfoWrapper
import ru.starfarm.core.util.cast
import ru.starfarm.core.util.texture.skin.SkinUtil
import java.util.*

object RpgDisguiseAdapter : PacketAdapter(Rpg, Server.SPAWN_ENTITY_LIVING, Server.ENTITY_METADATA) {

    val DisguiseTeam = Bukkit.getScoreboardManager().mainScoreboard
        .run { getTeam("DISGUISE") ?: registerNewTeam("DISGUISE") }!!
        .apply { nameTagVisibility = NameTagVisibility.NEVER }

    override fun onPacketSending(event: PacketEvent) {
        val container = event.packet
        val player = event.player
        val entityId = container.integers.read(0)
        val creature = RpgEntityRegistry.Entities[entityId]
            ?.takeIf { it is RpgCreature && it.disguise != null }
            ?.cast<RpgCreature>() ?: return
        val disguise = creature.disguise!!

        when (event.packetType) {
            Server.ENTITY_METADATA -> event.packet = disguise.metadataPacket.wrapper
            Server.SPAWN_ENTITY_LIVING -> {
                event.isCancelled = true
                sendInfo(player, disguise, creature)
            }
        }
    }

    fun sendInfo(player: Player, disguise: RpgDisguiseData, entity: RpgCreature) {
        disguise.infoPackets[PlayerInfoAction.ADD_PLAYER]!!.send(player)
        disguise.getSpawnPacket(entity.location).send(player)
        Tasks.asyncAfter(if (player.playerTime > 3000) 10 else 100) { disguise.infoPackets[PlayerInfoAction.REMOVE_PLAYER]!!.send(player) }
    }

}

data class RpgDisguiseData(val entityId: Int, val profile: WrappedGameProfile) {
    constructor(entityId: Int, skin: String, uuid: UUID = UUID.randomUUID()) : this(
        entityId,
        WrappedGameProfile(uuid, uuid.toString().replace("-", "").substring(16)).apply {
            val skin = SkinUtil.getSkin(skin)?.texture
            properties.put("textures", WrappedSignedProperty("textures",
                "ewogICJ0aW1lc3RhbXAiIDogMTY3NTYwOTgxNzcxOSwKICAicHJvZmlsZUlkIiA6ICJhMTdiNDhlYTcxNWE0MTExYWFhYjljNDA1Njk3MWFlMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb2xsZW5fIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogZmFsc2UsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cHM6Ly93ZWJkYXRhLmM3eC5kZXYvdGV4dHVyZXMvc2tpbi9lZDg4YzE0MC1iNjM4LTExZWEtYWNjYS0xY2I3MmNhYTM1ZmQiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfSwKICAgICJDQVBFIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yMzQwYzBlMDNkZDI0YTExYjE1YThiMzNjMmE3ZTllMzJhYmIyMDUxYjI0ODFkMGJhN2RlZmQ2MzVjYTdhOTMzIgogICAgfQogIH0KfQ==", null /*skin?.value, skin?.signature*/))
        }
    )

    init {
        RpgDisguiseAdapter.DisguiseTeam.addEntry(profile.name)
    }

    val infoPackets = object : HashMap<PlayerInfoAction, PacketPlayerInfoWrapper>() {

        val profileData = listOf(
            PlayerInfoData(profile, -1, EnumWrappers.NativeGameMode.NOT_SET, WrappedChatComponent.fromText(""))
        ).toMutableList()

        override fun get(action: PlayerInfoAction) = computeIfAbsent(action) {
            PacketPlayerInfoWrapper().apply {
                this.action = action
                data = profileData
            }
        }

    }

    val metadataPacket = PacketEntityMetadataWrapper().apply {
        entityId = this@RpgDisguiseData.entityId
        items = listOf(
            MetadataType.BYTE.newItem(13, 255.toByte())
        )
    }

    fun getSpawnPacket(location: Location) = PacketEntityNamedSpawnWrapper().apply {
        entityId = this@RpgDisguiseData.entityId
        uuid = profile.uuid

        wrapper.dataWatcherModifier.write(0, WrappedDataWatcher().apply {
            setObject(13, WrappedDataWatcher.Registry.get(java.lang.Byte::class.java), 255.toByte())
        })
        setLocation(location)
    }

    fun disable() = RpgDisguiseAdapter.DisguiseTeam.removeEntry(profile.name)

    companion object {

        fun create(bridge: RpgEntityBridge, skin: String) = RpgDisguiseData(bridge.self.id, skin)

    }

}