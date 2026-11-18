package com.example.urdu

object PhoneticUrduKeyboard {

    // Standard Phonetic Mapping (Pak Urdu Installer / Urdu Phonetic Layout)
    val charMap: Map<Char, String> = mapOf(
        'a' to "ا",
        'A' to "آ",
        'b' to "ب",
        'B' to "بھ",
        'p' to "پ",
        'P' to "پھ",
        't' to "ت",
        'T' to "ٹ",
        'c' to "چ",
        'C' to "ث",
        'j' to "ج",
        'J' to "ض",
        'h' to "ہ",
        'H' to "ح",
        'x' to "خ",
        'X' to "ش",
        'd' to "د",
        'D' to "ڈ",
        'z' to "ز",
        'Z' to "ذ",
        'r' to "ر",
        'R' to "ڑ",
        's' to "س",
        'S' to "ص",
        'f' to "ف",
        'F' to "ف",
        'q' to "ق",
        'Q' to "ق",
        'k' to "ک",
        'K' to "کھ",
        'g' to "گ",
        'G' to "غ",
        'l' to "ل",
        'L' to "ل",
        'm' to "م",
        'M' to "م",
        'n' to "ن",
        'N' to "ں",
        'w' to "و",
        'W' to "ؤ",
        'v' to "ط",
        'V' to "ظ",
        'y' to "ی",
        'Y' to "ے",
        'i' to "ی",
        'I' to "ئ",
        'e' to "ع",
        'E' to "ع",
        'o' to "ہ",
        'O' to "ۃ",
        'u' to "ء",
        'U' to "ئ",
        ',' to "،",
        '.' to "۔",
        '?' to "؟",
        '0' to "۰",
        '1' to "۱",
        '2' to "۲",
        '3' to "۳",
        '4' to "۴",
        '5' to "۵",
        '6' to "۶",
        '7' to "۷",
        '8' to "۸",
        '9' to "۹"
    )

    fun transliterateDelta(previous: String, updated: String): String {
        if (updated.length <= previous.length) return updated
        val sb = StringBuilder(updated.length)
        for (ch in updated) {
            sb.append(charMap[ch] ?: ch.toString())
        }
        return sb.toString()
    }

    data class KeyCap(
        val urdu: String,
        val hint: String = ""
    )

    val row1 = listOf(
        KeyCap("ق", "q"), KeyCap("و", "w"), KeyCap("ع", "e"), KeyCap("ر", "r"),
        KeyCap("ت", "t"), KeyCap("ے", "Y"), KeyCap("ء", "u"), KeyCap("ی", "i"),
        KeyCap("ہ", "o"), KeyCap("پ", "p")
    )

    val row2 = listOf(
        KeyCap("ا", "a"), KeyCap("س", "s"), KeyCap("د", "d"), KeyCap("ف", "f"),
        KeyCap("گ", "g"), KeyCap("ح", "H"), KeyCap("ج", "j"), KeyCap("ک", "k"),
        KeyCap("ل", "l")
    )

    val row3 = listOf(
        KeyCap("ز", "z"), KeyCap("ش", "X"), KeyCap("چ", "c"), KeyCap("ط", "v"),
        KeyCap("ب", "b"), KeyCap("ن", "n"), KeyCap("م", "m"), KeyCap("ں", "N"),
        KeyCap("،", ","), KeyCap("۔", ".")
    )

    val shiftRow1 = listOf(
        KeyCap("آ", "A"), KeyCap("ٹ", "T"), KeyCap("ڈ", "D"), KeyCap("ڑ", "R"),
        KeyCap("ژ", "Z"), KeyCap("ص", "S"), KeyCap("ض", "J"), KeyCap("ظ", "V"),
        KeyCap("غ", "G"), KeyCap("خ", "x")
    )

    val shiftRow2 = listOf(
        KeyCap("ث", "C"), KeyCap("ذ", "Z"), KeyCap("ھ", "h"), KeyCap("ئ", "I"),
        KeyCap("ؤ", "W"), KeyCap("؟", "?"), KeyCap("َ", "zab"), KeyCap("ِ", "zer"),
        KeyCap("ُ", "pesh"), KeyCap("ّ", "tash")
    )

    val quickLigatures = listOf("ﷺ", "﷽", "ؑ", "ؒ", "ؓ", "ٰ", "!", ":")
}
