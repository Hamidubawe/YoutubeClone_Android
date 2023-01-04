package com.arewabeatz.youtubeclone.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arewabeatz.youtubeclone.ChannelActivity
import com.arewabeatz.youtubeclone.Models.UserModel
import com.arewabeatz.youtubeclone.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_channel_about.view.*

class ChannelAboutFragment : Fragment() {

    val userId = ChannelActivity.userId

    //this fragment show the about of the channel you are viewing
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v =  inflater.inflate(R.layout.fragment_channel_about, container, false)

        val ref = FirebaseDatabase.getInstance().reference.child("Users")
            .child(userId)

        ref.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserModel::class.java)

               v.dateJoined.text = user!!.joined
                v.aboutDesc.text = "Joined ${user.about}"

            }

            override fun onCancelled(error: DatabaseError) {


            }

        })

        return v
    }

}