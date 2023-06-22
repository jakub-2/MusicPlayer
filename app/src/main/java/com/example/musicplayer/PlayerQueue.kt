package com.example.musicplayer

import com.example.musicplayer.databaseparts.MusicDatabase
import com.example.musicplayer.databaseparts.TrackData
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.PrintWriter
import java.io.Serializable
import java.lang.IndexOutOfBoundsException

class PlayerQueue(private var path: String) : Serializable {

    private var updateTracks: Boolean = false
    private var updateAlbums: Boolean = false
    private var updateArtists: Boolean = false

    private var updatePlayerStart: Boolean = false
    private var update: Boolean = false
    private val trackQueue = mutableListOf<TrackData>()
    private val databaseQueue = mutableListOf<Long>()
    private var index: Int = 0
    private var position: Int = 0

    suspend fun readDatabase(musicDatabase: MusicDatabase): Boolean {
        fillQueue(musicDatabase)
        if (trackQueue.size == 0)
            return false
        return true
    }

    private suspend fun fillQueue(musicDatabase: MusicDatabase) {
        for (x in databaseQueue.indices) {
            val track = musicDatabase.trackDao().getTrack(databaseQueue[x].toString())
            trackQueue.add(x, track)
        }
    }

    fun readSavedQueue() {
        databaseQueue.clear()
        val queueFile = path + File.separator + "queue.txt"
        try {
            File(queueFile).forEachLine {
                databaseQueue.add(it.toLong())
            }
        } catch (e: FileNotFoundException) {
            databaseQueue.clear()
        }
        val positionFile = path + File.separator + "position.txt"
        try {
            index = File(positionFile).readLines()[0].toInt()
            position = File(positionFile).readLines()[1].toInt()
        } catch (e: FileNotFoundException) {
            index = 0
            position = 0
        }
        if (index > databaseQueue.size) {
            index = 0
        }
    }

    fun saveQueue() {
        val file = path + File.separator + "queue.txt"
        val fileWriter = FileWriter(file)
        val printWriter = PrintWriter(fileWriter)
        for (x in databaseQueue) {
            printWriter.println(x)
        }
        printWriter.flush()
        printWriter.close()
    }

    fun saveState() {
        val file = path + File.separator + "position.txt"
        val fileWriter = FileWriter(file)
        val printWriter = PrintWriter(fileWriter)
        printWriter.println(index)
        printWriter.println(position)
        printWriter.flush()
        printWriter.close()
    }

    fun nextSong() {
        if (databaseQueue.size == 0) {
            index = 0
            return
        }
        if (index == databaseQueue.size - 1) {
            index = 0
        } else {
            index++
        }

    }

    fun previousSong() {
        if (databaseQueue.size == 0) {
            index = 0
            return
        }
        if (index == 0) {
            index = databaseQueue.size - 1
        } else {
            index--
        }
    }

    fun getSize(): Int {
        return databaseQueue.size
    }

    fun getIndex(): Int {
        return index
    }

    fun getPosition(): Int {
        return position
    }

    fun getTrackData(): TrackData {
        if (trackQueue.size == 0) {
            return TrackData("0", "", 0, null, null, false, "", null, "")
        }
        try {
            return trackQueue[index]
        } catch (e: IndexOutOfBoundsException) {
            return trackQueue[0]
        }

    }

    fun setPosition(pos: Int) {
        position = pos
    }

    fun setQueue(tracks: List<TrackData>) {
        trackQueue.clear()
        databaseQueue.clear()
        for (track in tracks) {
            trackQueue.add(track)
            databaseQueue.add(track.id_track.toLong())
        }
    }

    fun setIndex(position: Int) {
        index = position
    }

    fun setUpdate(update: Boolean) {
        this.update = update
    }

    fun getUpdate(): Boolean {
        return update
    }

    fun append(track: TrackData) {
        trackQueue.add(track)
        databaseQueue.add(track.id_track.toLong())
    }

    fun setDelete(id: String) {
        val remove = mutableListOf<Int>()
        for (i in 0 until trackQueue.size) {
            if (trackQueue[i].id_track == id) {
                remove.add(i)
            }
        }
        remove.reverse()
        for (i in remove) {
            try {
                trackQueue.removeAt(i)
                databaseQueue.removeAt(i)
            }catch (e: IndexOutOfBoundsException) {
                continue
            }
        }
    }

    fun deleteAll() {
        trackQueue.clear()
        databaseQueue.clear()
        index = 0
        position = 0
    }

    fun setUpdatePlayerStart(update: Boolean) {
        this.updatePlayerStart = update
    }

    fun getUpdatePlayerStart(): Boolean {
        return updatePlayerStart
    }

    fun readUpdate() {
        val updateFile = path + File.separator + "update.txt"
        try {
            update = File(updateFile).readLines()[0].toBoolean()
            updateTracks = File(updateFile).readLines()[1].toBoolean()
            updateAlbums = File(updateFile).readLines()[2].toBoolean()
            updateArtists = File(updateFile).readLines()[3].toBoolean()
            saveUpdate(false, false, false, false)
        } catch (e: FileNotFoundException) {
            update = false
            updateTracks = false
            updateAlbums = false
            updateArtists = false
        }
    }
    fun saveUpdate() {
        val file = path + File.separator + "update.txt"
        val fileWriter = FileWriter(file)
        val printWriter = PrintWriter(fileWriter)
        printWriter.println(update)
        printWriter.println(updateTracks)
        printWriter.println(updateAlbums)
        printWriter.println(updateArtists)
        printWriter.flush()
        printWriter.close()
    }

    fun saveUpdate(update: Boolean) {
        val file = path + File.separator + "update.txt"
        val fileWriter = FileWriter(file)
        val printWriter = PrintWriter(fileWriter)
        printWriter.println(update)
        printWriter.println(updateTracks)
        printWriter.println(updateAlbums)
        printWriter.println(updateArtists)
        printWriter.flush()
        printWriter.close()
    }

    fun saveUpdate(
        update: Boolean,
        updateTrack: Boolean,
        updateAlbum: Boolean,
        updateArtist: Boolean
    ) {
        val file = path + File.separator + "update.txt"
        val fileWriter = FileWriter(file)
        val printWriter = PrintWriter(fileWriter)
        printWriter.println(update)
        printWriter.println(updateTrack)
        printWriter.println(updateAlbum)
        printWriter.println(updateArtist)
        printWriter.flush()
        printWriter.close()
    }

    fun getUpdateTracks(): Boolean {
        return updateTracks
    }

    fun setUpdateTracks(update: Boolean) {
        updateTracks = update
    }

    fun getUpdateAlbums(): Boolean {
        return updateAlbums
    }

    fun setUpdateAlbums(update: Boolean) {
        updateAlbums = update
    }

    fun getUpdateArtists(): Boolean {
        return updateArtists
    }

    fun setUpdateArtists(update: Boolean) {
        updateArtists = update
    }

}