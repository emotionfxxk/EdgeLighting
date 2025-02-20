package com.argon.blue.edgelighting.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object WallpaperDataParser {
    fun parseJson(json: String): WallpaperData {
        val gson = Gson()
        return gson.fromJson(json, WallpaperData::class.java)
    }
    fun parseJsonArray(json: String): List<WallpaperData> {
        val gson = Gson()
        // 使用 TypeToken 获取 List<WallpaperData> 的类型
        val type = object : TypeToken<List<WallpaperData>>() {}.type
        return gson.fromJson(json, type)
    }
}