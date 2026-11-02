package org.stypox.dicio.skills.flight

/**
 * Turns a spoken flight number into a code like "UA123". Handles what speech-to-text produces:
 * "UA 123", "u a one two three", "united one twenty three", "delta twelve thirty four".
 */
object FlightNumberParser {
    private val AIRLINES = mapOf(
        "united" to "UA", "american" to "AA", "delta" to "DL", "jetblue" to "B6", "jet blue" to "B6",
        "southwest" to "WN", "spirit" to "NK", "frontier" to "F9", "alaska" to "AS",
        "hawaiian" to "HA", "sun country" to "SY", "allegiant" to "G4", "air canada" to "AC",
        "westjet" to "WS", "west jet" to "WS", "british airways" to "BA", "british" to "BA",
        "virgin atlantic" to "VS", "virgin" to "VS", "air france" to "AF", "klm" to "KL",
        "lufthansa" to "LH", "iberia" to "IB", "aer lingus" to "EI", "ryanair" to "FR",
        "easyjet" to "U2", "easy jet" to "U2", "wizz air" to "W6", "wizz" to "W6",
        "turkish" to "TK", "emirates" to "EK", "qatar" to "QR", "swiss" to "LX",
        "austrian" to "OS", "tap" to "TP", "bulgaria air" to "FB", "copa" to "CM",
        "avianca" to "AV", "latam" to "LA", "aeromexico" to "AM",
    )

    private val UNITS = mapOf(
        "zero" to 0, "oh" to 0, "o" to 0, "one" to 1, "two" to 2, "three" to 3, "four" to 4,
        "five" to 5, "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9,
    )
    private val TEENS = mapOf(
        "ten" to 10, "eleven" to 11, "twelve" to 12, "thirteen" to 13, "fourteen" to 14,
        "fifteen" to 15, "sixteen" to 16, "seventeen" to 17, "eighteen" to 18, "nineteen" to 19,
    )
    private val TENS = mapOf(
        "twenty" to 20, "thirty" to 30, "forty" to 40, "fifty" to 50, "sixty" to 60,
        "seventy" to 70, "eighty" to 80, "ninety" to 90,
    )
    private val FILLER = setOf("flight", "status", "number", "the", "for", "of", "airlines", "airline", "airways")

    fun parse(spoken: String): String? {
        var text = " " + spoken.lowercase().replace(Regex("[^a-z0-9 ]"), " ") + " "

        var airline = ""
        for ((name, code) in AIRLINES.entries.sortedByDescending { it.key.length }) {
            if (text.contains(" $name ")) {
                airline = code
                text = text.replaceFirst(" $name ", " ")
                break
            }
        }

        val tokens = text.split(" ").filter { it.isNotBlank() && it !in FILLER }
        val letters = StringBuilder()
        val digits = StringBuilder()
        var padNext = 0 // after "hundred"/"thousand": width the next number group must fill
        var i = 0
        while (i < tokens.size) {
            val tok = tokens[i]
            val group: String? = when {
                tok.all { it.isDigit() } -> tok
                tok in UNITS -> UNITS[tok].toString()
                tok in TEENS -> TEENS[tok].toString()
                tok in TENS -> {
                    val next = tokens.getOrNull(i + 1)
                    if (next != null && next in UNITS && UNITS[next] != 0) {
                        i++
                        (TENS[tok]!! + UNITS[next]!!).toString()
                    } else {
                        TENS[tok].toString()
                    }
                }
                else -> null
            }
            when {
                group != null -> {
                    digits.append(if (padNext > 0) group.padStart(padNext, '0') else group)
                    padNext = 0
                }
                tok == "hundred" -> padNext = 2
                tok == "thousand" -> padNext = 3
                Regex("[a-z]{2,3}[0-9]{1,4}").matches(tok) && digits.isEmpty() -> {
                    letters.append(tok.takeWhile { it.isLetter() })
                    digits.append(tok.dropWhile { it.isLetter() })
                }
                tok.length <= 3 && tok.all { it.isLetter() } && digits.isEmpty() -> letters.append(tok)
                else -> return null // something that isn't part of a flight number
            }
            i++
        }
        if (padNext > 0) digits.append("0".repeat(padNext))

        val code = airline.ifEmpty { letters.toString().uppercase() }
        val number = digits.toString().trimStart('0')
        if (code.length !in 2..3 || number.isEmpty() || number.length > 4) {
            return null
        }
        return code + number
    }
}
