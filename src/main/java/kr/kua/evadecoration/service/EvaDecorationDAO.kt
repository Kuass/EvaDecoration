package kr.kua.evadecoration.service

import com.google.gson.Gson
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kr.kua.evadecoration.EvaDecoration
import kr.kua.evadecoration.entity.global.EDPlayer
import kr.kua.evadecoration.entity.EDPlayerDataSet
import kr.kua.evadecoration.evaDecoration
import org.bukkit.Bukkit
import java.sql.SQLException
import java.util.*

class EvaDecorationDAO {
    private var hikari: HikariDataSource
    private val gson = Gson()

    init {
        val config = evaDecoration.pluginConfig

        Class.forName("org.mariadb.jdbc.Driver")
        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = "jdbc:mariadb://${config.mysqlHost}/${config.mysqlDB}"
        hikariConfig.username = config.mysqlUser
        hikariConfig.password = config.mysqlPassword
        hikari = HikariDataSource(hikariConfig)
    }

    fun update(mcUuid: UUID, edPlayerDataSet: EDPlayerDataSet) {
        Bukkit.getScheduler().runTaskAsynchronously(evaDecoration, Runnable {
            val connection = hikari.connection
            val sql = "UPDATE evadecoration_user SET `clothes_data`='${gson.toJson(edPlayerDataSet)}' WHERE `uuid`='$mcUuid' LIMIT 1"
            val preparedStatement = connection.prepareStatement(sql)
            try {
                preparedStatement.executeUpdate()
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                try {
                    if (!connection.isClosed) connection.close()
                    if (!preparedStatement.isClosed) preparedStatement.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        })
    }
//    fun update(mcUuid: UUID, userBadge: UserBadge) {
//        EvaDecoration.edPlayers[mcUuid]!!.clothes!!.badges.add(userBadge)
//        Bukkit.getScheduler().runTaskAsynchronously(evaDecoration, Runnable {
//            val connection = hikari.connection
//            val sql = "UPDATE evadecoration_user SET `badge`='${gson.toJson(userBadge)}' WHERE `uuid`='$mcUuid' LIMIT 1"
//            val preparedStatement = connection.prepareStatement(sql)
//            try {
//                preparedStatement.executeUpdate()
//            } catch (e: SQLException) {
//                e.printStackTrace()
//            } finally {
//                try {
//                    if (!connection.isClosed) connection.close()
//                    if (!preparedStatement.isClosed) preparedStatement.close()
//                } catch (e: SQLException) {
//                    e.printStackTrace()
//                }
//            }
//        })
//    }

    fun findUser(mcUuid: UUID) {
        Bukkit.getScheduler().runTaskAsynchronously(evaDecoration, Runnable {
            val connection = hikari.connection
            val sql = "SELECT `id`, `clothes_data`, `badge` FROM evadecoration_user WHERE uuid = '$mcUuid' LIMIT 1"
            val preparedStatement = connection.prepareStatement(sql)
            try {
                val resultSet = preparedStatement.executeQuery()
                var id = 0L
                var playerData: EDPlayerDataSet? = null
                while (resultSet.next()) {
                    id = resultSet.getLong("id")

                    val clothData = resultSet.getString("clothes_data")
                    if (!clothData.isNullOrEmpty()) {
                        try {
                            val edPlayerDataSet = gson.fromJson(clothData, EDPlayerDataSet::class.java)
                            playerData = edPlayerDataSet
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

//                    val badgeData = resultSet.getString("badge")
//                    if (!badgeData.isNullOrEmpty()) {
//                        try {
//                            val edPlayerDataSet = gson.fromJson(badgeData, EDPlayerDataSet::class.java)
//                            clothesData = edPlayerDataSet
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    }
                }
                resultSet.close()

                if (id == 0L) {
                    insertUser(mcUuid)
                    playerData = EDPlayerDataSet(mutableListOf(), mutableListOf())
                    EvaDecoration.edPlayers[mcUuid] = EDPlayer(EvaDecoration.edPlayers[mcUuid]!!.player, playerData, id)
                }
                else {
                    val mcPlayer = EvaDecoration.edPlayers[mcUuid]!!.player
                    if (playerData == null) playerData = EDPlayerDataSet(mutableListOf(), mutableListOf())
                    if (playerData.magicCosmeticClothes == null) playerData.magicCosmeticClothes = mutableListOf()
                    if (playerData.badges == null) playerData.badges = mutableListOf()

                    EvaDecoration.edPlayers[mcUuid] = EDPlayer(mcPlayer, playerData, id)

                    if (playerData != null) {
                        // 착용 중인 옷일 경우 착용 처리
                        Bukkit.getScheduler().runTaskLater(evaDecoration, Runnable {
                            val dressClothes = playerData.magicCosmeticClothes!!.filter { it.isDress }
                            dressClothes.forEach {
                                val clothData = EvaDecoration.clothData[it.id]!!
                                CosmeticHelperService.Instance.equipCosmetic(mcPlayer, clothData, it)
                            }
                        }, 20) // 1초
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                try {
                    if (!connection.isClosed) connection.close()
                    if (!preparedStatement.isClosed) preparedStatement.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun insertUser(mcUuid: UUID) {
        Bukkit.getScheduler().runTaskAsynchronously(evaDecoration, Runnable {
            val connection = hikari.connection
            val sql = "INSERT INTO evadecoration_user (`uuid`) VALUES(?)"
            val preparedStatement = connection.prepareStatement(sql)
            try {
                preparedStatement.setString(1, mcUuid.toString())
                preparedStatement.executeUpdate()
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                try {
                    if (!connection.isClosed) connection.close()
                    if (!preparedStatement.isClosed) preparedStatement.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        })
    }

    fun close() {
        try {
            hikari.close()
        } catch (e: Exception) {
//            e.printStackTrace()
        }
    }
}