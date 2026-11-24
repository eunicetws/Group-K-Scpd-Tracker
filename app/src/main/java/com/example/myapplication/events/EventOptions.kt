package com.example.myapplication.events

import android.content.Context
import org.json.JSONObject

data class EventOptions(
    val venues: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val activityTypes: List<String> = emptyList()
) {
    companion object {
        private const val FILE_NAME = "event_options.json"

        private var loadedOptions: EventOptions? = null

        fun load(context: Context): EventOptions {
            val alreadyLoaded = loadedOptions
            if (alreadyLoaded != null) {
                return alreadyLoaded
            }

            try {
                val inputStream = context.assets.open(FILE_NAME)
                val text = inputStream.bufferedReader().readText()
                inputStream.close()

                val options = fromJson(text)
                loadedOptions = options
                return options
            } catch (e: Exception) {
                print("Error: $e")
                return EventOptions()
            }
        }

        fun fromJson(text: String): EventOptions {
            val json = JSONObject(text)
            return EventOptions(
                venues = readList(json, "venues"),
                categories = readList(json, "categories"),
                activityTypes = readList(json, "activityTypes")
            )
        }

        private fun readList(json: JSONObject, name: String): List<String> {
            val list = mutableListOf<String>()
            val array = json.optJSONArray(name) ?: return list
            for (index in 0 until array.length()) {
                list.add(array.getString(index))
            }
            return list
        }
    }
}
