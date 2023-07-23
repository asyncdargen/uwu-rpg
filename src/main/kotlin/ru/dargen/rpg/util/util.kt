package ru.dargen.rpg.util

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import ru.dargen.rpg.player.RpgPlayer
import ru.starfarm.core.util.format.ChatUtil
import ru.starfarm.core.util.format.Formatter
import java.time.Duration
import kotlin.math.cos
import kotlin.math.sin

const val RPG_PREFIX = "§6RPG §e> §f"
val RpgPrefixComponent = TextComponent("§6RPG §e> §f")

val String.colored get() = ChatUtil.color(this)
val Boolean.asSymbol get() = if (this) "§a✔" else "§c✖"

val Duration.asTimeText get() = Formatter.formatTimeText(toMillis())
val String.asRange get() = split('-', limit = 2).let { it[0].toInt()..it[1].toInt() }
val String.asItemStack
    get() = split(':', limit = 2)
        .run { ItemStack(Material.matchMaterial(get(0)), 1, (getOrNull(1)?.toShort() ?: 0)) }

fun Int.formatLevel(receiver: RpgPlayer): String {
    val selfLevel: Int = receiver.level
    return when {
        selfLevel - 8 > this -> "§7$this"
        selfLevel - 6 == this || selfLevel - 7 == this || selfLevel - 8 == this -> "§9$this"
        selfLevel - 3 == this || selfLevel - 4 == this || selfLevel - 5 == this -> "§9$this"
        selfLevel - 2 == this || selfLevel + 2 == this || selfLevel - 1 == this || selfLevel + 1 == this || selfLevel == this -> "§e$this"
        selfLevel + 3 == this || selfLevel + 4 == this || selfLevel + 5 == this -> "§6$this"
        selfLevel + 6 == this || selfLevel + 7 == this || selfLevel + 8 == this -> "§c$this"
        selfLevel + 9 == this || selfLevel + 10 == this || selfLevel + 11 == this -> "§4$this"
        else -> "§4??"
    }
}

fun Player.sendOverlayMessage(message: String) = spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(message))

fun Player.sendRpgMessage(message: String) = sendMessage("$RPG_PREFIX$message")

fun Player.teleportWithRotation(location: Location) =
    teleport(location.clone().apply { direction = this@teleportWithRotation.location.direction })

fun Location.getNearEntities(radius: Number) =
    world.getNearbyEntities(this, radius.toDouble(), radius.toDouble(), radius.toDouble())
        .filterIsInstance<LivingEntity>()

fun Location.getNearPlayers(radius: Number) = getNearEntities(radius).filterIsInstance<Player>()

infix fun Location.distanceTo(location: Location) =
    if (location.world !== world) Double.MAX_VALUE else distance(location)

fun Location.direction(to: Location) = subtract(to).direction!!

fun direction(yaw: Number, pitch: Number): Vector {
    val vector = Vector()
    val rotationX = yaw.toDouble()
    val rotationY = pitch.toDouble()
    vector.y = -sin(Math.toRadians(rotationY))
    val xz = cos(Math.toRadians(rotationX))
    vector.x = -xz * sin(Math.toRadians(rotationX))
    vector.z = xz * cos(Math.toRadians(rotationX))
    return vector
}