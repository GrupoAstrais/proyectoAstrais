package avatar

import admin.FormClientData
import admin.RarityType
import admin.calculateCosmeticPrice
import admin.getAdminDao
import com.astrais.db.CosmeticType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.net.JarURLConnection

@Serializable
data class TranslationResource(
    val eng : String,
    val esp : String,
    val rus : String
)

@Serializable
data class PetAvatarResource(
    val type : String,
    val names : TranslationResource,
    val desc : String?,
    val collection : String,
    val price : Int?,
    val rarity: RarityType,
    val imageRef : String
)

@Serializable
data class ThemeColorResource(
    val primary : String,
    val secondary : String,
    val tertiary : String,
    val background : String,
    val backgroundAlt : String,
    val surface : String,
    val text : String,
    val error : String
)

@Serializable
data class ThemeResource(
    val type : String,
    val names : TranslationResource,
    val desc : String?,
    val collection : String,
    val price : Int?,
    val rarity: RarityType,
    val colors : ThemeColorResource
)


/**
 * Mira dentro del jar y busca todos los ficheros que terminen por .cosmetic.json (esa es la extension de los cosmeticos)
 * @return Lista con los ficheros cosmetico
 */
fun getInitialCosmeticsPaths() : List<String>{
    val path = "initial"
    val classLoader = Thread.currentThread().contextClassLoader
    val resource = classLoader.getResource(path) ?: return emptyList()

    if (resource.protocol == "jar") {
        val connection = resource.openConnection() as JarURLConnection
        val jarFile = connection.jarFile

        return jarFile.entries().asSequence()
            .map { it.name }
            .filter { it.startsWith("$path/") && it.endsWith(".cosmetic.json") }
            .map { it.removePrefix("$path/") }
            .toList()
    } else {
        return emptyList()
    }
}

/**
 * Carga los cosmeticos en el JAR dentro de la base de datos
 */
suspend fun loadInitialCosmetics() {
    val classLoader = Thread.currentThread().contextClassLoader
    val externalUploadsDir = System.getenv("UPLOAD_DIR") ?: "uploads"


    val cosmeticPaths = getInitialCosmeticsPaths()
    println("Cosmetics to load: ${cosmeticPaths}")

    cosmeticPaths.forEach { relPath->
        val data = classLoader.getResourceAsStream("initial/$relPath")?.bufferedReader().use {
            it?.readText()
        } ?: return

        // Detecta el tipo y hace las operaciones necesarias
        val jsonType = Json.parseToJsonElement(data).jsonObject["type"]?.jsonPrimitive?.content ?: return
        when (jsonType.lowercase()){
            "pet", "avatar"-> {
                val type = if (jsonType.lowercase() == "pet"){CosmeticType.PET} else {CosmeticType.AVATAR_PART}

                val p = Json.decodeFromString<PetAvatarResource>(data)
                val outImgPath = "$externalUploadsDir/${p.imageRef.substringAfterLast('/')}"
                moveResourceTo("initial/${p.imageRef}", outImgPath)
                getAdminDao().uploadCosmetic(FormClientData(
                    rarity = p.rarity,
                    engName = p.names.eng,
                    espName = p.names.esp,
                    rusName = p.names.rus,
                    desc = p.desc ?: "",
                    type = type,
                    price = p.price ?: calculateCosmeticPrice(type, p.rarity),
                    theme = "",
                    fileName = p.imageRef.substringAfterLast('/'),
                    fileBytes = File(outImgPath).readBytes(),
                    collection = p.collection
                ))
            }
            "theme"-> {
                val p = Json.decodeFromString<ThemeResource>(data)
                val out = getAdminDao().uploadCosmetic(
                    FormClientData(
                        rarity = p.rarity,
                        engName = p.names.eng,
                        espName = p.names.esp,
                        rusName = p.names.rus,
                        desc = p.desc ?: "",
                        type = CosmeticType.APP_THEME,
                        price = p.price ?: calculateCosmeticPrice(CosmeticType.APP_THEME, p.rarity),
                        theme = Json.encodeToString(p.colors),
                        fileName = "",
                        fileBytes = null,
                        collection = p.collection
                    )
                )
                println("Object ${p.names.eng} saved with ${out.first.name}, msg: ${out.second}")
            }
        }
    }
}

fun moveResourceTo(resourcePath : String, outPath : String) : File?{
    val classLoader = Thread.currentThread().contextClassLoader
    val input = classLoader.getResourceAsStream(resourcePath) ?: return null

    val file = File(outPath)
    file.parentFile?.mkdirs()

    file.outputStream().use { output ->
        input.use { inputStream ->
            inputStream.copyTo(output)
        }
        output.flush()
    }

    return file
}