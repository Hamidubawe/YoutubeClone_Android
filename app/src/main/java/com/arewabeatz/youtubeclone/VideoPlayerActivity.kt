package com.arewabeatz.youtubeclone

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arewabeatz.youtubeclone.Adapters.VideoAdapter
import com.arewabeatz.youtubeclone.Models.PostModel
import com.arewabeatz.youtubeclone.Models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_video_player.*
import java.io.File
import java.lang.Exception

class VideoPlayerActivity : AppCompatActivity() {
    private lateinit var videoId: String
    private lateinit var userId: String
    private lateinit var ref: DatabaseReference
    private var videoPlaylist = ""
    private var videoCategory = ""
    private lateinit var videoView: VideoView
    private lateinit var currentState: String
    private lateinit var videoUrl: String
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        //getting intent
        videoId = intent.getStringExtra("videoId")!!

        //getting video information
        ref = FirebaseDatabase.getInstance().reference.child("Videos").child(videoId)
        ref.addValueEventListener(object : ValueEventListener{

            override fun onCancelled(error: DatabaseError) {


            }

            override fun onDataChange(snapshot: DataSnapshot) {

                val video = snapshot.getValue(PostModel::class.java)

                videoPlaylist = video!!.playlist
                videoCategory = video.category
                userId = video.publisher
                videoUrl = video.videoUrl
                getSubscribers(userId)

                try {

                    videoView = findViewById(R.id.videoPlayer)
                    videoView.setVideoURI(Uri.parse(videoUrl))
                    //videoView.setVideoPath(videoUrl)
                    videoView.setMediaController(MediaController(this@VideoPlayerActivity))
                    videoView.requestFocus()
                    videoView.start()


                }
                catch (e:Exception){
                    e.printStackTrace()
                }

                videoTitle.text = video.videoTitle
                videoDate.text = video.videoDate
                getPublisher(channelName, video.publisher, videoPublisherPhoto)
                getPlayedTimes(video.videoId, videoViews)

            }
        })


        //open channelActivity
        channelInfoLayout.setOnClickListener {
            val i = Intent(this, ChannelActivity::class.java)
            i.putExtra("userId", userId)
            startActivity(i)
        }

        shareVideoBtn.setOnClickListener {

            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Watch this amazing video"
                )
                type = "text/plain"
            }

            val sharedIntent = Intent.createChooser(intent, null)
            startActivity(sharedIntent)
        }

        //download video to external storage
        downloadBtn.setOnClickListener {

            Dexter.withContext(this).withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_PHONE_STATE
            ).withListener(object : MultiplePermissionsListener {


                override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                    downloadSongToStorage()
                }

                override fun onPermissionRationaleShouldBeShown(
                    list: List<PermissionRequest>,
                    permissionToken: PermissionToken
                ) {

                    permissionToken.continuePermissionRequest()

                }
            }).check()

        }

        saveBtn.setOnClickListener {
            //save the video to a playlist
        }

        likeBtn.setOnClickListener {

            //setting up like and dislike
            //pls note this works you can uncomment it you want
            /*if (currentUser != null) {

                if (favorited) {
                    FirebaseDatabase.getInstance().reference.child("Favorites")
                        .child(videoId).child(currentUser.uid).removeValue()
                    favoriteImageView.setImageResource(R.drawable.ic_unlike)
                } else {

                    FirebaseDatabase.getInstance().reference.child("Favorites")
                        .child(list[position].songId).child(currentUser.uid).setValue("fav")
                    likeBtn.setImageResource(R.drawable.ic_like)

                }

            } else {
                Toast.makeText(this, "Sign in to star this song", Toast.LENGTH_SHORT)
                    .show()
            }

             */
        }

        //subscribing/unsubscribing to a youtube channel
        subscribeBtn.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser != null){
                setUpSubscribeRules()
            }else{
                Toast.makeText(this, "You must sign in", Toast.LENGTH_SHORT).show()
            }
        }

        if (FirebaseAuth.getInstance().currentUser != null){
            currentUser = FirebaseAuth.getInstance().currentUser!!
            val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
            val ref = FirebaseDatabase.getInstance().reference
            ref.child("Subscribers").child(currentUser)
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChild(userId)) {

                            currentState = "following"
                            subscribeBtn.text = "Unsubscribe"
                            subscribeBtn.setTextColor(resources.getColor(android.R.color.black))
                        } else {
                            currentState = "not following"
                            subscribeBtn.text = "Subscribe"
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {


                    }

                })
        }else{
            subscribeBtn.text = "Sign In"
        }

        //recyclerview for similar videos
        setUpRecyclerView()

    }

    private fun downloadSongToStorage() {

        //this function downloads video to external storage
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl)
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Downloading...")
        progressDialog.setMessage(title)
        progressDialog.isIndeterminate = true
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setCancelable(false)
        progressDialog.show()

        val rootPath = File(Environment.getExternalStorageDirectory(), "My Videos")

        if (!rootPath.exists()) {
            rootPath.mkdirs()
        }

        val localFile = File(rootPath, "${videoTitle.text} | Youtube.mp4")

        storageRef.getFile(localFile)
            .addOnProgressListener {
                val progress = (it.bytesTransferred / it.totalByteCount) * 100.0
                progressDialog.setMessage("Downloading: $progress%..")
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message.toString(), Toast.LENGTH_LONG).show()
            }
            .addOnCompleteListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Download Successfully completed to $rootPath",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun setUpRecyclerView() {

        //this function finds videos related/similar to the current video  being played
        val recyclerView = findViewById<RecyclerView>(R.id.relatedVideosRecycler)

        val linearLayoutManager = LinearLayoutManager(this)
        val videoList: List<PostModel>
        linearLayoutManager.reverseLayout = true
        //linearLayoutManager.stackFromEnd = true
        var videoAdapter : VideoAdapter

        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        val reference = FirebaseDatabase.getInstance().reference.child("Videos")
        videoList = ArrayList()

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (ds in snapshot.children) {

                    val video: PostModel? = ds.getValue(PostModel::class.java)
                    if (video!!.category == videoCategory || video.playlist == videoPlaylist) {
                        (videoList as ArrayList<PostModel>).add(video)
                    }

                }

                    videoAdapter = VideoAdapter(this@VideoPlayerActivity, videoList)
                    recyclerView.adapter = videoAdapter
                    videoAdapter.notifyDataSetChanged()
                    recyclerView.visibility = View.VISIBLE


            }

            override fun onCancelled(error: DatabaseError) {


            }
        })
    }

    private fun getPublisher(videoPublisher: TextView, publisher: String, publisherPhoto: CircleImageView) {

        //this function ggets the publisher information
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

    private fun getPlayedTimes(videoId: String, play: TextView) {

        //this function gets number times video is been played
        val ref = FirebaseDatabase.getInstance().reference.child("Plays")
            .child(videoId)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val playCount = snapshot.childrenCount.toInt()
                    play.text = "${playCount.toString()} views"
                }

            }

            override fun onCancelled(error: DatabaseError) {


            }
        })

    }

    private fun setUpSubscribeRules() {

        //this function checks if currentuser is already subscribed to the channel or not
        val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
        val reference = FirebaseDatabase.getInstance().reference
        if (currentState == "following") {

            reference.child("Subscribing").child(currentUser).child(userId).removeValue()
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

            reference.child("Subscribing").child(currentUser).child(userId).setValue(map)
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
                    Toast.makeText(this, "Failed to follow user", Toast.LENGTH_LONG).show()
                    currentState = "not following"
                    subscribeBtn.text = "Subscribe"
                }

        }

    }

    private fun getSubscribers(userId: String) {

        ///functions get  number of subscribers
        val reff =FirebaseDatabase.getInstance().reference
            .child("Subscribers").child(userId)
        reff.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    val subNumber = snapshot.childrenCount.toString()
                    channelSubscribers.text = "$subNumber Subscribers"
                }
            }

            override fun onCancelled(error: DatabaseError) {


            }
        })
    }

}