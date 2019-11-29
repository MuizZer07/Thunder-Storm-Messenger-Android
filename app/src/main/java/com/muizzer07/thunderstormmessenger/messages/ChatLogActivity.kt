package com.muizzer07.thunderstormmessenger.messages

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.muizzer07.thunderstormmessenger.R
import com.muizzer07.thunderstormmessenger.helpers.TimeStampManagement
import com.muizzer07.thunderstormmessenger.models.TextMessage
import com.muizzer07.thunderstormmessenger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.chat_row_from.*
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

        adapter.setOnItemClickListener { item, view ->
            if(item.getItem(0).javaClass == ChatFromItem::class.java){
                val visible = view.mesage_info_text.visibility
                if(visible == View.VISIBLE){
                    view.mesage_info_text.visibility = View.INVISIBLE
                }else{
                    view.mesage_info_text.visibility = View.VISIBLE
                }
            }else{
                val visible = view.mesage_info_text_to.visibility
                if(visible == View.VISIBLE){
                    view.mesage_info_text_to.visibility = View.INVISIBLE
                }else{
                    view.mesage_info_text_to.visibility = View.VISIBLE
                }
            }
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
                        adapter.add(ChatFromItem(textMessage, LatestMessagesActivity.currentUser!!))
                    }else{
                        adapter.add(ChatToItem(textMessage, toUser!!))
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

        val outGoingTextMessage = TextMessage(ref.key!!, newText, currentuser_uid, touser_uid, System.currentTimeMillis(), "Outgoing")
        ref.setValue(outGoingTextMessage)
                .addOnSuccessListener {
                    messageText.text.clear()
                    texts_recycleView.scrollToPosition(adapter.itemCount - 1)
                }

        val incomingTextMessage = TextMessage(ref.key!!, newText, currentuser_uid, touser_uid, System.currentTimeMillis()/1000, "Incoming")
        to_ref.setValue(incomingTextMessage)

        // update latest messages node
        FirebaseDatabase.getInstance().getReference("/latest-messages/$currentuser_uid/").child(touser_uid).setValue(outGoingTextMessage)
        FirebaseDatabase.getInstance().getReference("/latest-messages/$touser_uid/").child(currentuser_uid).setValue(incomingTextMessage)
    }
}

class ChatFromItem(val textMessage: TextMessage, val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.text_from.text = textMessage.text

        val uri = user.profileImageUrl
        val imageView = viewHolder.itemView.dp_from
        Picasso.get().load(uri).into(imageView)

        val time_stamp = "- sent " + TimeStampManagement().processTimeStamp(textMessage.timeStamp) + "\n- haven't seen yet"
        viewHolder.itemView.mesage_info_text.text = time_stamp
        viewHolder.itemView.mesage_info_text.visibility = View.INVISIBLE
    }

    override fun getLayout(): Int {
        return R.layout.chat_row_from
    }
}

class ChatToItem(val textMessage: TextMessage, val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.text_to.text = textMessage.text

        val uri = user.profileImageUrl
        val imageView = viewHolder.itemView.dp_to
        Picasso.get().load(uri).into(imageView)

        val time_stamp = "- sent " + TimeStampManagement().processTimeStamp(textMessage.timeStamp) + "\n- haven't seen yet"
        viewHolder.itemView.mesage_info_text_to.text = time_stamp
        viewHolder.itemView.mesage_info_text_to.visibility = View.INVISIBLE
    }

    override fun getLayout(): Int {
        return R.layout.chat_row_to
    }
}