package com.example.musicplayer.databaseparts

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "TrackTab")
data class TrackData(
    @PrimaryKey val id_track: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "duration") val duration: Int,
    @ColumnInfo(name = "location") val track_route: String?,
    @ColumnInfo(name = "image_route") val image_route: String?,
    @ColumnInfo(name = "isDeleted") val isDeleted: Boolean,
    @ColumnInfo(name = "album_name") val album_name: String,
    @ColumnInfo(name = "track_num") val track_num: Int?,
    @ColumnInfo(name = "artist_name") val artist_name: String
) : Serializable
