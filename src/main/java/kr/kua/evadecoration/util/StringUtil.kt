package kr.kua.evadecoration.util

import org.bukkit.ChatColor
import org.bukkit.entity.Player

fun String.infoFormat(): String {
    return "\ue4dc ${ChatColor.RESET}$this"
}

fun String.warnFormat(): String {
    return "\ue4dd ${ChatColor.RESET}$this"
}

fun String.errorFormat(): String {
    return "\ue4de ${ChatColor.RESET}$this"
}

fun Player.decoMessage(message: String) {
    this.sendMessage("\uD83C\uDFA8 ${ChatColor.RESET}$message")
}

fun makeDisassembleMessage(itemName: String, amount: Int): String {
    return "$itemName${ChatColor.RESET}을(를) $amount 만큼 얻었습니다."
}

private const val colorSign = '&'
private var removeFlag = false
fun String.toPlainText(): String {
    val sb = StringBuilder()
    var i = -1
    while (++i < this.length)
        if (this[i] == colorSign)
            removeFlag = true
        else if (removeFlag)
            removeFlag = false
        else sb.append(this[i])
    return sb.toString()
}