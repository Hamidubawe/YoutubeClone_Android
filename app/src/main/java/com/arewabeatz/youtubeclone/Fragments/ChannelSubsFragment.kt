package com.arewabeatz.youtubeclone.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arewabeatz.youtubeclone.Adapters.PlaylistAdapter
import com.arewabeatz.youtubeclone.Adapters.SubsAdapter
import com.arewabeatz.youtubeclone.ChannelActivity
import com.arewabeatz.youtubeclone.Models.PlaylistModel
import com.arewabeatz.youtubeclone.Models.UserModel
import com.arewabeatz.youtubeclone.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.database.*

class ChannelSubsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    lateinit var subList: List<String>
    lateinit var userList: List<UserModel>
    private lateinit var reference: DatabaseReference
    private lateinit var subsAdapter: SubsAdapter
    private lateinit var shimmerLayout : ShimmerFrameLayout
    val userId = ChannelActivity.userId

    //this fragment show the channels subscribed by the channel you are viewing
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_channel_subs, container, false)

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true

        shimmerLayout = view.findViewById(R.id.shimmer_layout)
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()

        subList = ArrayList()
        userList = ArrayList()
        recyclerView = view.findViewById(R.id.channelUploads)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        reference = FirebaseDatabase.getInstance().reference

        reference.child("Subscribing").child(userId)
            .addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {

                for (ds in snapshot.children){

                    val sub = ds.key
                    (subList as ArrayList<String>).add(sub!!)
                }

            }

            override fun onCancelled(error: DatabaseError) {


            }

        })

        reference.child("Users").addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot in snapshot.children){

                    val user = datasnapshot.getValue(UserModel::class.java)
                    for (sub in subList){

                        if (user!!.userId == sub){

                           (userList as ArrayList<UserModel>).add(user)

                        }

                    }
                }
                if (context != null){
                    subsAdapter = SubsAdapter(context!!, userList)
                    recyclerView.adapter = subsAdapter
                    subsAdapter.notifyDataSetChanged()
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }


            }

            override fun onCancelled(error: DatabaseError) {


            }
        })

        return view
    }

}