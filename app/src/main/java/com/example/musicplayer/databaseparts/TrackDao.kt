package com.example.musicplayer.databaseparts

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TrackDao {
    @Query("select * from tracktab")
    fun getAll(): List<TrackData>

    @Query("select * from tracktab where isDeleted = 0 order by title")
    fun getAllOrdered(): List<TrackData>

    @Query("select * from tracktab where artist_name = :artist and isDeleted = 0 order by album_name, track_num")
    fun getAllOrderedByArtist(artist: String): List<TrackData>

    @Query("select * from tracktab where album_name = :album and isDeleted = 0 order by track_num")
    fun getAllByAlbum(album: String): List<TrackData>

    @Query("select distinct title from tracktab order by title")
    fun getAllName(): List<String>

//    get specific track
    @Query("select * from tracktab where id_track = :track")
    suspend fun getTrack(track: String): TrackData

    //    get if specific track is deleted
    @Query("select isDeleted from tracktab where id_track = :track")
    suspend fun isTrackDeleted(track: String): Boolean

    @Query("select * from tracktab where album_name = :album and isDeleted = 0")
    fun getByAlbum(album: String): TrackData

    @Query("select * from tracktab where artist_name like :name and isDeleted = 0")
    fun getByArtist(name: String): TrackData


    @Query("select count(*) from tracktab where isDeleted = 0")
    suspend fun getTrackCount(): Int

    @Query("select count(*) from tracktab where album_name like :album_name")
    fun getTrackCountByAlbum(album_name: String): Int

    @Query("select sum(duration) from tracktab where album_name like :album_name")
    fun getDurationSumByAlbum(album_name: String): Int

    @Query("select count(*) from tracktab where artist_name like :artist_name and isDeleted = 0")
    fun getTrackCountByArtist(artist_name: String): Int

//    @Query("select image_route from tracktab where id_album = :id")
//    suspend fun getAlbumCover(id: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(TrackData: TrackData)

    @Delete
    suspend fun delete(trackData: TrackData)

    @Query("delete from tracktab")
    suspend fun deleteAll()

    @Query("update tracktab set isDeleted = :deleted where id_track = :id")
    suspend fun setDelete(deleted: Boolean, id: String)

    @Query("select count(*) from tracktab where isDeleted = 1")
    suspend fun getDeletedCount(): Int


}