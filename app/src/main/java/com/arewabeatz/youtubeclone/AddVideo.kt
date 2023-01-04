package com.arewabeatz.youtubeclone

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_video.*

class AddVideo : AppCompatActivity() {

    private lateinit var videoId : String
    private lateinit var photoUrl: String
    private lateinit var videoUri: Uri
    private val GALLERY_PICK = 13
    private lateinit var videoView: VideoView
    private lateinit var pd: ProgressDialog
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_video)

        pd = ProgressDialog(this)
        pd.setCanceledOnTouchOutside(false)

        videoId = intent.getStringExtra("videoId")!!
        photoUrl = intent.getStringExtra("photoUrl")!!

        reference = FirebaseDatabase.getInstance().reference.child("Videos").child(videoId)

        videoView = findViewById(R.id.videoView)

        val pickVideo = Intent()
        pickVideo.type = "video/*"
        pickVideo.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(pickVideo, GALLERY_PICK)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data!!.data != null){

            videoUri = data.data!!
            choseVideoBtn.text = "Change Video"

            try {
                val path = videoUri.toString()
                videoView.setVideoPath(path)
                videoView.requestFocus()
                videoView.start()
                videoView.setMediaController(MediaController(this))
            }
            catch (e: Exception){
                e.printStackTrace()
            }

            publishVideoBtn.setOnClickListener {
                pd.show()
                uploadToFirebase(videoUri)
            }
        }
    }

    private fun getFileExtension(uri: Uri): String? {

        //getting video file extension
        val contentResolver = contentResolver
        val mimetype = MimeTypeMap.getSingleton()
        return mimetype.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private fun uploadToFirebase(videoUri: Uri) {

        //uploading video to firebase
        val storageRef = FirebaseStorage.getInstance().reference.child("Videos")

        val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

        val filepath = storageRef.child(videoId + "." + getFileExtension(this.videoUri))

        filepath.putFile(videoUri)
            .addOnSuccessListener {
                filepath.downloadUrl.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val videoUrl = task.result.toString()

                        val map = HashMap<String, Any>()
                        map["videoUrl"] = videoUrl
                        reference.updateChildren(map)
                            .addOnCompleteListener { ok ->
                                if (ok.isSuccessful) {
                                    pd.dismiss()
                                    val i = Intent(this, ChannelActivity::class.java)
                                    i.putExtra("userId", currentUser)
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
                    this, "failed to upload Video please try again later",
                    Toast.LENGTH_LONG
                ).show()
                pd.dismiss()
            }

    }
}