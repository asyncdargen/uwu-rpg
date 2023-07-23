package ru.dargen.rpg.util.random

class RandomWeightList<T>(private val itemWeight: (T) -> Int, values: List<T>) : RandomList<T>, List<T> by values {

    val weight = map(itemWeight).sum()

    override fun takeRandom(): T {
        var random = randomInt(weight) + 1
        return filter { value ->
            val weight = itemWeight(value)
            if (random <= weight) return@filter true else random -= weight
            false
        }.first()
    }

    override fun takeRandom(n: Int) = (0 until n).map { takeRandom() }

    override fun takeRandomCount() = takeRandom(randomInt(size + 1))

}