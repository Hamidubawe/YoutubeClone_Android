package com.arewabeatz.youtubeclone.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.arewabeatz.youtubeclone.Models.PostModel
import com.arewabeatz.youtubeclone.R
import com.arewabeatz.youtubeclone.VideoPlayerActivity
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class RecentPlayedAdapter(val context: Context, private val  list: List<PostModel>)
    : RecyclerView.Adapter<RecentPlayedAdapter.VideoHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.thumbnail_layout,parent,false)

        return VideoHolder(view)
    }

    override fun getItemCount(): Int {

        return list.size
    }

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        val video = list[position]
        try {
            Picasso.get().load(list[position].videoThumbnail).into(holder.videoThumbNail)
        }catch (e:Exception){
            holder.videoThumbNail.setImageResource(R.drawable.ic_play)
        }

        holder.itemView.setOnClickListener {

            FirebaseDatabase.getInstance().reference.child("Plays")
                .child(video.videoId).push().setValue("played")

            val i = Intent(context, VideoPlayerActivity::class.java)
            i.putExtra("videoId", video.videoId)
            context.startActivity(i)
        }

    }


    class VideoHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val videoThumbNail: ImageView = itemView.findViewById(R.id.playedListImg)

    }
}