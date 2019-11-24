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
        val ref = FirebaseDatabase.getInstance().getReference("/messages")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val textMessage = p0.getValue(TextMessage::class.java)

                if(textMessage != null){
                    if(textMessage.fromId == FirebaseAuth.getInstance().uid){
                        adapter.add(ChatFromItem(textMessage.text, LatestMessagesActivity.currentUser!!))
                    }else{
                        if(textMessage.toId == toUser!!.uid){
                            adapter.add(ChatToItem(textMessage.text, toUser!!))
                        }
                    }
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
        val ref = FirebaseDatabase.getInstance().getReference("/messages").push()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid

        if(fromId == null) return

        val textMessage = TextMessage(ref.key!!, newText, fromId, toId, System.currentTimeMillis()/1000)
        ref.setValue(textMessage)
                .addOnSuccessListener {
                    Log.d("SendMessage", "Success")
                }
                .addOnFailureListener{
                    Log.d("SendMessage", it.message)
                }
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