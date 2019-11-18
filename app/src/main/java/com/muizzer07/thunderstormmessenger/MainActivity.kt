package com.muizzer07.thunderstormmessenger

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerBtn.setOnClickListener {
            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            Log.d("MainActivity", "Email is "+ email);
            Log.d("MainActivity", "Password is "+ password);
        }

        already_have_an_account_text.setOnClickListener {

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
