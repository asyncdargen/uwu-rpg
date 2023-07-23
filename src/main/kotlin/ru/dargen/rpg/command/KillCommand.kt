package ru.dargen.rpg.command

import org.bukkit.entity.Player
import ru.dargen.rpg.player.asRpgPlayer
import ru.dargen.rpg.util.RPG_PREFIX
import ru.starfarm.core.command.Command
import ru.starfarm.core.command.base.annotation.BaseCommand
import ru.starfarm.core.command.base.annotation.BaseCommandPrefix
import ru.starfarm.core.command.context.CommandContext

@BaseCommandPrefix(RPG_PREFIX)
@BaseCommand("kill", "Убиться", "die")
class KillCommand : Command<Player>() {

    override fun execute(ctx: CommandContext<Player>) {
        ctx.sender.asRpgPlayer?.death()
    }

}