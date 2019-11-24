package com.muizzer07.thunderstormmessenger.auth

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.muizzer07.thunderstormmessenger.R
import com.muizzer07.thunderstormmessenger.messages.LatestMessagesActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginBtn.setOnClickListener{
            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            Log.d("LoginActivity", "EMAIL IS::: " + email)
            if(email.isEmpty() || password.isEmpty() ) {
                Toast.makeText(this, "Please enter email address and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            perform_login(email, password)
        }

        registerText.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        };
    }

    fun perform_login(email: String, password:String){
        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if(!it.isSuccessful){
                        Log.d("LoginActivity", "Login Failed!")
                        return@addOnCompleteListener
                    }

                    Toast.makeText(this, "Successfully Signed In!", Toast.LENGTH_SHORT).show()
                    Log.d("LoginActivity", "Successfully Signed In!")

                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                }
    }
}