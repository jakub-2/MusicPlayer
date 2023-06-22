package com.example.musicplayer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.database.getStringOrNull
import com.example.musicplayer.databaseparts.AlbumData
import com.example.musicplayer.databaseparts.ArtistData
import com.example.musicplayer.databaseparts.MusicDatabase
import com.example.musicplayer.databaseparts.TrackData
import com.example.musicplayer.databinding.ActivityScanMediaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ScanMedia : AppCompatActivity() {
    private lateinit var binding: ActivityScanMediaBinding
    private lateinit var musicDatabase: MusicDatabase
    private lateinit var playerQueue: PlayerQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = this.intent.extras
        playerQueue = bundle!!.getSerializable("queue") as PlayerQueue

        musicDatabase = MusicDatabase.getDatabase(this)

        update()

        binding.artistScannerId

        binding.scanButton.setOnClickListener { scanMusic() }
        binding.recreateButton.setOnClickListener { redoDatabase() }
//        this.context = context
    }

    private fun update() {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                val tracks = musicDatabase.trackDao().getTrackCount()
                val albums = musicDatabase.albumDao().getAlbumCount()
                val artists = musicDatabase.artistDao().getArtistCount()
                val deleted = musicDatabase.trackDao().getDeletedCount()
                binding.trackScannerId.text = getString(R.string.scan_track, tracks)
                binding.albumScannerId.text = getString(R.string.scan_album,albums)
                binding.artistScannerId.text = getString(R.string.scan_artist, artists)
                binding.deletedTracks.text = getString(R.string.scan_deleted_track, deleted)
            }
        }
    }

    private fun redoDatabase() {
        Log.i("1", "recreating")
        GlobalScope.launch(Dispatchers.IO) {
            musicDatabase.trackDao().deleteAll()
            musicDatabase.albumDao().deleteAll()
            musicDatabase.artistDao().deleteAll()
            playerQueue.deleteAll()
            playerQueue.saveQueue()
            playerQueue.saveState()
            playerQueue.saveUpdate(true, true, true, true)
            update()
        }
    }

    private fun scanMusic(){
        Log.i("1", "scanning")
        Toast.makeText(this, "Scanning music", Toast.LENGTH_SHORT).show()
        val projection = arrayOf(
            //you only want to retrieve _ID and DISPLAY_NAME columns
            // track
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.RELATIVE_PATH,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.CD_TRACK_NUMBER,
            // album
            // id is already requested
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.YEAR,
            // num of tracks later
            // art from file
            MediaStore.Audio.Media.ARTIST_ID,
            // artist
            // id is already requested
            MediaStore.Audio.Media.ARTIST
            // num of albums and tracks later of query
//            MediaStore.Audio.Media.ARTIST,
            )
        val albums: HashSet<AlbumData> = HashSet()
        val artists: HashSet<ArtistData> = HashSet()
        val tracks: HashSet<TrackData> = HashSet()
        this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null, null)?.use { cursor ->
            //cache column indices
            // track
            val idTrack = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val trackColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
            val temp = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val trackNumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.CD_TRACK_NUMBER)

            // album
            val albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val yearColumn = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)

            // artist
            val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)

            //iterating over all of the found images
            while (cursor.moveToNext()) {
                // track
                val trackId = cursor.getString(idTrack)
                val trackName = cursor.getString(trackColumn)
                val duration = cursor.getString(durationColumn).toInt()
                val path = cursor.getString(temp)
                // album
                val albumName = cursor.getString(albumColumn)
                val year: Int? = cursor.getStringOrNull(yearColumn)?.toInt()
                val trackNumCol = cursor.getStringOrNull(trackNumColumn)
                var trackNum: Int? = null
                if (trackNumCol != null) {
                    if ("/" in trackNumCol) {
                        trackNum = trackNumCol.substring(0 until trackNumCol.indexOf("/")).toInt()
                    } else {
                        trackNum = trackNumCol.toInt()
                    }
                }

                // artist
                val artistName = cursor.getString(artistColumn)

                val track = TrackData(trackId, trackName, duration, path, null, false, albumName, trackNum, artistName)
                val album = AlbumData(albumName, year, 0, 0, null, artistName)
                val artist = ArtistData(artistName, null, 0, 0, 0)
                tracks.add(track)
                albums.add(album)
                artists.add(artist)
            }
        }
        val insert = Runnable {
            kotlin.run {
                for (x in tracks) {
                    var imagePath: String?
                    val temp = File(this.filesDir.path + File.separator + x.album_name + ".png")
                    if (!temp.exists()) {
                        val mmr = MediaMetadataRetriever()
                        mmr.setDataSource(x.track_route)
                        val art = mmr.embeddedPicture
                        if (art == null) {
                            imagePath = null
                        } else {
                            val image = BitmapFactory.decodeByteArray(art, 0, art.size)
                            val file = File(this.filesDir.path + File.separator + x.album_name + ".png")
                            val fos = FileOutputStream(file)
                            image.compress(Bitmap.CompressFormat.PNG, 90, fos)
                            fos.close()
                            imagePath = file.path
                        }
                    } else {
                        imagePath = temp.path
                    }
                    val insert = TrackData(x.id_track, x.title, x.duration, x.track_route, imagePath, x.isDeleted, x.album_name, x.track_num, x.artist_name)
                    musicDatabase.trackDao().insert(insert)
                }
                for (x in albums) {
                    if (musicDatabase.albumDao().getAlbum(x.album_name) == null) {
                        if (musicDatabase.trackDao().getByAlbum(x.album_name) != null) {
                            val path = musicDatabase.trackDao().getByAlbum(x.album_name).image_route
                            val numOfTracks = musicDatabase.trackDao()
                                .getTrackCountByAlbum("%" + x.album_name + "%")
                            val duration = musicDatabase.trackDao()
                                .getDurationSumByAlbum("%" + x.album_name + "%")
                            val insert = AlbumData(
                                x.album_name,
                                x.release_year,
                                numOfTracks,
                                duration,
                                path,
                                x.artist_name
                            )
                            musicDatabase.albumDao().insert(insert)
                        }
                    }
                }

                for (x in artists) {
                    if (musicDatabase.artistDao().getByArtist(x.name) == null) {
                        if (musicDatabase.trackDao().getByArtist("%" + x.name + "%") != null) {
                            val path = musicDatabase.albumDao().getCoverByArtist("%" + x.name + "%")
                            val numOfAlbums = musicDatabase.albumDao().getAlbumCountByArtist("%" + x.name + "%")
                            val duration = musicDatabase.albumDao().getAlbumDuration("%" + x.name + "%")
                            val numOfTracks = musicDatabase.trackDao().getTrackCountByArtist(x.name)
                            val insert = ArtistData(x.name, path, duration, numOfAlbums, numOfTracks)
                            musicDatabase.artistDao().insert(insert)
                        }
                    }
                }
                update()
                playerQueue.saveUpdate(true, true, true, true)
                this.runOnUiThread {
                    Toast.makeText(this, "Done scanning", Toast.LENGTH_SHORT).show()
                }
            }
        }
        val insertThread = Thread(insert)
        insertThread.start()
    }
}