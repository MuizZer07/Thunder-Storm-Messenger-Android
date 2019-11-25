package com.muizzer07.thunderstormmessenger.messages

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.muizzer07.thunderstormmessenger.R
import com.muizzer07.thunderstormmessenger.auth.LoginActivity
import com.muizzer07.thunderstormmessenger.models.TextMessage
import com.muizzer07.thunderstormmessenger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.chat_row_from.view.*
import kotlinx.android.synthetic.main.new_message_row.view.*

class LatestMessagesActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
    }

    val adapter = GroupAdapter<ViewHolder>()
    val latestMessageMap = HashMap<String, TextMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
        verifyUserIsLoggedIn()
        getCurrentUser()

        latest_message_recyclerView.adapter = adapter
        listenForLatestMessages()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_new_message -> {
                NewMessage()
            }
            R.id.menu_sign_out -> {
                logoutUser()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun refreshRecycleView(){
        adapter.clear()
        latestMessageMap.values.forEach{
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessages(){
        val fromuser_uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromuser_uid/")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val textMessage = p0.getValue(TextMessage::class.java) ?: return
                adapter.add(LatestMessageRow(textMessage))
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val textMessage = p0.getValue(TextMessage::class.java) ?: return
                adapter.add(LatestMessageRow(textMessage))
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    private fun getCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun verifyUserIsLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun logoutUser(){
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun NewMessage(){
        val intent = Intent(this, NewMessageActivity::class.java)
        startActivity(intent)
    }

    class LatestMessageRow(val textMessage: TextMessage): Item<ViewHolder>(){
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.latest_message_textview.text = textMessage.text

//            val uri = from_user.profileImageUrl
//            val imageView = viewHolder.itemView.message_dp
//            Picasso.get().load(uri).into(imageView)
        }

        override fun getLayout(): Int {
            return R.layout.new_message_row
        }
    }
}