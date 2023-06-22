package com.example.musicplayer.databaseparts

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AlbumDao {

    @Query("select * from albumtab")
    fun getAll(): List<AlbumData>

    @Query("select distinct album_name from albumtab order by album_name")
    fun getAllNames(): List<String>
    @Query("select * from albumtab order by album_name")
    fun getAllOrdered(): List<AlbumData>

//    get specific album
    @Query("select * from albumtab where album_name = :album")
    fun getAlbum(album: String): AlbumData

    @Query("select * from albumtab where artist_name = :artist")
    suspend fun getByArtist(artist: String): List<AlbumData>

    @Query("select * from albumtab where artist_name like :artist order by release_year desc, album_name")
    suspend fun getOrderedByArtist(artist: String): List<AlbumData>

    @Query("select image_route from albumtab where artist_name like :artist order by release_year desc")
    fun getCoverByArtist(artist: String): String

    @Query("select image_route from albumtab where album_name = :id")
    fun getAlbumCover(id: String): String

    @Query("select count(*) from albumtab")
    suspend fun getAlbumCount(): Int

    @Query("select count(*) from albumtab where artist_name like :artist_name")
    fun getAlbumCountByArtist(artist_name: String): Int

    @Query("select sum(duration) from albumtab where artist_name like :artist_name")
    fun getAlbumDuration(artist_name: String): Int

    @Query("select duration from albumtab where album_name like :album_name")
    fun getAlbumsDuration(album_name: String): Int

    @Query("select num_of_tracks from albumtab where album_name = :album_name")
    fun getAlbumNumOfTracks(album_name: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(albumData: AlbumData)

    @Delete
    suspend fun delete(albumData: AlbumData)

    @Query("delete from albumtab")
    suspend fun deleteAll()

    @Query("update albumtab set num_of_tracks = num_of_tracks - 1, duration = :duration where album_name = :albumId")
    fun updateTrackInfo(albumId: String, duration: Int)
}