package me.aberrantfox.kjdautils.internal.services

import com.google.gson.GsonBuilder
import me.aberrantfox.kjdautils.api.dsl.KConfiguration
import me.aberrantfox.kjdautils.internal.businessobjects.LibMessages
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import java.io.StringReader
import java.io.StringWriter

const val defaultLanguageSetting = "en-eu"
const val rootPath = "/languages"
const val templateName = "Dynamic template"

class LibLocale(val config: KConfiguration) {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val engine = VelocityEngine()
    var libMessages = loadMessageSet()

    /**
     * The one stop shop to setting the language, in both the configuration file and in the KConfiguration.
     */
    fun loadMessageSet(setName: String = defaultLanguageSetting): LibMessages {
        config.language = setName
        val languageContent = LibLocale::class.java.getResource("$rootPath/${config.language}.json").readText()
        return gson.fromJson(languageContent, LibMessages::class.java)
    }

    /**
     * The entry point for injecting a message which requires property injection.
     *
     * Sample: LibLocale.inject({ NO_COMMAND_FOUND }, "username" to event.user.name )
     */
    fun inject(message: LibMessages.() -> String, vararg properties: Pair<String, String>): String {
        val context = VelocityContext().apply { properties.forEach { put(it.first, it.second) } }
        val reader = StringReader(libMessages.message())

        return StringWriter().apply {
            engine.evaluate(context, this, templateName, reader)
        }.toString()
    }
}

