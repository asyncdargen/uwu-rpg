package ru.dargen.rpg.spell.type

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import ru.dargen.rpg.player.RpgPlayer
import ru.dargen.rpg.spell.release.RpgSpellJump
import ru.dargen.rpg.spell.release.RpgSpellReleaseHandler
import ru.dargen.rpg.util.asTimeText
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

enum class RpgSpell(
    val display: String, val description: List<String>,
    val mana: Int, val cooldown: Duration,
    private val releaseHandler: RpgSpellReleaseHandler,
    private val useCache: Cache<RpgPlayer, Instant> = CacheBuilder.newBuilder()
        .expireAfterWrite(cooldown.toMillis(), TimeUnit.MILLISECONDS)
        .build()
) {

    JUMP("§bПрыжок", listOf("При активации подкидывает вас"), 4, Duration.ZERO, RpgSpellJump)
    ;

    fun cast(player: RpgPlayer) {
        if (player in useCache.asMap()) {
            val usingTimestamp = useCache.getIfPresent(player)
            player.sendMessage("§cВы сможете воспользоваться этой способностью через §e${cooldown.minus(Duration.between(usingTimestamp, Instant.now())).asTimeText}")
        } else if (player.mana < mana) {
            player.sendMessage("§cНедостаточно маны")
        } else if (releaseHandler.isAllowedToUse(player)) {
            useCache.put(player, Instant.now())
            releaseHandler.release(player)
            player.mana -= mana
        }
    }

}