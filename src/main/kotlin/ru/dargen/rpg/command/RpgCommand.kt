package ru.dargen.rpg.command

import org.bukkit.entity.Player
import ru.dargen.rpg.command.admin.RpgItemCommand
import ru.dargen.rpg.util.RPG_PREFIX
import ru.starfarm.core.command.Command
import ru.starfarm.core.command.base.annotation.BaseCommand
import ru.starfarm.core.command.base.annotation.BaseCommandPrefix
import ru.starfarm.core.command.base.annotation.BaseCommandSubs
import ru.starfarm.core.command.context.CommandContext
import ru.starfarm.core.command.require.Require
import ru.starfarm.core.util.unit

@BaseCommandPrefix(RPG_PREFIX)
@BaseCommandSubs(inheritPrefix = true, RpgItemCommand::class)
@BaseCommand("rpg", "Управление сервером", "rp")
class RpgCommand : Command<Player>() {

    init {
        addRequire(Require.permission("rpg"))
    }

    override fun execute(ctx: CommandContext<Player>) = generateHelp(ctx.sender).unit()

}