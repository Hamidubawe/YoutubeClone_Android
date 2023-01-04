package com.arewabeatz.youtubeclone.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arewabeatz.youtubeclone.ChannelActivity
import com.arewabeatz.youtubeclone.Models.PlaylistModel
import com.arewabeatz.youtubeclone.Models.PostModel
import com.arewabeatz.youtubeclone.Models.UserModel
import com.arewabeatz.youtubeclone.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_video_player.*
import java.lang.Exception

class SubsAdapter(private val context: Context, private val list: List<UserModel>)
    : RecyclerView.Adapter<SubsAdapter.Holder>() {

    override fun getItemCount(): Int {

        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {

        val v = LayoutInflater.from(context).inflate(R.layout.subs_layout, parent, false)
        return Holder(v)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val user = list[position]
        holder.subName.text = user.name
        try {
            Picasso.get().load(user.profilePhoto).into(holder.subsPhoto)
        }catch (e:Exception){
            holder.subsPhoto.setImageResource(R.drawable.ic_explore)
        }
        getSubscribers(user.userId, holder.subsCount)
        getVideos(user.userId, holder.videosCount)

        holder.itemView.setOnClickListener {
            val i = Intent(context, ChannelActivity::class.java)
            i.putExtra("userId", user.userId)
            context.startActivity(i)
        }

    }

    private fun getSubscribers(userId: String, channelSubscribers:TextView ) {
        val reff = FirebaseDatabase.getInstance().reference
            .child("Subscribers").child(userId)
        reff.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    val subNumber = snapshot.childrenCount.toString()
                    channelSubscribers.text = "$subNumber Subscribers"
                }
            }

            override fun onCancelled(error: DatabaseError) {


            }
        })
    }

    private fun getVideos(userId: String, videosCount: TextView){

        FirebaseDatabase.getInstance().reference.child("Videos")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    var songNumber = 0

                    for (ds in snapshot.children) {

                        val song = ds.getValue(PostModel::class.java)
                        if (song!!.publisher == userId) {
                            songNumber +=1

                            videosCount.text = "${songNumber.toString()} Videos"
                        }

                    }

                }

                override fun onCancelled(error: DatabaseError) {


                }

            })

    }

    class Holder(itemView: View):RecyclerView.ViewHolder(itemView){

        val subsPhoto: CircleImageView = itemView.findViewById(R.id.subsPhoto)
        val subName:TextView = itemView.findViewById(R.id.subName)
        val subsCount:TextView = itemView.findViewById(R.id.subsCount)
        val videosCount:TextView = itemView.findViewById(R.id.videosCount)
        val subscribe: TextView = itemView.findViewById(R.id.subscribe)


    }
}