package com.arewabeatz.youtubeclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arewabeatz.youtubeclone.Adapters.PlaylistAdapter
import com.arewabeatz.youtubeclone.Adapters.VideoAdapter
import com.arewabeatz.youtubeclone.Adapters.VideoListAdapter
import com.arewabeatz.youtubeclone.Fragments.HomeFragment
import com.arewabeatz.youtubeclone.Models.PlaylistModel
import com.arewabeatz.youtubeclone.Models.PostModel
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_playlist.*

class PlaylistActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    lateinit var videoList: List<PostModel>
    private lateinit var reference: DatabaseReference
    private lateinit var videoAdapter: VideoListAdapter
    private lateinit var shimmerLayout : ShimmerFrameLayout
    var playlist = ""
    var playlistDescr = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)
        setSupportActionBar(playlistToolbar)

        //getting intent
        playlist = intent.getStringExtra("playlist")!!
        playlistDescr = intent.getStringExtra("playlistDesc")!!
        supportActionBar!!.title = playlist

        playlistName.text = playlist
        playlistDesc.text = playlistDescr

        //setting the linear layout for the recycler
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true

        //initializing the shimmer layout
        shimmerLayout = findViewById(R.id.shimmer_layout)
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()

        recyclerView = findViewById(R.id.channelUploads)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)


        reference = FirebaseDatabase.getInstance().reference.child("Videos")
        videoList = ArrayList()

        //getting videos in the playlist
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    for (ds in snapshot.children) {

                        val video: PostModel? = ds.getValue(PostModel::class.java)
                        if (video!!.playlist == playlist) {
                            (videoList as ArrayList<PostModel>).add(video)
                        }

                    }

                    videoAdapter = VideoListAdapter(this@PlaylistActivity, videoList)
                    recyclerView.adapter = videoAdapter
                    videoAdapter.notifyDataSetChanged()
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                }
                else{
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                }

            }

            override fun onCancelled(error: DatabaseError) {


            }
        })
    }
}