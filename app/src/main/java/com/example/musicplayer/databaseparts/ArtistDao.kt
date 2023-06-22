package com.example.musicplayer.databaseparts

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ArtistDao {
    @Query("select * from artisttab order by name")
    fun getAll(): List<ArtistData>

//    @Query("select distinct name from artisttab order by name")
//    fun getNamesAll(): List<String>
    @Query("select name from artisttab order by name")
    fun getNamesAll(): List<String>

//    get specific artist
    @Query("select * from artisttab where name = :artist")
    fun getByArtist(artist: String): ArtistData

    @Query("select count(*) from artisttab")
    suspend fun getArtistCount(): Int

    @Query("select count(*) from albumtab join artisttab on(artist_name = name)")
    fun getNumOfAlbumsAll(): List<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(artistData: ArtistData)

    @Delete
    suspend fun delete(artistData: ArtistData)

    @Query("delete from artisttab")
    suspend fun deleteAll()

    @Query("update artisttab set num_of_albums = :num_of_albums, total_num_of_tracks = :total_num_of_tracks, duration = :duration where name = :name")
    suspend fun updateNums(name: String, num_of_albums: Int, total_num_of_tracks: Int, duration: Int)
}