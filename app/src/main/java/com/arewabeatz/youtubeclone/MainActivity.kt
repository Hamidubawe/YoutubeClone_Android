package com.arewabeatz.youtubeclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.arewabeatz.youtubeclone.Fragments.*
import com.arewabeatz.youtubeclone.Models.UserModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (FirebaseAuth.getInstance().currentUser!= null){
            val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
            FirebaseDatabase.getInstance().reference.child("Users").child(currentUser)
                .addValueEventListener(object : ValueEventListener{

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(UserModel::class.java)
                        try {
                            Picasso.get().load(user!!.profilePhoto).into(userPhoto)
                        }catch (e:Exception){
                            userPhoto.setImageResource(R.drawable.ic_home)
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {


                    }
                })
        }

        //setting up bottom navigation menu
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, HomeFragment())
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()

        bottomNav = findViewById(R.id.bottom_nav)
        //bottomnav item select listener
        bottomNav.setOnNavigationItemSelectedListener { item ->

            when(item.itemId){

                R.id.home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, HomeFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()

                }

                R.id.explore -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, ExploreFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()

                }
                /*R.id.addVideo -> {
                    //do something
                }

                 */

                R.id.library -> {

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, LibraryFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()

                }

                R.id.subscription -> {
                    if (FirebaseAuth.getInstance().currentUser != null){
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.frameLayout, ChannelSubsFragment())
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit()
                    }
                    else{

                        Toast.makeText(this, "You are not signed in", Toast.LENGTH_SHORT).show()
                    }

                }
            R.id.notifications -> {

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, NotyFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }

            }
            true

        }
    }
}