package com.example.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

const val HISTORY_KEY = "HistoryKey"

class SearchHistory(private val sharedPref: SharedPreferences) {

    fun saveHistory(tracks: MutableList<Track>) {
        val json = Gson().toJson(tracks)
        sharedPref.edit()
            .putString(HISTORY_KEY, json)
            .apply()
    }

    fun readHistory(): MutableList<Track> {
        val readString = sharedPref.getString(HISTORY_KEY, "")
        if (readString.isNullOrEmpty()) {
            return mutableListOf()
        }
        val type = object : TypeToken<MutableList<Track>>(){}.type
        return Gson().fromJson(readString, type)
    }

    fun clearHistory(mutableList: MutableList<Track>) {
        sharedPref.edit()
            .remove(HISTORY_KEY)
            .apply()
        mutableList.clear()
    }

    fun addTrackToHistory(track: Track, list : MutableList<Track>) {
       // val history = readHistory()
        for (i in list.indices) {
            if (list[i].trackId == track.trackId) {
                list.removeAt(i)
                break
            }
        }
        list.add(0, track)
        if (list.size > 10) {
            list.removeLast()
        }
        saveHistory(list)
    }
}

