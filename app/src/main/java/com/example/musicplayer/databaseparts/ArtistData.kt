package com.example.musicplayer.databaseparts

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ArtistTab")
data class ArtistData(
    @PrimaryKey val name: String,
    @ColumnInfo(name = "image_route") val image_route: String?,
    @ColumnInfo(name = "duration") val duration: Int,
    @ColumnInfo(name = "num_of_albums") val num_of_albums: Int,
    @ColumnInfo(name = "total_num_of_tracks") val total_num_of_tracks: Int
)
