package com.arewabeatz.youtubeclone.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arewabeatz.youtubeclone.Adapters.VideoAdapter
import com.arewabeatz.youtubeclone.Models.PostModel
import com.arewabeatz.youtubeclone.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    lateinit var videoList: List<PostModel>
    private lateinit var reference: DatabaseReference
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var shimmerLayout : ShimmerFrameLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_home, container, false)

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true

        //initialiing shimmer layout
        shimmerLayout = view.findViewById(R.id.shimmer_layout)
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()

        //setting up recyclerview
        recyclerView = view.findViewById(R.id.homeVideosRecycler)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        reference = FirebaseDatabase.getInstance().reference.child("Videos")
        videoList = ArrayList()

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    for (ds in snapshot.children) {

                        val video: PostModel? = ds.getValue(PostModel::class.java)
                        if (video != null) {
                            (videoList as ArrayList<PostModel>).add(video)
                        }

                    }
                    if (context != null){
                        //dismissing shimmer layout and showing recyclerview content
                        videoAdapter = VideoAdapter(context!!, videoList)
                        recyclerView.adapter = videoAdapter
                        videoAdapter.notifyDataSetChanged()
                        shimmerLayout.stopShimmer()
                        shimmerLayout.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE

                    }
                }
                else{
                   view.noVideos.visibility = View.VISIBLE
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                }

            }

            override fun onCancelled(error: DatabaseError) {


            }
        })

        return view
    }

}