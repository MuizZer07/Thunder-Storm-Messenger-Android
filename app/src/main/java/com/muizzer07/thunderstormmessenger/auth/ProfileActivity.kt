package com.muizzer07.thunderstormmessenger.auth

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.muizzer07.thunderstormmessenger.R

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar?.title = "Profile"
    }
}
