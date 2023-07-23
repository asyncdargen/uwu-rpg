package ru.dargen.rpg.util

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Entity
import ru.dargen.rpg.Tasks
import ru.dargen.rpg.util.random.randomDouble
import ru.dargen.rpg.util.random.randomInt
import ru.starfarm.adapter.entitymeta.MetadataType
import ru.starfarm.core.entity.type.Animation
import ru.starfarm.core.protocol.entity.PacketEntityAnimationWrapper
import ru.starfarm.core.protocol.entity.PacketEntityDestroyWrapper
import ru.starfarm.core.protocol.entity.PacketEntityLivingSpawnWrapper
import ru.starfarm.core.protocol.entity.PacketEntityMetadataWrapper
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

fun Location.tempNearHologram(text: String, aliveTicks: Int = 40, radius: Int) {
    val entityId = randomInt(Int.MAX_VALUE / 2, Int.MAX_VALUE)
    val players = getNearPlayers(radius).toTypedArray()

    PacketEntityLivingSpawnWrapper().apply {
        this.entityId = entityId
        this.typeId = 30
        setLocation(this@tempNearHologram)
    }.send(*players)
    PacketEntityMetadataWrapper().apply {
        this.entityId = entityId
        items = listOf(
            MetadataType.BYTE.newItem(0, 0x20.toByte()),
            MetadataType.BYTE.newItem(11, 0x11.toByte()),
            MetadataType.BOOLEAN.newItem(3, true),
            MetadataType.STRING.newItem(2, text)
        )
    }.send(*players)

    Tasks.asyncAfter(aliveTicks) {
        PacketEntityDestroyWrapper().apply { this.entityIds = intArrayOf(entityId) }.send(*players)
    }
}

fun Location.damageHologram(damage: Int, crit: Boolean, radius: Int) = add(-1.1 + randomDouble() * 3.1, randomDouble() * 2, -1.1 + randomDouble() * 2.1)
    .tempNearHologram("${if (crit) "§6§l" else "§c"}-$damage", 25, radius)

fun Entity.playDamage(radius: Int) = PacketEntityAnimationWrapper().apply {
    this.entityId = this@playDamage.entityId
    animation = Animation.TAKE_DAMAGE.ordinal
}.send(*location.getNearPlayers(radius).toTypedArray())

fun drawArc(
    location: Location, particles: List<Particle>, radius: Double,
    yaw: Float, pitch: Float, steps: Float = yaw.absoluteValue / 2,
    yawStep: Float = yaw / steps, pitchStep: Float = pitch / steps
) {
    val initLocation = location.clone().also { it.yaw -= yaw / 2; it.pitch -= pitch / 2 }
    for (step in 0..steps.toInt().absoluteValue) {
        initLocation.clone().apply {
            add(direction.normalize().multiply(radius))
            particles.forEach { world.spawnParticle(it, this, 1, .0, .0, .0, .0) }
        }
        initLocation.also { it.yaw += yawStep; it.pitch += pitchStep }
    }
}

fun drawSpiral(
    center: Location, particles: List<Particle>,
    radius: Double, step: Float, yPerStep: Double = .0
) {
    center.yaw = 0f; center.pitch = 0f;
    for (i in 0..(360 / step).roundToInt()) {
        center.clone().apply {
            yaw += i * step
            y += i * yPerStep
            add(direction.normalize().multiply(radius))
            particles.forEach { world.spawnParticle(it, this, 1, .0, .0, .0, .0) }
        }
    }
}

fun drawSphere(
    center: Location, particles: List<Particle>,
    radius: Double, step: Float
) {
    for (yaw in 0..(360 / step).roundToInt()) {
        for (pitch in -(90 / step).roundToInt()..(90 / step).roundToInt()) {
            center.clone().also { location ->
                location.yaw = yaw * step
                location.pitch = pitch * step
                println(location.pitch)
                location.add(location.direction.normalize().multiply(radius))
                particles.forEach { location.world.spawnParticle(it, location, 1, .0, .0, .0, .0) }
            }
        }
    }
}
