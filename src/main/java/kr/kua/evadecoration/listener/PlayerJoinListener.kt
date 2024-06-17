package kr.kua.evadecoration.listener

import kr.kua.evadecoration.EvaDecoration
import kr.kua.evadecoration.entity.global.EDPlayer
import kr.kua.evadecoration.evaDecoration
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(evaDecoration, Runnable {
            val player = event.player
            if (EvaDecoration.edPlayers[player.uniqueId] == null)
                EvaDecoration.edPlayers[player.uniqueId] = EDPlayer(player, null, null)
            evaDecoration.getDAO().findUser(player.uniqueId)
        }, 20)
    }
}