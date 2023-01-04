package com.arewabeatz.youtubeclone.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arewabeatz.youtubeclone.Adapters.VideoAdapter
import com.arewabeatz.youtubeclone.Adapters.VideoListAdapter
import com.arewabeatz.youtubeclone.ChannelActivity
import com.arewabeatz.youtubeclone.Models.PostModel
import com.arewabeatz.youtubeclone.Models.UserModel
import com.arewabeatz.youtubeclone.R
import com.arewabeatz.youtubeclone.SettingsActivity
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_video_player.*
import kotlinx.android.synthetic.main.fragment_channel_home.view.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import java.lang.Exception


class ChannelHomeFragment : Fragment() {
    private lateinit var userId: String
    private lateinit var recyclerView: RecyclerView
    lateinit var videoList: List<PostModel>
    private lateinit var reference: DatabaseReference
    private lateinit var videoAdapter: VideoListAdapter
    private lateinit var shimmerLayout : ShimmerFrameLayout
    private var currentState = ""


    //this fragment show the channel info of the channel you are viewing

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v =  inflater.inflate(R.layout.fragment_channel_home, container, false)

        userId = ChannelActivity.userId

        getPublisher(v.channelName, userId, v.videoPublisherPhoto )
        getSubscribers(userId, v.channelSubscribers)
        setUpSubscribeRules(userId)

        if (FirebaseAuth.getInstance().currentUser != null){

            val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
            if (currentUser == userId){
                v.settingBtn.visibility = View.VISIBLE
                v.subscribeBtn.visibility = View.GONE
            }

        }

        v.subscribeBtn.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser != null){
                setUpSubscribeRules(userId)
            }else{
                Toast.makeText(context, "You must sign in", Toast.LENGTH_SHORT).show()
            }
        }

        v.settingBtn.setOnClickListener {

            val i = Intent(context, SettingsActivity::class.java)
            startActivity(i)
        }

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

    private fun getPublisher(videoPublisher: TextView, publisher: String, publisherPhoto: CircleImageView) {
        val ref = FirebaseDatabase.getInstance().reference.child("Users")
            .child(publisher)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserModel::class.java)

                videoPublisher.text = user!!.name
                try {
                    Picasso.get().load(user.profilePhoto).into(publisherPhoto)
                }catch (e: Exception){
                    publisherPhoto.setImageResource(R.drawable.ic_play)
                }

            }

            override fun onCancelled(error: DatabaseError) {


            }
        })
    }

    private fun getSubscribers(userId: String, channelSubscriber: TextView) {
        //getting the number of subscribers
        val reff =FirebaseDatabase.getInstance().reference
            .child("Subscribers").child(userId)
        reff.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    val subNumber = snapshot.childrenCount.toString()
                    channelSubscriber.text = "$subNumber Subscribers"
                }
            }

            override fun onCancelled(error: DatabaseError) {


            }
        })
    }

    private fun setUpSubscribeRules(currentUser: String) {

        //val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = FirebaseDatabase.getInstance().reference

        if (currentState == "following") {

            ref.child("Subscribing").child(currentUser).child(userId).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        FirebaseDatabase.getInstance().reference
                            .child("Subscribers").child(userId).child(currentUser).removeValue()
                        currentState = "not following"
                        subscribeBtn.text = "Subscribe"
                    }


                }.addOnFailureListener {
                    currentState = "following"
                    subscribeBtn.text = "Unsubscribe"
                }

        }

        if (currentState == "not following") {
            val map = HashMap<String, Any>()
            map["subscribing"] = userId

            ref.child("Subscribing").child(currentUser).child(userId).setValue(map)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val follower = HashMap<String, Any>()
                        follower["subscriber"] = currentUser

                        FirebaseDatabase.getInstance().reference
                            .child("Subscribers").child(userId).child(currentUser)
                            .setValue(follower)
                            .addOnCompleteListener { work ->

                                if (work.isSuccessful) {

                                    /*PushNotification(
                                        NotificationData("New follower", "Someone started following you", "", ""), token).also {
                                        sendNotification(it)
                                    }

                                     */
                                    currentState = "following"
                                    subscribeBtn.text = "Unsubscribe"
                                }

                            }
                    }

                }
                .addOnFailureListener {
                    Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
                    currentState = "not following"
                    subscribeBtn.text = "Subscribe"
                }

        }

    }

}