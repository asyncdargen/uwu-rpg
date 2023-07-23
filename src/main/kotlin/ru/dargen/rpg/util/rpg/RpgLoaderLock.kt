package ru.dargen.rpg.util.rpg

import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import ru.dargen.rpg.Events
import ru.dargen.rpg.Logger
import ru.starfarm.core.event.GlobalEventContext.on
import java.util.concurrent.Future

object RpgLoaderLock {

    private val Context = Events.fork(false)
    private val Tasks: MutableSet<Future<*>> = hashSetOf()

    init {
        Context.on<AsyncPlayerPreLoginEvent> {
            disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                "Загрузка контента сервера"
            )
        }
        Logger.info("Awaiting content loading...")
    }

    operator fun plus(task: Future<*>) = apply { Tasks += task }

    fun await() {
        Tasks.forEach(Future<*>::get)
        Context.close()
        Logger.info("Content loading successful!")
    }

}