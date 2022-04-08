package network.warzone.mars.utils

import com.google.common.base.Preconditions
import com.google.common.collect.Table
import com.google.common.collect.Tables
import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import tc.oc.pgm.lib.net.kyori.adventure.key.Key
import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.translatable
import tc.oc.pgm.lib.net.kyori.adventure.text.TranslatableComponent
import tc.oc.pgm.lib.net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import tc.oc.pgm.lib.net.kyori.adventure.translation.GlobalTranslator
import tc.oc.pgm.lib.net.kyori.adventure.translation.Translator
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.text.MessageFormat
import java.util.*

/**
 * Utility methods for Translatable components.
 * Stripped from PGM's TextTranslations and adapted for Kotlin.
 * <a href="https://github.com/PGMDev/PGM/blob/dev/util/src/main/java/tc/oc/pgm/util/text/TextTranslations.java">Original Source</a>
 */

private val SOURCE_LOCALE = Locale.US
private val LOCALES = Object2ObjectLinkedOpenHashMap<Locale, Locale>()

private val TRANSLATIONS_MAP =
    Object2ObjectAVLTreeMap<String, Map<Locale, MessageFormat>> { obj: String, str: String ->
        obj.compareTo(
            str,
            ignoreCase = true
        )
    }

private val TRANSLATIONS_TABLE: Table<String, Locale, MessageFormat> = Tables.newCustomTable(
    TRANSLATIONS_MAP
) { buildHashMap<Locale, MessageFormat>() }

fun loadTranslations() {
    Preconditions.checkArgument(
        loadKeys(SOURCE_LOCALE) > 0,
        "No text translations found for Mars"
    )
    loadKeys(Locale.getDefault())
    GlobalTranslator.get().addSource(MarsTranslator())
}


private fun <T, U> buildHashMap(): Map<T, U> {
    return Object2ObjectLinkedOpenHashMap(Hash.DEFAULT_INITIAL_SIZE, Hash.FAST_LOAD_FACTOR)
}

private fun parseLocale(locale: String): Locale {
    try {
        val split = locale.split("_").toTypedArray()
        when (split.size) {
            1 -> return Locale(split[0])
            2 -> return Locale(split[0], split[1])
            3 -> return Locale(split[0], split[1], split[2])
        }
    } catch (_: IllegalArgumentException) {}
    return Locale.US
}

fun getLocales(): Set<Locale> {
    return TRANSLATIONS_TABLE.columnKeySet()
}

fun getNearestLocale(locale: Locale): Locale {
    if (locale === SOURCE_LOCALE) return locale
    var nearest = LOCALES[locale]
    if (nearest != null || loadKeys(locale) < 0) return nearest!!
    var maxScore = 0
    for (other in getLocales()) {
        val score = ((if (locale.language == other.language) 3 else 0)
                + (if (locale.country == other.country) 2 else 0)
                + if (locale.variant == other.variant) 1 else 0)
        if (score > maxScore) {
            maxScore = score
            nearest = other
        }
    }
    LOCALES[locale] = nearest
    return nearest!!
}

fun loadKeys(locale: Locale): Long {
    if (getLocales().contains(locale)) return 0
    var keysFound: Long = 0
    // If the locale is not the source code locale,
    // then append the language tag to get the proper resource
    var resourceName = locale.toLanguageTag().replace("-".toRegex(), "_")
    val resource: ResourceBundle
    resource = try {
        ResourceBundle.getBundle(resourceName, locale, UTF8Control())
    } catch (e: MissingResourceException) {
        return 0
    }
    for (key in resource.keySet()) {
        var format = resource.getString(key)

        // Single quotes are a special keyword that need to be escaped in MessageFormat
        // Templates are not escaped, whereas translations are escaped
        if (locale === SOURCE_LOCALE) format = format.replace("'".toRegex(), "''")
        TRANSLATIONS_TABLE.put(key, locale, MessageFormat(format, locale))
        keysFound++
    }

    // Clear locale cache when a new locale is loaded
    if (keysFound > 0) {
        LOCALES.clear()
    }
    return keysFound
}

fun getKey(locale: Locale, key: String): MessageFormat? {
    return TRANSLATIONS_TABLE[key, locale]
}

fun getNearestKey(locale: Locale, key: String): MessageFormat? {
    val nearestLocale = getNearestLocale(locale)
    val format = getKey(nearestLocale, key)
    return if (format != null || nearestLocale === SOURCE_LOCALE) format else getKey(
        SOURCE_LOCALE, key
    )

    // If the format is also missing from the source locale, it is likely an external
    // translation, typically one provided by Mojang for item and block translations.
}


fun getLocale(sender: CommandSender?): Locale {
    return if (sender == null || sender !is Player) {
        SOURCE_LOCALE
    } else parseLocale(sender.spigot().locale)
}

/**
 * Gets a translated text component.
 *
 * @param text The text.
 * @param locale A locale.
 * @return The translated text.
 */
fun translate(text: Component, locale: Locale): Component {
    return GlobalTranslator.render(text, locale)
}

/**
 * Gets a translated text in legacy format.
 *
 * @param text The text.
 * @param sender A command sender or null.
 * @return The translated legacy text.
 */
fun translateLegacy(text: Component, sender: CommandSender?): String {
    return LegacyComponentSerializer.legacySection().serialize(translate(text, getLocale(sender)))
}

/**
 * Gets a translated legacy text.
 *
 * @param key A translation key.
 * @param sender A command sender, or null for the source locale.
 * @param args Optional array of arguments.
 * @return A legacy text.
 * @see .translate
 */
@Deprecated("")
fun translate(key: String, sender: CommandSender?, vararg args: Any): String {
    val locale = getLocale(sender)
    val text: Component = translatable(
        key,
        args.map { Component.text(it.toString()) }
    )
    return LegacyComponentSerializer.legacySection().serialize(translate(text, locale))
}

/**
 * Gets a translated legacy text.
 *
 * @param component A translatable component.
 * @param sender A command sender, or null for the source locale.
 * @return A legacy text.
 * @see .translate
 */
fun translate(component: Component, sender: CommandSender? = null): String {
    val locale = getLocale(sender)
    return LegacyComponentSerializer.legacySection().serialize(translate(component, locale))
}

class MarsTranslator : Translator {
    private val NAMESPACE = Key.key("mars", "translations")

    override fun name(): Key = NAMESPACE

    override fun translate(key: String, locale: Locale): MessageFormat? {
        return getNearestKey(locale, key)
    }
}

/**
 * Stripped from PGM source code.
 *
 * A [java.util.ResourceBundle.Control] implementation which allows reading of .properties
 * files encoded with UTF-8.
 *
 *
 * See https://stackoverflow.com/a/4660195 for more details.
 */
internal class UTF8Control : ResourceBundle.Control() {
    /** {@inheritDoc}  */
    @Throws(IOException::class)
    override fun newBundle(
        baseName: String, locale: Locale, format: String, loader: ClassLoader, reload: Boolean
    ): ResourceBundle {
        // The below is a copy of the default implementation.
        val bundleName = toBundleName(baseName, locale)
        val resourceName = toResourceName(bundleName, "properties")
        var bundle: ResourceBundle? = null
        var stream: InputStream? = null
        if (reload) {
            val url = loader.getResource(resourceName)
            if (url != null) {
                val connection = url.openConnection()
                if (connection != null) {
                    connection.useCaches = false
                    stream = connection.getInputStream()
                }
            }
        } else {
            stream = loader.getResourceAsStream(resourceName)
        }
        if (stream != null) {
            bundle = try {
                // Only this line is changed to make it to read properties files as UTF-8.
                PropertyResourceBundle(InputStreamReader(stream, StandardCharsets.UTF_8))
            } finally {
                stream.close()
            }
        }
        return bundle!!
    }

    override fun getCandidateLocales(name: String, locale: Locale): List<Locale> {
        return Arrays.asList(Locale.ROOT)
    }
}
