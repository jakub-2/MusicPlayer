package com.example.musicplayer.databaseparts

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AlbumData::class, TrackData::class, ArtistData::class], version = 1)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun albumDao() : AlbumDao
    abstract fun artistDao() : ArtistDao
    abstract fun trackDao() : TrackDao

    companion object {
        @Volatile
        private var INSTANCE : MusicDatabase? = null

        fun getDatabase(context: Context): MusicDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
