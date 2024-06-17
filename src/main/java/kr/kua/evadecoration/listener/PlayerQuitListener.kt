package kr.kua.evadecoration.listener

import kr.kua.evadecoration.EvaDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener : Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        if (EvaDecoration.edPlayers[event.player.uniqueId] != null)
            EvaDecoration.edPlayers.remove(event.player.uniqueId)
    }
}