package kr.kua.evadecoration.service

import com.francobm.magicosmetics.api.MagicAPI
import kr.kua.evadecoration.EvaDecoration
import kr.kua.evadecoration.entity.EDCosmeticType
import kr.kua.evadecoration.entity.UserMagicCosmetic
import kr.kua.evadecoration.entity.global.EDCloth
import kr.kua.evadecoration.evaDecoration
import kr.kua.evadecoration.util.MagicCosmeticUtil
import kr.kua.evadecoration.util.decoMessage
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player

class CosmeticHelperService {

    companion object {
        lateinit var Instance: CosmeticHelperService
    }

    fun equipCosmetic(player: Player, cloth: EDCloth, cmPlayer: UserMagicCosmetic) {
        if (!MagicAPI.hasCosmetic(player, cloth.cosmeticId))
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cosmetics add ${player.name} ${cloth.cosmeticId}")

        try {
            when (cloth.edCosmeticType) {
                EDCosmeticType.HAT -> {
                    // 장착 하려는 옷이 모자이고, 장착 중인 옷인 모자가 있다면 장착 해제
                    EvaDecoration.edPlayers[player.uniqueId]!!.clothes!!.magicCosmeticClothes!!
                        .filter {
                            val clothData = EvaDecoration.clothData[it.id]!!
                            clothData.edCosmeticType == EDCosmeticType.HAT && it.isDress
                        }.map {
                            unEquipCosmetic(player, EvaDecoration.clothData[it.id]!!, it, false)
                        }
                }

                EDCosmeticType.BALLOON -> {
                    // 장착 하려는 옷이 풍선이고, 장착 중인 옷인 풍선이 있다면 장착 해제
                    EvaDecoration.edPlayers[player.uniqueId]!!.clothes!!.magicCosmeticClothes!!
                        .filter {
                            val clothData = EvaDecoration.clothData[it.id]!!
                            clothData.edCosmeticType == EDCosmeticType.BALLOON && it.isDress
                        }.map {
                            unEquipCosmetic(player, EvaDecoration.clothData[it.id]!!, it, false)
                        }
                }

                else -> {
                    player.decoMessage("장착할 수 없는 옷입니다.")
                }
            }

            // 인게임 장착
            MagicAPI.EquipCosmetic(player, cloth.cosmeticId, "#${cmPlayer.magicCosmeticColor}", true)
            player.decoMessage("${cloth.name}${ChatColor.RESET}을(를) 장착했습니다.")

            // 플러그인 내부적으로 장착 상태 업데이트
            val clothIndex =
                EvaDecoration.edPlayers[player.uniqueId]!!.clothes!!.magicCosmeticClothes!!.indexOf(cmPlayer)
            EvaDecoration.edPlayers[player.uniqueId]!!.clothes!!.magicCosmeticClothes!![clothIndex].isDress = true

            player.update()
        } catch (ex: Exception) {
            player.decoMessage("장착을 시도하던 중 문제가 발생하였습니다.")
            evaDecoration.server.consoleSender.sendMessage("[EvaDecoration] An Error Occurred(${player}, ${cloth.cosmeticId}, #${cmPlayer.magicCosmeticColor})")
            ex.printStackTrace()
        }
    }

    fun unEquipCosmetic(player: Player, cloth: EDCloth, cmPlayer: UserMagicCosmetic, queryDB: Boolean = true) {
        try {
            // 인게임 장착
            MagicAPI.UnEquipCosmetic(player, MagicCosmeticUtil.clothTypeToMagicType(cloth.edCosmeticType))
            player.decoMessage("${cloth.name}${ChatColor.RESET}을(를) 장착 해제했습니다.")

            // 플러그인 내부적으로 장착 상태 업데이트
            val clothIndex =
                EvaDecoration.edPlayers[player.uniqueId]!!.clothes!!.magicCosmeticClothes!!.indexOf(cmPlayer)
            EvaDecoration.edPlayers[player.uniqueId]!!.clothes!!.magicCosmeticClothes!![clothIndex].isDress = false

            if (queryDB) player.update()
        } catch (ex: Exception) {
            player.decoMessage("장착을 시도하던 중 문제가 발생하였습니다.")
            evaDecoration.server.consoleSender.sendMessage("[EvaDecoration] An Error Occurred(${player}, ${cloth.cosmeticId}, #${cmPlayer.magicCosmeticColor})")
            ex.printStackTrace()
        }
    }

    fun takeCosmetic(player: Player, cloth: EDCloth, cmPlayer: UserMagicCosmetic) {
        try {
            // 인게임 장착
            MagicAPI.UnEquipCosmetic(player, MagicCosmeticUtil.clothTypeToMagicType(cloth.edCosmeticType))
            player.decoMessage("${cloth.name}${ChatColor.RESET}을(를) 장착 해제했습니다. [제거됨]")

            // 플러그인 내부적으로 장착 상태 업데이트
            val clothIndex =
                EvaDecoration.edPlayers[player.uniqueId]!!.clothes!!.magicCosmeticClothes!!.indexOf(cmPlayer)
            EvaDecoration.edPlayers[player.uniqueId]!!.clothes!!.magicCosmeticClothes!!.removeAt(clothIndex)

            player.update()
        } catch (ex: Exception) {
            player.decoMessage("장착을 시도하던 중 문제가 발생하였습니다.")
            evaDecoration.server.consoleSender.sendMessage("[EvaDecoration] An Error Occurred(${player}, ${cloth.cosmeticId}, #${cmPlayer.magicCosmeticColor})")
            ex.printStackTrace()
        }
    }

    private fun Player.update() {
        // 장착 상태 DB 에 반영
        evaDecoration.getDAO().update(
            player!!.uniqueId,
            EvaDecoration.edPlayers[player!!.uniqueId]!!.clothes!!
        )
    }
}