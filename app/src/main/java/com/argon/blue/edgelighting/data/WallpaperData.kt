package com.argon.blue.edgelighting.data

data class WallpaperData( val name: String,
                          val frontImage: ImageInfo,
                          val backImage: ImageInfo,
                          val clockFont: String,
                          val clockFontSize: Float,
                          val clockFontColor: String,
                          val dateFormat: String,
                          val dateColor: String,
                          val dateFont: String,
                          val dateFontSize: Float)

data class ImageInfo(
    val url: String,
    val thumbUrl: String,
    val width: Int,
    val height: Int
)