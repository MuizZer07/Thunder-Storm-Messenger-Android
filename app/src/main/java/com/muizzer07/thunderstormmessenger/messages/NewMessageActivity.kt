package com.muizzer07.thunderstormmessenger.messages

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muizzer07.thunderstormmessenger.R
import com.muizzer07.thunderstormmessenger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_layout.view.*

class NewMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        Log.d("NewMessageActivity", "NewMessageActivity started")

        supportActionBar?.title = "New Message"
        fetchUsers()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.d("NewMessageActivity", "Back Button Pressed")
        val intent = Intent(this, LatestMessagesActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers(){
        Log.d("NewMessageActivity", "Fetching users from firebase")
        val db =FirebaseDatabase.getInstance().getReference("/users")
        db.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                p0.children.forEach{
                    Log.d("NewMessageActivity", "User: " + it.toString())

                    val user = it.getValue(User::class.java)
                    if(user != null){
                        adapter.add(UserItem(user))
                        Log.d("NewMessageActivity", "User added to adapter: " + user.username)
                    }
                }
                recycleview_new_message.adapter = adapter

                adapter.setOnItemClickListener { item, view ->
                    view.isEnabled = false
                    val userItem = item as UserItem
                    Log.d("NewMessageActivity", "User clicked: " + userItem.toString())

                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)

                    Log.d("NewMessageActivity", "NewMessageActivity finished")
                    finish()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}

class UserItem(val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.usernameTextView.text = user.username
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.profile_dp)

        Log.d("NewMessageActivity", "Username and profile picture loaded")
    }

    override fun getLayout(): Int {
        return R.layout.user_row_layout
    }
}