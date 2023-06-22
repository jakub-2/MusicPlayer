package com.example.musicplayer.albumdetail

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.TypedArray
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.PlayerQueue
import com.example.musicplayer.R
import com.example.musicplayer.databaseparts.MusicDatabase
import com.example.musicplayer.databaseparts.TrackData
import com.example.musicplayer.databinding.ActivityAlbumDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class AlbumDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlbumDetailBinding
    private lateinit var playerQueue: PlayerQueue
    private lateinit var trackRecyclerView: RecyclerView
    private val musicDatabase = MusicDatabase.getDatabase(this)
    private lateinit var adapteris: AlbumDetailRecyclerAdapter

    private var numOfTracks: Int = 0
    private var duration: Int = 0
    private lateinit var albumName: String

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAlbumDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = this.intent.extras
        albumName = bundle!!.getString("name")!!
        val imagePath = bundle.getString("path")
        val year = bundle.getInt("year")
        numOfTracks = bundle.getInt("tracks")
        duration = bundle.getInt("duration")
        playerQueue = bundle.getSerializable("queue") as PlayerQueue
        if (imagePath != null) {
            val image = binding.imageView
            image.setImageURI(Uri.parse(imagePath))
            image.layoutParams.height = dpToPx(300, this)
            image.layoutParams.width = dpToPx(300, this)
        } else {
            val ta: TypedArray = this.obtainStyledAttributes(
                R.style.record,
                intArrayOf(android.R.attr.drawable)
            )
            val test = ta.getDrawable(0)
            ta.recycle()
            binding.imageView.setImageDrawable(test)
            binding.imageView.layoutParams.height = dpToPx(300, this)
            binding.imageView.layoutParams.width = dpToPx(300, this)
        }

        binding.albumDetailMainName.text = albumName

        if (year == 0) {
            binding.albumDetailYear.text = ""
        } else {
            binding.albumDetailYear.text = year.toString()
        }
        binding.albumDetailTracks.text = getString(R.string.albums_tracks, numOfTracks)
        binding.albumDetailDuration.text = getString(R.string.albums_duration, duration / 1000 / 60)

        // tracks
        trackRecyclerView = binding.albumDetailTracksRecycler
        trackRecyclerView.layoutManager = LinearLayoutManager(this)
        trackRecyclerView.hasFixedSize()

        GlobalScope.launch(Dispatchers.IO) {
            val tracks = musicDatabase.trackDao().getAllByAlbum(albumName) as MutableList
            adapteris = AlbumDetailRecyclerAdapter(this@AlbumDetailActivity, tracks)
            withContext(Dispatchers.Main) {
                trackRecyclerView.adapter = adapteris
                adapteris.setOnItemClickListener(object : AlbumDetailRecyclerAdapter.OnItemClickListener{
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
                adapteris.setOnOptionsMenuClickListener(object : AlbumDetailRecyclerAdapter.OnOptionsMenuClickListener{
                    override fun onOptionsMenuClicked(position: Int, tracks: MutableList<TrackData>) {
                        optionMenuClicked(position, tracks)
                    }
                })
                adapteris.notifyDataSetChanged()
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun optionMenuClicked(position: Int, tracks: MutableList<TrackData>) {
        val popupMenu = PopupMenu(this, trackRecyclerView.layoutManager!!.findViewByPosition(position)!!.findViewById(R.id.album_track_option_button))
        popupMenu.inflate(R.menu.track_menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.track_delete -> {
                    Log.i("1", "delete")
                    playerQueue.setDelete(tracks[position].id_track)
                    playerQueue.saveQueue()
//                    playerQueue.setUpdateTracks(true)
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
                            val album = musicDatabase.albumDao().getAlbum(albumName)
                            numOfTracks = album.num_of_tracks
                            duration = album.duration
                            Log.i("update", "Update done")
                            withContext(Dispatchers.Main) {
                                binding.albumDetailTracks.text = getString(R.string.albums_tracks, numOfTracks)
                                binding.albumDetailDuration.text = getString(R.string.albums_duration, duration / 1000 / 60)
                                adapteris.notifyDataSetChanged()
                                Log.i("update", "adapter notified")
                            }
                            playerQueue.saveUpdate(true, true, true, true)
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

    private fun dpToPx(dp: Int, context: Context): Int {
        val density = context.resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }
}