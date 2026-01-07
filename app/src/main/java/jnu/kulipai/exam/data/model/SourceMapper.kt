package jnu.kulipai.exam.data.model

import org.json.JSONObject

object SourceMapper {

    fun fromJson(json: String): List<SourceItem> {
        val result = mutableListOf<SourceItem>()
        val obj = JSONObject(json)

        obj.keys().forEach { key ->
            val item = obj.getJSONObject(key)
            result += SourceItem(
                name = key,
                jsonUrl = item.getString("json_url"),
                fileKey = item.getString("file_key")
            )
        }
        return result
    }
}
