package com.arewabeatz.youtubeclone

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.arewabeatz.youtubeclone.Models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.text.SimpleDateFormat
import java.util.*

class SignUpActivity : AppCompatActivity() {
    private lateinit var pd: ProgressDialog
    private lateinit var interest: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val spinner = findViewById<Spinner>(R.id.categorySpinner)

        //here a user will choses his interested type of content
        val categories = arrayOf(
            "Uncategorized", "Education", "Sports", "Entertainment", "Technology",
            "Health", "Politics", "Gist"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener,
            AdapterView.OnItemClickListener {


            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                interest = parent!!.getItemAtPosition(position).toString()
                Toast.makeText(
                    this@SignUpActivity,
                    "$interest selected",
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

        pd = ProgressDialog(this)
        pd.setCanceledOnTouchOutside(false)

        signUp.setOnClickListener {
            //signing in the user
            val name = signUpName.text.toString()
            val email = signUpEmail.text.toString()
            val  password = signUpPassword.text.toString()

            if (name.isNotEmpty() || email.isNotEmpty() || password.isNotEmpty()){

                pd.setMessage("Creating account pls wait...")
                registerUser(email, password, name)
            }
        }
    }

    private fun registerUser(email: String, password: String, name: String) {

        //registering the user to the database
        pd.show()
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val token = FirebaseInstanceId.getInstance().token
        val mAuth = FirebaseAuth.getInstance()
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

            if (it.isSuccessful){
                val userId = mAuth.currentUser!!.uid
                val reference = FirebaseDatabase.getInstance().reference
                    .child("Users")
                    .child(userId)
                pd.dismiss()

                val userModel = UserModel(name, userId, "default", "default", email, token!!, "Hello Youtube",
                false, interest, currentDate)

                reference.setValue(userModel).addOnCompleteListener {
                    pd.dismiss()
                    val main = Intent(this,
                        MainActivity::class.java)
                    main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(main)
                }

                    .addOnFailureListener {task ->
                    pd.dismiss()
                    Toast.makeText(this, task.toString(), Toast.LENGTH_SHORT).show()
                }

            }


        }

    }

}