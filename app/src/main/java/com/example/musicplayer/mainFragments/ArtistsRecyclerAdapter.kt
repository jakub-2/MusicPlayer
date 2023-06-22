package com.example.musicplayer.mainFragments

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.databaseparts.ArtistData

class ArtistsRecyclerAdapter(private var context: Context?, private var artists: List<ArtistData>) :
    RecyclerView.Adapter<ArtistsRecyclerAdapter.MyViewHolder>() {
    private lateinit var mListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v: View = LayoutInflater.from(context).inflate(R.layout.artist_item, parent, false)
        return MyViewHolder(v, mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.text = artists[position].name
        holder.numberOfAlbums.text = context?.getString(R.string.scan_album, artists[position].num_of_albums) ?: ""
        holder.numberOfTracks.text = context?.getString(R.string.scan_track, artists[position].total_num_of_tracks) ?: ""
        if (artists[position].image_route != null) {
            holder.image.setImageURI(Uri.parse(artists[position].image_route))
        }
    }

    override fun getItemCount(): Int {
        return artists.size
    }

    class MyViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView
        var name: TextView
        var numberOfAlbums: TextView
        var numberOfTracks: TextView
        init {
            image = itemView.findViewById(R.id.artist_image)
            image.layoutParams.height = 200
            image.layoutParams.width = 200
            name = itemView.findViewById(R.id.artist_name)
            numberOfAlbums = itemView.findViewById(R.id.number_of_albums)
            numberOfTracks = itemView.findViewById(R.id.number_of_tracks)

            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }
}