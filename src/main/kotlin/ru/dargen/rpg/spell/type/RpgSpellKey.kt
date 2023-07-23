package ru.dargen.rpg.spell.type

enum class RpgSpellKey(val display: String, val starter: Boolean = true) {

    F("§b§lF"),
    Q("§c§lQ"),
    MOUSE_L("§6§lL", starter = false),
    MOUSE_R("§6§lR")
    ;

}