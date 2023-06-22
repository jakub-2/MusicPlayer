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
import com.example.musicplayer.databaseparts.AlbumData

class AlbumsRecyclerAdapter(private var context: Context?,private var albums: List<AlbumData>) :
    RecyclerView.Adapter<AlbumsRecyclerAdapter.MyViewHolder>() {
    private lateinit var mListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v: View = LayoutInflater.from(context).inflate(R.layout.album_item, parent, false)
        return MyViewHolder(v, mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.text = albums[position].album_name
        holder.trackCount.text = context?.getString(R.string.albums_tracks, albums[position].num_of_tracks) ?: ""
        holder.albumLength.text = context?.getString(R.string.albums_duration, convert(albums[position].duration.toString())[0].toInt())
        if (albums[position].image_route != null) {
            holder.image.setImageURI(Uri.parse(albums[position].image_route))
        }
    }

    override fun getItemCount(): Int {
        return albums.size
    }

    private fun convert(time: String?): List<String> {
        val milliseconds = time?.toLong()
        val minutes =  (milliseconds?.div(1000) ?: 0) / 60
        val seconds = (milliseconds?.div(1000) ?: 0) % 60
        if (seconds < 10) return listOf(minutes.toString(), "0$seconds") else return listOf(minutes.toString(), seconds.toString())
    }

    class MyViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView
        var name: TextView
        var trackCount: TextView
        var albumLength: TextView
        init {
            image = itemView.findViewById(R.id.album_image)
            image.layoutParams.height = 200
            image.layoutParams.width = 200
            name = itemView.findViewById(R.id.album_name)
            trackCount = itemView.findViewById(R.id.track_count)
            albumLength = itemView.findViewById(R.id.album_length)

            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
    }
}