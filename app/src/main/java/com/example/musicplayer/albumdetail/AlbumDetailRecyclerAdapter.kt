package com.example.musicplayer.albumdetail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.databaseparts.TrackData

class AlbumDetailRecyclerAdapter(private var context: Context?, track: MutableList<TrackData>) :
    RecyclerView.Adapter<AlbumDetailRecyclerAdapter.MyViewHolder>() {

    private var tracks: MutableList<TrackData> = track
    private lateinit var mListener: OnItemClickListener
    private lateinit var optionsMenuClickListener: OnOptionsMenuClickListener


    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    interface OnOptionsMenuClickListener{
        fun onOptionsMenuClicked(position: Int, tracks: MutableList<TrackData>)
    }

    fun setOnOptionsMenuClickListener(listener: OnOptionsMenuClickListener) {
        optionsMenuClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v: View = LayoutInflater.from(context).inflate(R.layout.album_track_item, parent, false)
        return MyViewHolder(v, mListener, optionsMenuClickListener, tracks)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.num.text = context?.getString (R.string.number, position + 1) ?: ""
        holder.title.text = tracks[position].title
        holder.artist.text = tracks[position].artist_name
    }

    override fun getItemCount(): Int {
        return tracks.size
    }

    class MyViewHolder(itemView: View, listener: OnItemClickListener, optionListener: OnOptionsMenuClickListener, tracks: MutableList<TrackData>) : RecyclerView.ViewHolder(itemView) {
        var num: TextView
        var title: TextView
        var artist: TextView
        var optionsButton: TextView
        init {
            num = itemView.findViewById(R.id.album_detail_track_number)
            title = itemView.findViewById(R.id.album_detail_title)
            artist = itemView.findViewById(R.id.album_detail_artist)
            optionsButton = itemView.findViewById(R.id.album_track_option_button)
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
            optionsButton.setOnClickListener {
                optionListener.onOptionsMenuClicked(adapterPosition, tracks)
            }
        }
    }

}