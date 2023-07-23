package ru.dargen.rpg

import org.bukkit.plugin.java.JavaPlugin
import ru.dargen.rpg.creature.RpgCreatureRegistry
import ru.dargen.rpg.entity.RpgEntityRegistry
import ru.dargen.rpg.item.RpgItemRegistry
import ru.dargen.rpg.player.RpgPlayerRegistry
import ru.dargen.rpg.region.RpgRegionRegistry
import ru.dargen.rpg.util.injectMySQLFix
import ru.dargen.rpg.util.rpg.RpgLoaderLock
import ru.starfarm.core.CorePlugin
import ru.starfarm.core.database.DatabaseApi
import ru.starfarm.core.database.executor.factory.QueuedDatabaseExecuteHandlerFactory
import ru.starfarm.core.realm.IRealmService

val Rpg by lazy { JavaPlugin.getPlugin(RpgPlugin::class.java)!! }

val Logger by lazy(Rpg::getLogger)
val Events by lazy(Rpg::eventContext)
val Tasks by lazy(Rpg::taskContext)

val DatabaseConnection = DatabaseApi.createConnectionFromenv("rpg", QueuedDatabaseExecuteHandlerFactory)
val Database by lazy(DatabaseConnection::executeHandler)

class RpgPlugin : CorePlugin() {

    override fun enable() {
        //Register login canceler
        RpgLoaderLock

        injectMySQLFix()

        RpgRegionRegistry
        RpgItemRegistry
        RpgEntityRegistry
        RpgPlayerRegistry
        RpgCreatureRegistry

        registerBaseCommands("ru.dargen.rpg.command")

        //Wait tasks completed and allows to join
        RpgLoaderLock.await()
    }

    override fun handleTowerConnect() {
        IRealmService.get().updateInfo { it.lobby = "AZL" }
        IRealmService.get().transfer(IRealmService.get().realmId, arrayOf("DargenCode"))
//        postToMainThread {
//            val slime = RpgCreatureRegistry[2]
//            Cuboid.atCoordinates(Bukkit.getWorlds().first(), -20.0, 4.0, -20.0, 20.0, 4.0, 20.0).forEach {
//                if (it.x % 10 == 0 && it.z % 10 == 0)
//                    slime.newEntity(it.location)
//            }
//        }
    }

}