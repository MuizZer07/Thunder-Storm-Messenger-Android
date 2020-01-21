package com.muizzer07.thunderstormmessenger.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.muizzer07.thunderstormmessenger.R
import com.muizzer07.thunderstormmessenger.messages.LatestMessagesActivity
import kotlinx.android.synthetic.main.activity_login.*
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.opengl.ETC1.getHeight
import android.support.v4.view.ViewCompat.animate
import android.R.attr.translationY
import android.opengl.Visibility
import android.view.animation.AlphaAnimation
import android.view.animation.AccelerateDecelerateInterpolator
import android.support.v4.view.ViewCompat.animate
import android.R.attr.scaleY

class LoginActivity: AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = 1000
        animation.startOffset = 500
        animation.fillAfter = false
        emailText.startAnimation(animation)
        passwordText.startAnimation(animation)
        loginBtn.startAnimation(animation)
        registerText.startAnimation(animation)

        loginBtn.setOnClickListener{
            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            if(email.isEmpty() || password.isEmpty() ) {
                Toast.makeText(this, "Please enter email address and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginBtn.visibility = View.INVISIBLE
            registerText.visibility = View.INVISIBLE
            perform_login(email, password)
        }

        registerText.setOnClickListener {
            registerPage()
        };
    }

    private fun registerPage(){
        registerText.isEnabled = false
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun perform_login(email: String, password:String){
        Thread(Runnable {
            this@LoginActivity.runOnUiThread(java.lang.Runnable {
                login_progressLoader.visibility = View.VISIBLE
            })

            try {
                var i=0;
                while(i<Int.MAX_VALUE){
                    i++
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }).start()

        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if(!it.isSuccessful){
                        Log.d("LoginActivity", "Login Failed!")
                        loginBtn.visibility = View.VISIBLE
                        registerText.visibility = View.VISIBLE
                        return@addOnCompleteListener
                    }

                    Toast.makeText(this, "Successfully Signed In!", Toast.LENGTH_SHORT).show()
                    Log.d("LoginActivity", "Successfully Signed In!")

                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener {

                    this@LoginActivity.runOnUiThread(java.lang.Runnable {
                        login_progressLoader.visibility = View.GONE
                    })
                    loginBtn.visibility = View.VISIBLE
                    registerText.visibility = View.VISIBLE
                    Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                }
    }
}