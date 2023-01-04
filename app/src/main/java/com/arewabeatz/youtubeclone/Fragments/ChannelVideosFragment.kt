package com.arewabeatz.youtubeclone.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arewabeatz.youtubeclone.Adapters.VideoListAdapter
import com.arewabeatz.youtubeclone.ChannelActivity
import com.arewabeatz.youtubeclone.Models.PostModel
import com.arewabeatz.youtubeclone.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.database.*


class ChannelVideosFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    lateinit var videoList: List<PostModel>
    private lateinit var reference: DatabaseReference
    private lateinit var videoAdapter: VideoListAdapter
    private lateinit var shimmerLayout : ShimmerFrameLayout

    //this fragment show the videos of the channel you are viewing

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_channel_videos, container, false)

        val userId = ChannelActivity.userId

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true

        shimmerLayout = v.findViewById(R.id.shimmer_layout)
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()

        recyclerView = v.findViewById(R.id.channelUploads)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        reference = FirebaseDatabase.getInstance().reference.child("Videos")
        videoList = ArrayList()

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    for (ds in snapshot.children) {

                        val video: PostModel? = ds.getValue(PostModel::class.java)
                        if (video!!.publisher == userId) {
                            (videoList as ArrayList<PostModel>).add(video)
                        }

                    }
                    if (context != null){
                        videoAdapter = VideoListAdapter(context!!, videoList)
                        recyclerView.adapter = videoAdapter
                        videoAdapter.notifyDataSetChanged()
                        shimmerLayout.stopShimmer()
                        shimmerLayout.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE

                    }
                }
                else{
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                }

            }

            override fun onCancelled(error: DatabaseError) {


            }
        })

        return v
    }
}