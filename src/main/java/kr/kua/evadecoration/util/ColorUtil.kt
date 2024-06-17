package kr.kua.evadecoration.util

import org.bukkit.ChatColor
import java.util.regex.Matcher
import java.util.regex.Pattern

class ColorUtil {

    companion object {
        private val HEX_PATTERN: Pattern = Pattern.compile("&(#[A-Fa-f0-9]{6})")
        private const val COLOR_CHAR: Char = ChatColor.COLOR_CHAR

        fun translateHexColorCodes(message: String): String {
            //Sourced from this post by imDaniX: https://github.com/SpigotMC/BungeeCord/pull/2883#issuecomment-653955600
            val matcher: Matcher = HEX_PATTERN.matcher(message)
            val buffer = StringBuffer(message.length + 4 * 8)
            while (matcher.find()) {
                val group: String = matcher.group(1)
                matcher.appendReplacement(
                    buffer, COLOR_CHAR.toString() + "x"
                            + COLOR_CHAR + group[0] + COLOR_CHAR + group[1]
                            + COLOR_CHAR + group[2] + COLOR_CHAR + group[3]
                            + COLOR_CHAR + group[4] + COLOR_CHAR + group[5]
                )
            }

            return matcher.appendTail(buffer).toString()
        }
    }
}

fun String.convertAmpersand(): String {
    return this.replace("&", "§")
}