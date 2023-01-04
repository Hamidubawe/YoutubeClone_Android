package com.arewabeatz.youtubeclone.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arewabeatz.youtubeclone.Models.PostModel
import com.arewabeatz.youtubeclone.R
import com.arewabeatz.youtubeclone.VideoPlayerActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class VideoListAdapter(private val context: Context, private val list: List<PostModel>)
    :RecyclerView.Adapter<VideoListAdapter.ViewHolder>(){

    override fun getItemCount(): Int {

        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.playlist_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val videoList = list[position]

        Picasso.get().load(videoList.videoThumbnail).placeholder(R.drawable.ic_play).into(holder.videoThumbNail)
        holder.videoTitle.text = videoList.videoTitle
        holder.videoDate.text = videoList.videoDate
        getPlayedTimes(videoList.videoId, holder.videoViews)

        holder.itemView.setOnClickListener {

            FirebaseDatabase.getInstance().reference.child("Plays")
                .child(videoList.videoId).push().setValue("played")

            val i = Intent(context, VideoPlayerActivity::class.java)
            i.putExtra("videoId", videoList.videoId)
            context.startActivity(i)
        }

    }

    private fun getPlayedTimes(videoId: String, play: TextView) {

        val ref = FirebaseDatabase.getInstance().reference.child("Plays")
            .child(videoId)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val playCount = snapshot.childrenCount.toInt()
                    play.text = "${playCount.toString()} views"
                }

            }

            override fun onCancelled(error: DatabaseError) {


            }
        })

    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val videoThumbNail: ImageView = itemView.findViewById(R.id.playlistImg)
        val videoTitle: TextView = itemView.findViewById(R.id.descriptionPlaylist)
        val videoDate: TextView = itemView.findViewById(R.id.videoDate)
        val videoViews: TextView = itemView.findViewById(R.id.videoViews)
    }
}