package com.arewabeatz.youtubeclone

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {
    private lateinit var pd: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        pd =ProgressDialog(this)
        pd.setMessage("Logging in pls wait")
        pd.setCanceledOnTouchOutside(false)

        logInBtn.setOnClickListener {

            //logging in
            val email = loginEMail.text.toString()
            val password = logInPassword.text.toString()

            if (email.isNotEmpty() || password.isNotEmpty()){
                pd.show()
                signInUser(email, password)
            }
        }

        signUpBtn.setOnClickListener {
            //new user clicking on the register button
            val i = Intent(this, SignUpActivity::class.java)
            startActivity(i)
        }
    }

    private fun signInUser(email: String, password: String) {

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {
            val mUser = FirebaseAuth.getInstance().currentUser
            updateUi(mUser)
            pd.dismiss()

        }
            .addOnFailureListener {
                pd.dismiss()
                Toast.makeText(this, "Failed to log in", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUi(mUser: FirebaseUser?) {

        val main = Intent(this,
            MainActivity::class.java)
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(main)

    }
}