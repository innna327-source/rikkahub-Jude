package me.rerere.rikkahub.data.datastore.migration

import android.net.Uri
import android.util.Log
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import me.rerere.rikkahub.utils.JsonInstant
import java.io.File

private const val TAG = "SettingsJsonMigrator"

/**
 * 对备份文件中的 settings.json 应用与 DataStore migration 相同的迁移逻辑。
 *
 * DataStore migration 作用于分散的 key-value 存储，而备份文件中的 settings.json
 * 是整个 [me.rerere.rikkahub.data.datastore.Settings] 对象的序列化结果。
 * 此工具类负责在反序列化前对旧格式的 JSON 执行等价的迁移操作。
 */
object SettingsJsonMigrator {
    private val LOCAL_FILE_FOLDERS = setOf("upload", "images", "fonts", "skills")

    /**
     * 对 settings JSON 字符串依次应用所有版本的迁移。
     * 若发生异常则返回原始 JSON，不中断恢复流程。
     */
    fun migrate(settingsJson: String): String {
        return runCatching {
            val root = JsonInstant.parseToJsonElement(settingsJson).jsonObject.toMutableMap()

            // V1: 修复 mcpServers 中全限定类名的 type 字段
            root["mcpServers"]?.let { element ->
                val migrated = migrateMcpServersJson(JsonInstant.encodeToString(element))
                root["mcpServers"] = JsonInstant.parseToJsonElement(migrated)
            }

            // V2: 修复 assistants 中 UIMessagePart 的 type 字段
            root["assistants"]?.let { element ->
                val migrated = migrateAssistantsJson(JsonInstant.encodeToString(element))
                root["assistants"] = JsonInstant.parseToJsonElement(migrated)
            }

            // V3: 将 assistants 中内嵌的 quickMessages 提取为全局 quickMessages
            root["assistants"]?.let { element ->
                val (migratedAssistants, extractedQuickMessages) =
                    migrateAssistantsQuickMessages(JsonInstant.encodeToString(element))
                root["assistants"] = JsonInstant.parseToJsonElement(migratedAssistants)

                if (extractedQuickMessages.isNotEmpty()) {
                    val existing = root["quickMessages"]
                    val existingArray = existing?.let {
                        runCatching { JsonInstant.parseToJsonElement(JsonInstant.encodeToString(it)) as? JsonArray }.getOrNull()
                    } ?: JsonArray(emptyList())
                    val existingIds = existingArray.mapNotNull {
                        (it as? JsonObject)?.get("id")?.toString()?.trim('"')
                    }.toSet()
                    val merged = JsonArray(
                        existingArray + extractedQuickMessages.filter { e ->
                            val id = (e as? JsonObject)?.get("id")?.toString()?.trim('"')
                            id != null && id !in existingIds
                        }
                    )
                    root["quickMessages"] = merged
                }
            }

            root.keys.filter { root[it] is JsonNull }.forEach { key ->
                root.remove(key)
            }

            JsonInstant.encodeToString(JsonObject(root))
        }.onFailure {
            Log.e(TAG, "migrate: Failed to migrate settings JSON, using original", it)
        }.getOrDefault(settingsJson)
    }

    fun migrateLocalFileUris(settingsJson: String, filesDir: File): String {
        return runCatching {
            val root = JsonInstant.parseToJsonElement(settingsJson)
            JsonInstant.encodeToString(rewriteLocalFileUris(root, filesDir))
        }.onFailure {
            Log.e(TAG, "migrateLocalFileUris: Failed to migrate local file uris, using original", it)
        }.getOrDefault(settingsJson)
    }

    private fun rewriteLocalFileUris(element: JsonElement, filesDir: File): JsonElement {
        return when (element) {
            is JsonObject -> JsonObject(
                element.mapValues { (_, value) ->
                    rewriteLocalFileUris(value, filesDir)
                }
            )

            is JsonArray -> JsonArray(
                element.map { item ->
                    rewriteLocalFileUris(item, filesDir)
                }
            )

            is JsonPrimitive -> {
                if (element.isString) {
                    JsonPrimitive(rewriteLocalFileUri(element.content, filesDir))
                } else {
                    element
                }
            }
        }
    }

    private fun rewriteLocalFileUri(value: String, filesDir: File): String {
        val isFileUri = value.startsWith("file:")
        if (!isFileUri && !value.startsWith("/")) {
            return value
        }

        val path = if (isFileUri) {
            Uri.parse(value).path ?: return value
        } else {
            value
        }

        val relativePath = extractKnownFilesDirPath(path) ?: return value
        val targetFile = File(filesDir, relativePath)
        return if (isFileUri) {
            Uri.fromFile(targetFile).toString()
        } else {
            targetFile.absolutePath
        }
    }

    private fun extractKnownFilesDirPath(path: String): String? {
        val filesMarker = "/files/"
        val filesIndex = path.indexOf(filesMarker)
        if (filesIndex < 0) return null

        val relativePath = path.substring(filesIndex + filesMarker.length)
        val folder = relativePath.substringBefore('/')
        return relativePath.takeIf {
            folder in LOCAL_FILE_FOLDERS && it.length > folder.length
        }
    }
}
