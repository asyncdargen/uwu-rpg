package ru.dargen.rpg.util.random

import ru.dargen.rpg.util.max
import ru.dargen.rpg.util.min
import ru.starfarm.core.util.normalize
import java.security.SecureRandom
import kotlin.random.asKotlinRandom

val Random = SecureRandom().asKotlinRandom()

fun randomInt(lowerBound: Int, upperBound: Int) = if (lowerBound == upperBound) lowerBound else Random.nextInt(lowerBound, upperBound)

fun randomInt(upperBound: Int) = randomInt(0, upperBound)

fun randomDouble() = Random.nextDouble()

fun randomBoolean() = Random.nextBoolean()

fun Pair<Int, Int>.random() = randomInt(min, max)

val Int.percentRandomSuccess get() = randomInt(101) <= this

val Double.percentRandomSuccess get() = randomDouble() <= this

val Double.percentCeilRandomSuccess get() = randomDouble() * 100 <= this

val Int.asColoredPercent get() = when (this / 25) {
    in 3..4 -> "§a$this"
    2 -> "§e$this"
    1 -> "§6$this"
    else -> "§c$this"
}

infix fun Int.percentTo(max: Int) = (this / max.toDouble()).normalize(.0, 1.0)

infix fun Int.percentCeilTo(max: Int) = percentTo(max) * 100

fun Double.withRandomSign() = (if (randomBoolean()) 1 else -1) * this

fun Int.withRandomSign() = (if (randomBoolean()) 1 else -1) * this

fun Float.withRandomSign() = (if (randomBoolean()) 1 else -1) * this