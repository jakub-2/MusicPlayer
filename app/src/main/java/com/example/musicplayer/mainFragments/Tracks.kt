package com.example.musicplayer.mainFragments

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.PlayerQueue
import com.example.musicplayer.R
import com.example.musicplayer.databaseparts.MusicDatabase
import com.example.musicplayer.databaseparts.TrackData
import com.example.musicplayer.databinding.FragmentTracksBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class Tracks(private val musicPlayer: MediaPlayer, private val playerQueue: PlayerQueue) : Fragment() {
    private lateinit var binding: FragmentTracksBinding
    private lateinit var musicDatabase: MusicDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TracksRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentTracksBinding.inflate(layoutInflater)
        musicDatabase = MusicDatabase.getDatabase(requireContext())
        Log.i("track", "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.i("track", "onCreateView")
        return inflater.inflate(R.layout.fragment_tracks, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("track", "onViewCreated")
        recyclerView = view.findViewById(R.id.tracks_recycler)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(false)
        update()
    }

//    override fun onResume() {
//        super.onResume()
//        if (playerQueue.getUpdateTracks()) {
//            Log.i("update", "update tracks")
//            update()
//            playerQueue.setUpdateTracks(false)
//        }
//    }

    @SuppressLint("NotifyDataSetChanged")
    fun update() {
        GlobalScope.launch {
            val tracks = musicDatabase.trackDao().getAllOrdered() as MutableList
            val albums = musicDatabase.albumDao().getAll() as MutableList
            adapter = TracksRecyclerAdapter(context, tracks, albums, playerQueue, musicDatabase)
            withContext(Dispatchers.Main) {
                adapter.setOnItemClickListener(object : TracksRecyclerAdapter.OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        playerQueue.setQueue(tracks)
                        playerQueue.setIndex(position)
                        playerQueue.setPosition(0)
                        playerQueue.saveState()
                        playerQueue.saveQueue()
                        playerQueue.setUpdatePlayerStart(true)
                        musicPlayer.stop()
                        musicPlayer.reset()
                        musicPlayer.setDataSource(playerQueue.getTrackData().track_route)
                        musicPlayer.prepare()
                        musicPlayer.start()
                    }
                })
                adapter.setOnOptionsMenuClickListener(object : TracksRecyclerAdapter.OptionsMenuClickListener {
                    override fun onOptionsMenuClicked(position: Int, tracks: MutableList<TrackData>) {
                        optionMenuClicked(position, tracks)
                    }
                })
                recyclerView.adapter = adapter
                adapter.notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun optionMenuClicked(position: Int, tracks: MutableList<TrackData>) {
        val popupMenu = PopupMenu(context, recyclerView.layoutManager!!.findViewByPosition(position)!!.findViewById(R.id.track_option_button))
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
                        playerQueue.setUpdateTracks(false)
                        playerQueue.setUpdateAlbums(true)
                        playerQueue.setUpdateArtists(true)
                        playerQueue.saveUpdate(true)
                        tracks.removeAt(position)
                        Log.i("update", "Update done")
                        withContext(Dispatchers.Main) {
                            adapter.notifyDataSetChanged()
                            Log.i("update", "adapter notified")
                        }
                    }

                    return@setOnMenuItemClickListener true
                }
                R.id.track_add_queue -> {
                    playerQueue.append(tracks[position])
                    playerQueue.saveQueue()
                    playerQueue.setUpdate(true)
                    Log.i("2", "adding to queue")
                    Toast.makeText(context, "Added to queue", Toast.LENGTH_SHORT).show()
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        popupMenu.show()
    }

}