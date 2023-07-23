package ru.dargen.rpg.util

inline fun <reified E : Enum<E>> enumValueOn(index: Int) = E::class.java.enumConstants[index]!!

val Pair<Int, Int>.max get() = second
val Pair<Int, Int>.min get() = first

operator fun Pair<Int, Int>.contains(value: Int) = value in min..max