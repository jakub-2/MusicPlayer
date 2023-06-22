package com.example.musicplayer.mainFragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.PlayerQueue
import com.example.musicplayer.R
import com.example.musicplayer.albumdetail.AlbumDetailActivity
import com.example.musicplayer.databaseparts.MusicDatabase
import com.example.musicplayer.databinding.FragmentAlbumsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Albums(private val musicPlayer: MediaPlayer, private val playerQueue: PlayerQueue) : Fragment() {
    private lateinit var binding: FragmentAlbumsBinding
    private lateinit var musicDatabase: MusicDatabase

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAlbumsBinding.inflate(layoutInflater)
        musicDatabase = MusicDatabase.getDatabase(requireContext())
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result ->

            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data!!.getBooleanExtra("selected", false)) {
                    playerQueue.readSavedQueue()
                    playerQueue.setUpdatePlayerStart(true)
                    musicPlayer.reset()
                }
            } else {
                playerQueue.readUpdate()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_albums, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.albums_recycler)
        update()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun update() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.hasFixedSize()
        GlobalScope.launch(Dispatchers.IO) {
            val albums = musicDatabase.albumDao().getAllOrdered()
            Log.i("albums", albums.size.toString())
            val adapter = AlbumsRecyclerAdapter(context, albums)
            withContext(Dispatchers.Main) {
                recyclerView.adapter = adapter
                adapter.setOnItemClickListener(object : AlbumsRecyclerAdapter.OnItemClickListener {
                    override fun onItemClick(position: Int) {

                        val intent = Intent(context, AlbumDetailActivity::class.java)
                        val name = albums[position].album_name
                        val path = albums[position].image_route
                        val year = albums[position].release_year
                        val numOfTracks = albums[position].num_of_tracks
                        val duration = albums[position].duration

                        val bundle = Bundle()
                        bundle.putString("name", name)
                        bundle.putString("path", path)
                        bundle.putInt("year", year ?: 0)
                        bundle.putInt("tracks", numOfTracks)
                        bundle.putInt("duration", duration)
                        bundle.putSerializable("queue", playerQueue)
                        intent.putExtras(bundle)
                        resultLauncher.launch(intent)
//                        startActivity(intent)
                    }
                })
                adapter.notifyDataSetChanged()
            }
        }
    }
}