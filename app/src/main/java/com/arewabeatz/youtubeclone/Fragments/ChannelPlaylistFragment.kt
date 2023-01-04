package com.arewabeatz.youtubeclone.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arewabeatz.youtubeclone.Adapters.PlaylistAdapter
import com.arewabeatz.youtubeclone.Adapters.VideoAdapter
import com.arewabeatz.youtubeclone.ChannelActivity
import com.arewabeatz.youtubeclone.Models.PlaylistModel
import com.arewabeatz.youtubeclone.Models.PostModel
import com.arewabeatz.youtubeclone.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_home.view.*

class ChannelPlaylistFragment : Fragment() {

    val userId = ChannelActivity.userId
    private lateinit var recyclerView: RecyclerView
    lateinit var playList: List<PlaylistModel>
    private lateinit var reference: DatabaseReference
    private lateinit var videoAdapter: PlaylistAdapter
    private lateinit var shimmerLayout : ShimmerFrameLayout

    //this fragment show the playlists of the channel you are viewing

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_channel_playlist, container, false)

        reference = FirebaseDatabase.getInstance().reference.child("Playlist")

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true

        shimmerLayout = view.findViewById(R.id.shimmer_layout)
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()

        recyclerView = view.findViewById(R.id.channelUploads)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        playList = ArrayList()

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){

                    for (ds in snapshot.children) {

                        val list = ds.getValue(PlaylistModel::class.java)

                        if (list!!.pUid == userId) {
                            (playList as ArrayList<PlaylistModel>).add(list)
                        }

                    }
                    if (context != null){
                        videoAdapter = PlaylistAdapter(context!!, playList)
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

        return view

    }

}