package me.rerere.rikkahub.utils

private val cjkRegex = Regex("[\\u3400-\\u9FFF\\uF900-\\uFAFF]")
private val latinLetterRegex = Regex("[A-Za-z]")

fun String.keepEnglishOnlyForTts(): String {
    return lineSequence()
        .map { it.trim() }
        .filter { line ->
            line.any { it.isLetterOrDigit() } &&
                latinLetterRegex.containsMatchIn(line) &&
                !cjkRegex.containsMatchIn(line)
        }
        .joinToString("\n")
        .trim()
}
