package ru.dargen.rpg.creature.minecraft

import net.minecraft.server.v1_12_R1.NavigationAbstract

fun NavigationAbstract.move(x: Double, y: Double, z: Double, speed: Double = 1.0) = a(x, y, z, speed)

fun NavigationAbstract.clearPath() = p()

fun NavigationAbstract.noPath() = o()
