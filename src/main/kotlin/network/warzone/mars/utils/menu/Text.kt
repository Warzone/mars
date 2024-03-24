package network.warzone.mars.utils.menu

fun wrap(text: String, max: Int = 32) = text.split("\n").flatMap {
    val list = mutableListOf<String>()
    val words = it.split(" ")
    var line = words[0]
    var lastCodes = ""

    for (word in words.drop(1)) {
        val pre = "$line $word"

        // Extract all color and formatting codes used
        val codeMatches = Regex("§.").findAll(line).toList().map { it.value }
        lastCodes = codeMatches.joinToString("")

        if (pre.length > max + lastCodes.length) {
            list += line
            line = "$lastCodes$word"
        } else line = pre
    }

    list += line
    list
}