package com.arewabeatz.youtubeclone

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.arewabeatz.youtubeclone.Models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {
    private lateinit var photoUrl: String
    private val GALLERY_PICK = 10

    private lateinit var storageRef: StorageReference
    private lateinit var imageUri: Uri
    private lateinit var pd: ProgressDialog
    private lateinit var mUserRef: DatabaseReference
    private lateinit var spinner:Spinner
    lateinit var videoCategory: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        spinner = findViewById(R.id.categorySpinner)

        val user = FirebaseAuth.getInstance().currentUser

        mUserRef = FirebaseDatabase.getInstance().reference.child("Users")
            .child(user!!.uid)

        mUserRef.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {

                val sUser = snapshot.getValue(UserModel::class.java)
                channelNameSettings.setText(sUser!!.name)
                channelDescSettings.setText(sUser.about)

                try {
                    Picasso.get().load(sUser.profilePhoto).into(profilePhoto)
                }catch (e: Exception){
                    profilePhoto.setImageResource(R.drawable.ic_play)
                }
            }

            override fun onCancelled(error: DatabaseError) {


            }
        })

        val categories = arrayOf(
            "Uncategorized", "Education", "Sports", "Entertainment", "Technology",
            "Health", "Politics", "Gist"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener,
            AdapterView.OnItemClickListener {


            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                videoCategory = parent!!.getItemAtPosition(position).toString()
                Toast.makeText(
                    this@SettingsActivity,
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

        logOutBtn.setOnClickListener {
            //user log out button
            FirebaseAuth.getInstance().signOut()
            val i = Intent(this, SignInActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(i)
        }

        updatePhotoBtn.setOnClickListener {

            //user wants to change photo
            val pickAlbum = Intent()
            pickAlbum.type = "image/*"
            pickAlbum.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(pickAlbum, GALLERY_PICK)
        }

        updateProfileBtn.setOnClickListener {
            val name = channelNameSettings.text.toString()
            val about = channelDescSettings.text.toString()
            val interest = videoCategory
            if (name.isNotEmpty() || about.isNotEmpty()){
                updateName(name, about, interest)
            }
        }
    }

    private fun updateName(name: String, about: String, interest: String) {

        //this function update the current changes made in the settings
        val map = HashMap<String, Any>()
        map["name"] = name
        map["about"] = about
        map["interest"] = interest
        mUserRef.updateChildren(map).addOnCompleteListener {
            if (it.isSuccessful){
                val i = Intent(this, ChannelActivity::class.java)
                i.putExtra("userId", FirebaseAuth.getInstance().currentUser!!.uid)
                startActivity(i)
                finish()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data!!.data != null) {
            imageUri = data.data!!
            profilePhoto.setImageURI(imageUri)
            uploadToDatabase()
        }

    }

    private fun uploadToDatabase() {

        //uploading new image to firebase
        val pd = ProgressDialog(this)
        pd.setMessage("saving photo pls wait...")
        pd.setCanceledOnTouchOutside(false)
        pd.show()

        storageRef = FirebaseStorage.getInstance().reference.child("ProfilePhotos")
        val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

        val path = storageRef.child("$currentUser.jpg")

        path.putFile(imageUri)
            .addOnSuccessListener {
                path.downloadUrl.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        photoUrl = task.result.toString()
                       mUserRef.child("profilePhoto").setValue(photoUrl)
                        pd.dismiss()
                    } else {
                        Toast.makeText(
                            this,
                            "Failed to update profile photo", Toast.LENGTH_LONG
                        ).show()
                        pd.dismiss()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload to album storage", Toast.LENGTH_LONG)
                    .show()
                pd.dismiss()
            }

    }

}