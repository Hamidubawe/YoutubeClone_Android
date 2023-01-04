package com.arewabeatz.youtubeclone.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arewabeatz.youtubeclone.Models.PlaylistModel
import com.arewabeatz.youtubeclone.PlaylistActivity
import com.arewabeatz.youtubeclone.R
import com.squareup.picasso.Picasso

class PlaylistAdapter(private val context: Context, private val list: List<PlaylistModel>)
    :RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    override fun getItemCount(): Int {

        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.playlist_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val videoList = list[position]

        Picasso.get().load(videoList.pThumbnail).placeholder(R.drawable.ic_play)
            .into(holder.videoThumbNail)
        holder.videoTitle.text = videoList.pName
        holder.videoDate.visibility = View.GONE
        holder.videoViews.visibility = View.GONE

        holder.itemView.setOnClickListener {

            val i = Intent(context, PlaylistActivity::class.java)
            i.putExtra("playlist", videoList.pName)
            i.putExtra("playlistDesc", videoList.pDescription)
            context.startActivity(i)
        }

    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val videoThumbNail: ImageView = itemView.findViewById(R.id.playlistImg)
        val videoTitle: TextView = itemView.findViewById(R.id.descriptionPlaylist)
        val videoDate: TextView = itemView.findViewById(R.id.videoDate)
        val videoViews: TextView = itemView.findViewById(R.id.videoViews)
    }
}