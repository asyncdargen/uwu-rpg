package ru.dargen.rpg.quest

data class RpgQuest(
    val id: Short, val name: String, val description: List<String>,
    val required: Boolean,
)