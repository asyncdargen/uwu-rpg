package ru.dargen.rpg.command.admin

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player
import ru.dargen.rpg.item.RpgItemRegistry
import ru.dargen.rpg.item.asChatComponent
import ru.dargen.rpg.item.asRpg
import ru.dargen.rpg.player.asRpgPlayer
import ru.dargen.rpg.util.RpgPrefixComponent
import ru.starfarm.core.command.Command
import ru.starfarm.core.command.base.annotation.BaseCommand
import ru.starfarm.core.command.base.annotation.BaseCommandIgnore
import ru.starfarm.core.command.base.annotation.BaseCommandParameter
import ru.starfarm.core.command.base.annotation.BaseCommandSubs
import ru.starfarm.core.command.context.CommandContext

@BaseCommandIgnore
@BaseCommandSubs(inheritPrefix = true)
@BaseCommand("item", "Управление предметами", "i")
class RpgItemCommand : Command<Player>() {

    override fun execute(ctx: CommandContext<Player>) {
        generateHelp(ctx.sender)
    }

    @BaseCommand("pull", "Загрузить предметы")
    @BaseCommandParameter("Предметы", "String")
    fun executePull(ctx: CommandContext<Player>) {
        val itemsIdsRaw = ctx.getArg<String>(0)!!
        val itemsIds = when {
            ".." in itemsIdsRaw -> itemsIdsRaw.split("..", limit = 2)
                .map(String::toInt)
                .let { it[0]..it[1] }
                .toList()
                .toIntArray()

            ',' in itemsIdsRaw -> itemsIdsRaw.split(',')
                .map(String::trim)
                .map(String::toInt)
                .toIntArray()

            else -> intArrayOf(itemsIdsRaw.toInt())
        }

        RpgItemRegistry.pull(*itemsIds).whenComplete { _, throwable ->
            if (throwable != null) {
                ctx.sendMessage("§cОшибка при загрузке предметов (см. консоль)")
                throwable.printStackTrace()
            } else {
                ctx.sendMessage("§aПредметы успешно загружены: §e$itemsIdsRaw §7(§e${itemsIds.size} шт.§7)")
            }
        }
    }

    @BaseCommand("get", "Выдать предмет", "g")
    @BaseCommandParameter("Айди", "Integer")
    @BaseCommandParameter("Кол-во", "Integer", required = false)
    fun executeGet(ctx: CommandContext<Player>) {
        val itemId = ctx.getArg<Int>(0)!!.toShort()
        val rpgItem = runCatching { RpgItemRegistry[itemId] }.getOrNull()
        if (rpgItem == null) ctx.sendMessage("§cПредмет не найден!")
        else {
            ctx.sendMessage("§aПредмет успешно выдан!")
            ctx.sender.inventory.addItem(rpgItem.buildItem(ctx.sender.asRpgPlayer!!, ctx.getArg<Int>(1) ?: 1))
        }
    }

    @BaseCommand("enchant", "Заточить предмет", "e")
    @BaseCommandParameter("Уровень", "Integer", args = ["0", "10"], required = false)
    fun executeEnchant(ctx: CommandContext<Player>) {
        val item = ctx.sender.inventory.itemInMainHand?.asRpg
        if (item == null)
            ctx.sendMessage("§cВозьмите предмет в руку!")
        else {
            val enchant = (ctx.getArg<Int>(0)?.toByte()?.let { it + 1} ?: item.enchant)
            ctx.sender.inventory.itemInMainHand =
                item.withPrototype(enchant = enchant.toByte()).buildItem(ctx.sender.asRpgPlayer!!)
            ctx.sendMessage("§aПердмет заточен на §c+$enchant§a!")
        }
    }

    @BaseCommand("find", "Найти предмет", "f")
    @BaseCommandParameter("Имя", "String")
    fun executeFind(ctx: CommandContext<Player>) {
        val name = ctx.getArg<String>(0)!!
        val items = RpgItemRegistry.ItemMap.values.filter { name.lowercase() in it.display.first.lowercase() }
        if (items.isEmpty()) ctx.sendMessage("§cПредметы не найдены!")
        else {
            ctx.sender.spigot().sendMessage(
                RpgPrefixComponent, TextComponent("§aНайденные пердметы:"),
                *items.map {
                    listOf(
                        TextComponent("\n"), RpgPrefixComponent, TextComponent("${it.id} - "),
                        it.buildItem(ctx.sender.asRpgPlayer!!).asChatComponent
                            .apply { clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rpg item get ${it.id}") }
                    )
                }.flatten().toTypedArray()
            )
        }
    }

    @BaseCommand("show", "Посмотреть предмет")
    @BaseCommandParameter("Айди", "Integer")
    @BaseCommandParameter("Ур. заточки", "Integer", args = ["0", "10"], required = false)
    fun executeShow(ctx: CommandContext<Player>) {
        val itemId = ctx.getArg<Int>(0)!!.toShort()
        val enchant = (ctx.getArg<Int>(1) ?: 0).toByte()
        val rpgItem = runCatching { RpgItemRegistry[itemId] }.getOrNull()
        if (rpgItem == null) ctx.sendMessage("§cПредмет не найден!")
        else ctx.sender.spigot().sendMessage(
            RpgPrefixComponent,
            rpgItem.withPrototype(enchant = enchant).buildItem(ctx.sender.asRpgPlayer!!).asChatComponent
        )
    }


}