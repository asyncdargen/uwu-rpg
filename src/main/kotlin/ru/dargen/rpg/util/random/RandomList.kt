package ru.dargen.rpg.util.random

interface RandomList<T> : List<T> {

    fun takeRandom(): T

    fun takeRandom(n: Int): List<T>

    fun takeRandomCount(): List<T>

}