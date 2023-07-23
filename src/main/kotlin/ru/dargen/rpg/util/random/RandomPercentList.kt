package ru.dargen.rpg.util.random

class RandomPercentList<T>(private val itemWeight: (T) -> Double, values: List<T>) : RandomList<T>, List<T> by values {

    override fun takeRandom(): T {
        forEachIndexed { index, it -> if (itemWeight(it).percentCeilRandomSuccess || index == size - 1) return it }
        throw IllegalStateException("No taked items")
    }

    override fun takeRandom(n: Int) = take(n).filter { itemWeight(it).percentCeilRandomSuccess }

    override fun takeRandomCount() = filter { itemWeight(it).percentCeilRandomSuccess }

}