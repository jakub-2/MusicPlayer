package com.example.musicplayer.artistdetail

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.PlayerQueue
import com.example.musicplayer.R
import com.example.musicplayer.albumdetail.AlbumDetailActivity
import com.example.musicplayer.databaseparts.MusicDatabase
import com.example.musicplayer.databaseparts.TrackData
import com.example.musicplayer.databinding.ActivityArtistDetailBinding
import com.example.musicplayer.mainFragments.TracksRecyclerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArtistDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArtistDetailBinding
    private lateinit var playerQueue: PlayerQueue
    private lateinit var trackRecyclerView: RecyclerView
    private val musicDatabase = MusicDatabase.getDatabase(this)
    private lateinit var trackAdapter: TracksRecyclerAdapter
    private var numOfTracks: Int = 0
    private var numOfAlbums: Int = 0
    private lateinit var artistName: String

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtistDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = this.intent.extras
        artistName = bundle!!.getString("artist_name")!!
        numOfTracks = bundle.getInt("artist_num_of_tracks")
        numOfAlbums = bundle.getInt("artist_num_of_albums")
        playerQueue = bundle.getSerializable("queue") as PlayerQueue

        binding.artistDetailName.text = artistName
        binding.artistDetailNumbers.text = getString(R.string.artist_details, numOfAlbums, numOfTracks)

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data!!.getBooleanExtra("selected", false)) {
                    val data = Intent()
                    data.putExtra("selected", true)
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }
            }
        }

        // albums
        val albumsRecyclerView: RecyclerView = binding.albumsRecycler
        albumsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        albumsRecyclerView.hasFixedSize()
        GlobalScope.launch(Dispatchers.IO) {
            val albums = musicDatabase.albumDao().getOrderedByArtist("%$artistName%")
            val adapter = ArtistDetailRecyclerAdapter(this@ArtistDetailActivity, albums)
            withContext(Dispatchers.Main) {
                albumsRecyclerView.adapter = adapter
                adapter.setOnItemClickListener(object : ArtistDetailRecyclerAdapter.OnItemClickListener{
                    override fun onItemClick(position: Int) {
                        val intent = Intent(this@ArtistDetailActivity, AlbumDetailActivity::class.java)
                        val name = albums[position].album_name
                        val path = albums[position].image_route
                        val year = albums[position].release_year
                        val numOfTracks = albums[position].num_of_tracks
                        val duration = albums[position].duration

                        val albumBundle = Bundle()
                        albumBundle.putString("name", name)
                        albumBundle.putString("path", path)
                        albumBundle.putInt("year", year ?: 0)
                        albumBundle.putInt("tracks", numOfTracks)
                        albumBundle.putInt("duration", duration)
                        albumBundle.putSerializable("queue", playerQueue)
                        intent.putExtras(albumBundle)
                        resultLauncher.launch(intent)
                    }
                })
                adapter.notifyDataSetChanged()
            }
        }

        // tracks
        trackRecyclerView = binding.tracksRecycler
        trackRecyclerView.layoutManager = LinearLayoutManager(this)
        trackRecyclerView.hasFixedSize()
        GlobalScope.launch(Dispatchers.IO) {
            val tracks = musicDatabase.trackDao().getAllOrderedByArtist(artistName) as MutableList
            val albums = musicDatabase.albumDao().getAll() as MutableList
            trackAdapter = TracksRecyclerAdapter(
                this@ArtistDetailActivity,
                tracks,
                albums,
                playerQueue,
                musicDatabase
            )
            withContext(Dispatchers.Main) {
                trackRecyclerView.adapter = trackAdapter
                trackAdapter.setOnItemClickListener(object : TracksRecyclerAdapter.OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        playerQueue.setQueue(tracks)
                        playerQueue.setIndex(position)
                        playerQueue.setPosition(0)
                        playerQueue.saveState()
                        playerQueue.saveQueue()
                        val data = Intent()
                        data.putExtra("selected", true)
                        setResult(Activity.RESULT_OK, data)
                        finish()
                    }
                })
                trackAdapter.setOnOptionsMenuClickListener(object : TracksRecyclerAdapter.OptionsMenuClickListener {
                    override fun onOptionsMenuClicked(position: Int, tracks: MutableList<TrackData>) {
                        optionMenuClicked(position, tracks)
                    }
                })
                trackAdapter.notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun optionMenuClicked(position: Int, tracks: MutableList<TrackData>) {
        val popupMenu = PopupMenu(this, trackRecyclerView[position].findViewById(R.id.track_option_button))
        popupMenu.inflate(R.menu.track_menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.track_delete -> {
                    playerQueue.setDelete(tracks[position].id_track)
                    playerQueue.saveQueue()
                    GlobalScope.launch (Dispatchers.IO) {
                        musicDatabase.trackDao().setDelete(true, tracks[position].id_track)
                        if (musicDatabase.albumDao().getAlbumNumOfTracks(tracks[position].album_name) == 1) {
                            musicDatabase.albumDao().delete(musicDatabase.albumDao().getAlbum(tracks[position].album_name))
                        } else {
                            val totalDuration = musicDatabase.albumDao().getAlbumsDuration(tracks[position].album_name)
                            musicDatabase.albumDao().updateTrackInfo(tracks[position].album_name, totalDuration - tracks[position].duration)
                        }
                        if (musicDatabase.albumDao().getAlbumCountByArtist("%" + tracks[position].artist_name + "%") == 0 || musicDatabase.trackDao().getTrackCountByArtist("%" + tracks[position].artist_name + "%") == 0) {
                            musicDatabase.artistDao().delete(musicDatabase.artistDao().getByArtist(tracks[position].artist_name))
                        } else {
                            val numOfAlbums = musicDatabase.albumDao().getAlbumCountByArtist("%" + tracks[position].artist_name + "%")
                            val duration = musicDatabase.albumDao().getAlbumDuration("%" + tracks[position].artist_name + "%")
                            val numOfTracks = musicDatabase.trackDao().getTrackCountByArtist(tracks[position].artist_name)
                            musicDatabase.artistDao().updateNums(tracks[position].artist_name, numOfAlbums, numOfTracks, duration)
                        }
                        if (tracks.size == 1) {
                            playerQueue.saveUpdate(true, true, true, true)
                            finish()
                        } else {
                            tracks.removeAt(position)
                            val artist = musicDatabase.artistDao().getByArtist(artistName)
                            numOfTracks = artist.total_num_of_tracks
                            numOfAlbums = artist.num_of_albums

                            withContext(Dispatchers.Main) {
                                binding.artistDetailNumbers.text = getString(R.string.artist_details, numOfAlbums, numOfTracks)
                                trackAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                    return@setOnMenuItemClickListener true
                }
                R.id.track_add_queue -> {
                    playerQueue.append(tracks[position])
                    playerQueue.saveQueue()
                    playerQueue.saveUpdate(true)
                    Toast.makeText(this, "Added to queue", Toast.LENGTH_SHORT).show()
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        popupMenu.show()
    }
}