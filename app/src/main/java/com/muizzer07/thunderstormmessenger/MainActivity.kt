package com.muizzer07.thunderstormmessenger

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerBtn.setOnClickListener {
            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            if(email.isEmpty() || password.isEmpty() ) {
                Toast.makeText(this, "Please enter email address and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            perform_registration(email, password)
        }

        already_have_an_account_text.setOnClickListener {

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun perform_registration(email: String, password:String){
        val auth =  FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if(!it.isSuccessful){
                        Log.d("MainActivity", "Registration Failed!")
                        return@addOnCompleteListener
                    }

                    Toast.makeText(this, "Successfully Signed Up!", Toast.LENGTH_SHORT).show()
                    Log.d("MainActivity", "Successfully created user!")
                }
                .addOnFailureListener{
                    Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                }
    }
}
