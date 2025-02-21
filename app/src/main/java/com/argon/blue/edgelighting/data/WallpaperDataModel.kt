package com.argon.blue.edgelighting.data

object WallpaperDataModel {
    private var wallpaperList: List<WallpaperData>? = null
    private var currentIndex:Int = 0
    fun getWallpaperList(): List<WallpaperData> {
        if (wallpaperList == null) {
            wallpaperList = WallpaperDataParser.parseJsonArray(SAMPLE_WALLPAPER_JSON)
        }
        return wallpaperList!!
    }
    fun updateCurrentWallpaperIndex(index:Int) {
        currentIndex = index
    }
    fun currentWallpaperIndex():Int {
        return currentIndex
    }
}