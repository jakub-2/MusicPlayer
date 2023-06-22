package com.example.musicplayer.databaseparts

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "AlbumTab")
data class AlbumData(
    @PrimaryKey val album_name: String,
    @ColumnInfo(name = "release_year") val release_year: Int?,
    @ColumnInfo(name = "num_of_tracks") val num_of_tracks: Int,
    @ColumnInfo(name = "duration") val duration: Int,
    @ColumnInfo(name = "image_route") val image_route: String?,
    @ColumnInfo(name = "artist_name") val artist_name: String
)
