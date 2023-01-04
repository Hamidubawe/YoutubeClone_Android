package com.arewabeatz.youtubeclone.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arewabeatz.youtubeclone.Adapters.VideoAdapter
import com.arewabeatz.youtubeclone.Models.PostModel
import com.arewabeatz.youtubeclone.Models.UserModel
import com.arewabeatz.youtubeclone.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_explore.view.*
import kotlinx.android.synthetic.main.fragment_explore.view.noVideos
import kotlinx.android.synthetic.main.fragment_home.view.*


class ExploreFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    lateinit var videoList: List<PostModel>
    private lateinit var reference: DatabaseReference
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var shimmerLayout : ShimmerFrameLayout
    private var interest: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true

        shimmerLayout = view.findViewById(R.id.shimmer_layout)
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()

        recyclerView = view.findViewById(R.id.exploreVideosRecycler)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        reference = FirebaseDatabase.getInstance().reference.child("Videos")
        videoList = ArrayList()

        if (FirebaseAuth.getInstance().currentUser != null){
            val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
            val ref = FirebaseDatabase.getInstance().reference.child("Users")

            ref.child(currentUser).addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserModel::class.java)

                    interest = user!!.interest
                    getVideos(interest, context)
                }

                override fun onCancelled(error: DatabaseError) {


                }
            })


        }
        else{

            view.noVideos.visibility = View.VISIBLE
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
        }

        return view
    }

    private fun getVideos(interest: String, context: Context?) {

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    for (ds in snapshot.children) {

                        val video: PostModel? = ds.getValue(PostModel::class.java)
                        if (video!!.category == interest) {
                            (videoList as ArrayList<PostModel>).add(video)
                        }

                    }
                    if (context != null){
                        videoAdapter = VideoAdapter(context, videoList)
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
    }

}