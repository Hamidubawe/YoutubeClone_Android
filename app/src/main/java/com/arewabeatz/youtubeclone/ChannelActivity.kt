package com.arewabeatz.youtubeclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.arewabeatz.youtubeclone.Fragments.*
import com.arewabeatz.youtubeclone.Models.UserModel
import com.github.clans.fab.FloatingActionMenu
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_channel.*
import java.util.ArrayList

class ChannelActivity : AppCompatActivity() {
    private var viewPager: ViewPager? = null
    private var tabLayout: TabLayout? = null
    private lateinit var floatingActionMenu: FloatingActionMenu

    companion object{
        var userId: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel)
        setSupportActionBar(channelToolbar)

        //initializing views
        floatingActionMenu = findViewById(R.id.floatBtn)
        tabLayout = findViewById(R.id.channelTabLayout)
        viewPager = findViewById(R.id.viewPager)

        userId = if (intent == null){
            FirebaseAuth.getInstance().currentUser!!.uid

        } else{
            intent.getStringExtra("userId")!!
        }

        if (FirebaseAuth.getInstance().currentUser != null){
            val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
            if (currentUser == userId){
                floatingActionMenu.visibility = View.VISIBLE
            }
        }

        //getting user videos
        FirebaseDatabase.getInstance().reference.child("Users")
            .child(userId).addValueEventListener(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {

                    val user = snapshot.getValue(UserModel::class.java)
                    supportActionBar!!.title = user!!.name
                }

                override fun onCancelled(error: DatabaseError) {


                }
            })

        //adding video to floATING button
        addVideoBtn!!.setOnClickListener {
            floatingActionMenu.close(true)
            val i = Intent(this, UploadVideoActivity::class.java)
            startActivity(i)
        }

        addPlaylist.setOnClickListener {
            floatingActionMenu.close(true)
            val i = Intent(this, AddPlaylist::class.java)
            startActivity(i)
        }


        //initializing viewpager
        val viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager
        )
        viewPagerAdapter.addFragments(ChannelHomeFragment(), "HOME")
        viewPagerAdapter.addFragments(ChannelVideosFragment(), "VIDEOS")
        viewPagerAdapter.addFragments(ChannelPlaylistFragment(), "PLAYLISTS")
        //viewPagerAdapter.addFragments(NotyFragment(), "COMMUNITY")
        viewPagerAdapter.addFragments(ChannelSubsFragment(), "CHANNELS")
        viewPagerAdapter.addFragments(ChannelAboutFragment(), "ABOUT")
        viewPager!!.adapter = viewPagerAdapter
        tabLayout!!.setupWithViewPager(viewPager)
    }

    //viewpager class
    class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        private val fragments: ArrayList<Fragment> = ArrayList()
        private val title: ArrayList<String> = ArrayList()
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        fun addFragments(fragment: Fragment, titles: String) {
            fragments.add(fragment)
            title.add(titles)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return title[position]
        }

    }

}