package com.arewabeatz.youtubeclone

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import com.arewabeatz.youtubeclone.Models.PlaylistModel
import com.arewabeatz.youtubeclone.Models.PostModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_upload_video.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UploadVideoActivity : AppCompatActivity() {

    private lateinit var storageRef: StorageReference
    private lateinit var reference: DatabaseReference
    private val GALLERY_PICK = 101
    private lateinit var currentUser: String
    private lateinit var videoId: String
    private lateinit var byte: ByteArray
    lateinit var videoCategory: String
    private lateinit var metadataRetriever: MediaMetadataRetriever
    private lateinit var spinner: Spinner
    private lateinit var videoUri: Uri
    private lateinit var thumbnailUri: Uri
    private lateinit var pd: ProgressDialog
    private lateinit var playList: List<String>
    private var playlist: String = ""
    private var type = "video"
    private lateinit var playlistSpinner: Spinner

    companion object var channelUid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_video)
        getPlaylist()

        storageRef = FirebaseStorage.getInstance().reference.child("Images")
        reference = FirebaseDatabase.getInstance().reference.child("Videos")
        metadataRetriever = MediaMetadataRetriever()
        spinner = findViewById(R.id.videoCategory)
        playlistSpinner = findViewById(R.id.playlistSpinner)


        pd = ProgressDialog(this)
        pd.setCanceledOnTouchOutside(false)

        val categories = arrayOf(
            "Un-categorized", "Education", "Sports", "Entertainment", "Technology",
            "Health", "Politics", "Gist"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener,
            AdapterView.OnItemClickListener {

            //spinner of video category

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                videoCategory = parent!!.getItemAtPosition(position).toString()
                Toast.makeText(
                    this@UploadVideoActivity,
                    "$videoCategory selected",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemClick(
                parent: AdapterView<*>?, view: View?,
                position: Int, id: Long
            ) {

            }

        }

        choseThumbNailBtn.setOnClickListener {

            //adding video thumbnail
            val pickPhoto = Intent()
            type = "thumbnail"
            pickPhoto.type = "image/*"
            pickPhoto.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(pickPhoto, GALLERY_PICK)

        }

        publishVideoBtn.setOnClickListener {
            //publishing video
            val title = videoTitle.text.toString()
            val videoDescription = videoDescription.text.toString()

            if(title.isNotEmpty() && videoDescription.isNotEmpty()){
                uploadToFirebase(title, videoDescription)

            }
            else{
                Toast.makeText(this, "Pls enter title and description", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        //actvity result
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data!!.data != null){
            thumbnailUri = data.data!!
            choseThumbNailBtn.text = "Change Thumbnail"

            thumbnail.setImageURI(thumbnailUri)
        }
    }

    private fun getFileExtension(uri: Uri): String? {

        //getting the video file extension eg. .mp4 or .mkv etc..
        val contentResolver = contentResolver
        val mimetype = MimeTypeMap.getSingleton()
        return mimetype.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private fun uploadToFirebase(title: String, videoDescription: String) {

        //this function uploads the Video to firebase
        pd.show()
        reference = FirebaseDatabase.getInstance().reference.child("Videos")
        videoId = reference.push().key!!
        currentUser = FirebaseAuth.getInstance().currentUser!!.uid
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        val filepath = storageRef.child(title + "." + getFileExtension(thumbnailUri))

        filepath.putFile(thumbnailUri)
            .addOnSuccessListener{
                filepath.downloadUrl.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val thumbnail = task.result.toString()

                        //setting items to model view
                        val postModel = PostModel(currentUser, videoId, title, videoDescription, currentDate,"",
                        thumbnail,  playlist, videoCategory)

                        reference.child(videoId).setValue(postModel)
                            .addOnCompleteListener { ok ->
                                if (ok.isSuccessful) {

                                    Toast.makeText(
                                        this, "Updated successfully..", Toast.LENGTH_LONG).show()
                                    pd.dismiss()

                                    val i = Intent(this, AddVideo::class.java)
                                    i.putExtra("videoId", videoId)
                                    i.putExtra("photoUrl", thumbnail)
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    startActivity(i)

                                }

                            }
                    }
                }
            }
            .addOnProgressListener {
                pd.setMessage("Uploading: ${(it.bytesTransferred / it.totalByteCount) * 100.0}%..")
            }
            .addOnFailureListener {
                Toast.makeText(
                    this, "failed to upload song please check your internet connection",
                    Toast.LENGTH_LONG
                ).show()
                pd.dismiss()
            }

    }

    private fun getPlaylist(){

        //this function gets the previous playlists of the currentUser
        playList = ArrayList()
        (playList as ArrayList<String>).add("Uncategorized")
        val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

        val ref = FirebaseDatabase.getInstance().reference.child("Playlist")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (ds in snapshot.children){
                        val list = ds.getValue(PlaylistModel::class.java)
                        if (list!!.pUid == currentUser){

                            (playList as ArrayList<String>).add(list.pName)

                            val adapter = ArrayAdapter(this@UploadVideoActivity, android.R.layout.simple_spinner_dropdown_item, playList)
                            playlistSpinner.adapter = adapter

                            playlistSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener,
                                AdapterView.OnItemClickListener {


                                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                    playlist = parent!!.getItemAtPosition(position).toString()
                                    Toast.makeText(
                                        this@UploadVideoActivity,
                                        "$playlist selected",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {

                                }

                                override fun onItemClick(
                                    parent: AdapterView<*>?, view: View?,
                                    position: Int, id: Long
                                ) {

                                }

                            }
                        }
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {


            }
        })
    }
}