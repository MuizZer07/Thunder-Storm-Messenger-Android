package com.muizzer07.thunderstormmessenger.auth

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.muizzer07.thunderstormmessenger.R
import com.muizzer07.thunderstormmessenger.messages.LatestMessagesActivity.Companion.currentUser
import com.muizzer07.thunderstormmessenger.messages.NewMessageActivity
import com.muizzer07.thunderstormmessenger.models.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.chat_row_to.view.*

class ProfileActivity : AppCompatActivity() {

    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar?.title = "Profile"
        val uri = currentUser!!.profileImageUrl
        Picasso.get().load(uri).into(profileimageView)
        profilenameText.text = currentUser!!.username
    }
}
