package com.arewabeatz.youtubeclone.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arewabeatz.youtubeclone.Adapters.RecentPlayedAdapter
import com.arewabeatz.youtubeclone.Adapters.VideoAdapter
import com.arewabeatz.youtubeclone.ChannelActivity
import com.arewabeatz.youtubeclone.Models.PostModel
import com.arewabeatz.youtubeclone.R
import com.arewabeatz.youtubeclone.SignInActivity
import com.arewabeatz.youtubeclone.UploadVideoActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_library.view.*

class LibraryFragment : Fragment() {

    private lateinit var playedList: List<String>
    private lateinit var videoList: List<PostModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)


        playedList =ArrayList()
        videoList = ArrayList()
        reference = FirebaseDatabase.getInstance().reference.child("Videos")

        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true)
        linearLayoutManager.stackFromEnd = true
        linearLayoutManager.reverseLayout = true

        //recently watched videos recyclerview
        recyclerView = view.findViewById(R.id.recentPlayedVideos)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)


        view.channelRelative.setOnClickListener {
            //open your own youtube channel
            if (FirebaseAuth.getInstance().currentUser != null){
                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                val i = Intent(context, ChannelActivity::class.java)
                i.putExtra("userId", uid)
                startActivity(i)
            }
            else{

                val i = Intent(context, SignInActivity::class.java)
                startActivity(i)
            }

        }

        view.videosRelatve.setOnClickListener {

            //addvideo
            if (FirebaseAuth.getInstance().currentUser != null){
                val i = Intent(context, UploadVideoActivity::class.java)
                startActivity(i)
            }

        }

        if (FirebaseAuth.getInstance().currentUser != null) {

            //getting the last videos played by the curent user
            val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
            val ref =FirebaseDatabase.getInstance().reference
                .child("Played").child(currentUser)

            ref.addValueEventListener(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {

                    for (ds in snapshot.children){
                        val videoPlayed = ds.key
                        (playedList as ArrayList<String>).add(videoPlayed!!)
                    }
                }

                override fun onCancelled(error: DatabaseError) {


                }
            })

            reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if(snapshot.exists()){
                        for (ds in snapshot.children) {

                            val video: PostModel? = ds.getValue(PostModel::class.java)
                            for (played in playedList){

                                if (video!!.videoId == played) {
                                    (videoList as ArrayList<PostModel>).add(video)
                                }
                            }


                        }
                        if (context != null){
                            videoAdapter = RecentPlayedAdapter(context!!, videoList)
                            recyclerView.adapter = videoAdapter
                            videoAdapter.notifyDataSetChanged()
                            recyclerView.visibility = View.VISIBLE

                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {


                }
            })
        }

        return view
    }

    private lateinit var reference: DatabaseReference
    private lateinit var videoAdapter: RecentPlayedAdapter
    private lateinit var recyclerView: RecyclerView
}