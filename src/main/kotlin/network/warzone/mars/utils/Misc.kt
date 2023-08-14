package network.warzone.mars.utils

/**
 * The first character of a String is uppercased. The reamining characters are lowercased.
 *
 * Example:
 * <b>Input: hELlO</b>
 * <b>Output: Hello</b>
 *
 */
fun String.capitalizeFirst(): String {
    if (this.isEmpty()) return ""
    return this[0].toUpperCase() + this.substring(1).toLowerCase()
}