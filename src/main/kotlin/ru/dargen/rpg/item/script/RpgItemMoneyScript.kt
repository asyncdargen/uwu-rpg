package ru.dargen.rpg.item.script

import org.bukkit.Sound
import org.bukkit.event.entity.EntityPickupItemEvent
import ru.dargen.rpg.item.RpgItem
import ru.dargen.rpg.player.MoneyGrade
import ru.dargen.rpg.player.RpgPlayer

object RpgItemMoneyScript : RpgItemScript("money") {

    override fun apply(item: RpgItem) {
        val type = MoneyGrade.valueOf(item.baseItemStack.type) ?: throw IllegalStateException("Money item haven`t type (${item.id})")
        type.item = item
    }

    override fun pickUp(item: RpgItem, player: RpgPlayer, event: EntityPickupItemEvent) {
        event.isCancelled = true
        event.item.remove()

        player.playSound(Sound.ENTITY_ITEM_PICKUP)
        player.balance += MoneyGrade.valueOf(item.baseItemStack.type)!!.price * event.item.itemStack.amount
    }

}