package com.arewabeatz.youtubeclone

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.MimeTypeMap
import com.arewabeatz.youtubeclone.Models.PlaylistModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_playlist.*

class AddPlaylist : AppCompatActivity() {
    private val galleryPICK = 111
    private lateinit var photoUri : Uri
    val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_playlist)


        addPlaylistImg.setOnClickListener {

            val pickPhoto = Intent()
            pickPhoto.type = "image/*"
            pickPhoto.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(pickPhoto, galleryPICK)
        }

        postPlaylistBtn.setOnClickListener {

            //uplaod to firebase
            val playlistName = playlistName.text.toString()
            val playlistDesc = playlistDesc.text.toString()

            if (playlistName.isNotEmpty()){
                uploadToFirebase(playlistName, playlistDesc)
            }
        }

    }

    private fun getFileExtension(uri: Uri): String? {

        val contentResolver = contentResolver
        val mimetype = MimeTypeMap.getSingleton()
        return mimetype.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private fun uploadToFirebase(playlistName: String, playlistDesc: String) {

        //adding playlist to database
        val ref = FirebaseDatabase.getInstance().reference.child("Playlist")

        val storageRef = FirebaseStorage.getInstance().reference.child("Playlist")

        val key = ref.push().key

        val filepath = storageRef.child(key + "." + getFileExtension(photoUri))

        filepath.putFile(photoUri)
            .addOnSuccessListener {
                filepath.downloadUrl.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val thumbnailUrl = task.result.toString()
                        val playlistModel = PlaylistModel(playlistName, playlistDesc, thumbnailUrl, currentUser, key!!)

                        ref.child(key).setValue(playlistModel).addOnCompleteListener {
                            if (it.isSuccessful){
                                val intent = Intent(this, UploadVideoActivity::class.java)
                                intent.putExtra("playlist", playlistName)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                }
            }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == galleryPICK && resultCode == RESULT_OK && data!!.data != null){

            photoUri = data.data!!
            playlistImage.setImageURI(photoUri)
            addPlaylistImg.text = "Change thumbnail"

        }
    }
}