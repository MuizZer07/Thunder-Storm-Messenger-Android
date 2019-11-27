package com.muizzer07.thunderstormmessenger.messages

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.muizzer07.thunderstormmessenger.R
import com.muizzer07.thunderstormmessenger.models.TextMessage
import com.muizzer07.thunderstormmessenger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.chat_row_to.view.*
import kotlinx.android.synthetic.main.chat_row_from.view.*

class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()
    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        texts_recycleView.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser!!.username

        listenForMessages()

        sendBtn.setOnClickListener {
            performSendMessage()
        }
    }

    private fun listenForMessages(){
        val currentuser_uid = LatestMessagesActivity.currentUser!!.uid
        val touser_uid = toUser!!.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$currentuser_uid/$touser_uid")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val textMessage = p0.getValue(TextMessage::class.java)

                if(textMessage != null){
                    if(textMessage.fromId == FirebaseAuth.getInstance().uid){
                        adapter.add(ChatFromItem(textMessage.text, LatestMessagesActivity.currentUser!!))
                    }else{
                        adapter.add(ChatToItem(textMessage.text, toUser!!))
                    }
                    texts_recycleView.scrollToPosition(adapter.itemCount - 1)
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    private fun performSendMessage(){
        val newText = messageText.text.toString()
        val currentuser_uid = LatestMessagesActivity.currentUser!!.uid
        val touser_uid = toUser!!.uid

        // all messages node
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$currentuser_uid/$touser_uid").push()
        val to_ref = FirebaseDatabase.getInstance().getReference("/user-messages/$touser_uid/$currentuser_uid").push()

        if(currentuser_uid == null) return

        val textMessage = TextMessage(ref.key!!, newText, currentuser_uid, touser_uid, System.currentTimeMillis()/1000)
        ref.setValue(textMessage)
                .addOnSuccessListener {
                    messageText.text.clear()
                    texts_recycleView.scrollToPosition(adapter.itemCount - 1)
                }
        to_ref.setValue(textMessage)

        // update latest messages node
        FirebaseDatabase.getInstance().getReference("/latest-messages/$currentuser_uid/").child(touser_uid).setValue(textMessage)
        FirebaseDatabase.getInstance().getReference("/latest-messages/$touser_uid/").child(currentuser_uid).setValue(textMessage)
    }
}

class ChatFromItem(val text: String, val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.text_from.text = text

        val uri = user.profileImageUrl
        val imageView = viewHolder.itemView.dp_from
        Picasso.get().load(uri).into(imageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_row_from
    }
}

class ChatToItem(val text: String, val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.text_to.text = text

        val uri = user.profileImageUrl
        val imageView = viewHolder.itemView.dp_to
        Picasso.get().load(uri).into(imageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_row_to
    }
}