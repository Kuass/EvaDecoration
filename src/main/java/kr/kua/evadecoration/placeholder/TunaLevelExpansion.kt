package kr.kua.evadecoration.placeholder

import kr.kua.evadecoration.evaDecoration
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer

class TunaLevelExpansion : PlaceholderExpansion() {

    override fun getIdentifier(): String {
        return "EvaDecoration"
    }

    override fun getAuthor(): String {
        return "Kua_"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }

    override fun onRequest(player: OfflinePlayer, params: String): String? {
        evaDecoration.server.broadcast(Component.text("PAPI TEST: $params"))

        return super.onRequest(player, params)
    }


}