package kr.kua.evadecoration.service

import com.nametagedit.plugin.NametagEdit
import kr.kua.evadecoration.EvaDecoration
import kr.kua.evadecoration.entity.BadgePosition
import kr.kua.evadecoration.entity.UserBadge
import kr.kua.evadecoration.entity.global.EDBadge
import kr.kua.evadecoration.evaDecoration
import kr.kua.evadecoration.util.decoMessage
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player

class NameTagService {

    companion object {
        lateinit var Instance: NameTagService
    }

    private val defaultBadge: List<String> = listOf("\uf01e", "\uf01f", "\uf020", "\uf021")

    fun equip(player: Player, badge: EDBadge, userBadge: UserBadge) {
        try {
            EvaDecoration.edPlayers[player.uniqueId]!!.clothes!!.badges!!
                .filter {
                    it.isDress
                }.map {
                    unequip(player, EvaDecoration.badgeData[it.id]!!, it)
                }

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "setbadge ${player.name} ${badge.code}")

            var badgeComponent = "§f" + badge.code + " "
            when (badge.position) {
                BadgePosition.PREFIX -> {
                    if (badge.nameColor != null) badgeComponent += "${badge.nameColor}"
                    NametagEdit.getApi().setPrefix(player, badgeComponent)
                }
                BadgePosition.SUFFIX -> {
                    NametagEdit.getApi().setSuffix(player, badgeComponent)
                }
            }

            player.decoMessage("뱃지 ${badge.code}${ChatColor.RESET}을(를) 장착했습니다.")

            EvaDecoration.edPlayers[player.uniqueId]!!.clothes!!.badges!!.indexOf(userBadge).let {
                EvaDecoration.edPlayers[player.uniqueId]!!.clothes!!.badges!![it].isDress = true
            }

            player.update()
        } catch (ex: Exception) {
            player.decoMessage("뱃지를 장착하려던 중 문제가 발생하였습니다.")
            evaDecoration.server.consoleSender.sendMessage("[EvaDecoration] An Error Occurred(${player}, ${badge.id}, ${userBadge.acquisitionDate})")
            ex.printStackTrace()
        }
    }

    fun unequip(player: Player, badge: EDBadge, userBadge: UserBadge, queryDB: Boolean = true) {
        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "setbadge ${player.name} ${defaultBadge.random()}")

            NametagEdit.getApi().clearNametag(player)
            player.decoMessage("뱃지 ${badge.code}${ChatColor.RESET}을(를) 장착 해제했습니다.")

            EvaDecoration.edPlayers[player.uniqueId]!!.clothes!!.badges!!.indexOf(userBadge).let {
                EvaDecoration.edPlayers[player.uniqueId]!!.clothes!!.badges!![it].isDress = false
            }

            if (queryDB) player.update()
        } catch (ex: Exception) {
            player.decoMessage("뱃지를 장착하려던 중 문제가 발생하였습니다.")
            evaDecoration.server.consoleSender.sendMessage("[EvaDecoration] An Error Occurred(${player}, ${badge.id}, ${userBadge.acquisitionDate})")
            ex.printStackTrace()
        }
    }

    fun take(player: Player, badge: EDBadge, userBadge: UserBadge) {
        try {
            NametagEdit.getApi().clearNametag(player)
            player.decoMessage("뱃지 ${badge.name}${ChatColor.RESET}을(를) 장착 해제했습니다. [제거됨]")

            val badgeIndex = EvaDecoration.edPlayers[player.uniqueId]!!.clothes!!.badges!!.indexOf(userBadge)
            EvaDecoration.edPlayers[player.uniqueId]!!.clothes!!.badges!!.removeAt(badgeIndex)

            player.update()
        } catch (ex: Exception) {
            player.decoMessage("뱃지를 장착하려던 중 문제가 발생하였습니다.")
            evaDecoration.server.consoleSender.sendMessage("[EvaDecoration] An Error Occurred(${player}, ${badge.id}, ${userBadge.acquisitionDate})")
            ex.printStackTrace()
        }
    }

    private fun Player.update() {
        evaDecoration.getDAO().update(
            player!!.uniqueId,
            EvaDecoration.edPlayers[player!!.uniqueId]!!.clothes!!
        )
    }
}