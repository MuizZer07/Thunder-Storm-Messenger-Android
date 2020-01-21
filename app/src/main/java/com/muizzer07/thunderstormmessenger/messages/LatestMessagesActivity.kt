package com.muizzer07.thunderstormmessenger.messages

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.muizzer07.thunderstormmessenger.R
import com.muizzer07.thunderstormmessenger.R.id.latest_message_recyclerView
import com.muizzer07.thunderstormmessenger.R.id.menu_sign_out
import com.muizzer07.thunderstormmessenger.RestAPI.RetrofitClient
import com.muizzer07.thunderstormmessenger.auth.LoginActivity
import com.muizzer07.thunderstormmessenger.auth.ProfileActivity
import com.muizzer07.thunderstormmessenger.helpers.TimeStampManagement
import com.muizzer07.thunderstormmessenger.models.TextMessage
import com.muizzer07.thunderstormmessenger.models.User
import com.muizzer07.thunderstormmessenger.notification.NewFirebaseMessagingService
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.chat_row_from.view.*
import kotlinx.android.synthetic.main.new_message_row.view.*
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class LatestMessagesActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
    }

    val adapter = GroupAdapter<ViewHolder>()
    val latestMessageMap = HashMap<String, TextMessage>()
    var toUsers = HashMap<String, User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        verifyUserIsLoggedIn()

        getCurrentUser()
        sendDeviceIDtoFirebase()

        latest_message_recyclerView.adapter = adapter
        listenForLatestMessages()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_dp -> {
                ProfilePage(item)
            }
            R.id.menu_new_message -> {
                NewMessage(item)
            }
            R.id.menu_sign_out -> {
                logoutUser(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun refreshRecycleView(){
        adapter.clear()
        latestMessageMap.values.sortedByDescending { it.timeStamp }.forEach{
            adapter.add(LatestMessageRow(it, toUsers))
            Log.i("Adapter", it.toString())
        }
    }

    private fun sendDeviceIDtoFirebase(){
        val token = FirebaseInstanceId.getInstance().getToken()
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/").child("Token").setValue(token)

        ref.addOnSuccessListener {
            Log.d("Device Token", "sendRegistrationTokenToServer(token)")
        }
        ref.addOnFailureListener {
            Log.d("Device Token", "Failed:(${it.toString()})")
        }
    }

    private fun listenForLatestMessages(){
        val fromuser_uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromuser_uid/")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val textMessage = p0.getValue(TextMessage::class.java) ?: return
                latestMessageMap.put(textMessage.toId, textMessage)
                refreshRecycleView()

                adapter.setOnItemClickListener { item, view ->
                    view.isEnabled = false
                    val userItem = item as LatestMessageRow
                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    if(userItem.textMessage.channel.equals("Outgoing")){
                        if(toUsers.containsKey(userItem.textMessage.toId)){
                            intent.putExtra(NewMessageActivity.USER_KEY, toUsers[userItem.textMessage.toId])
                            startActivity(intent)
                        }else{
                            view.isEnabled = true
                        }
                    }else if(userItem.textMessage.channel.equals("Incoming")){
                        if(toUsers.containsKey(userItem.textMessage.fromId)){
                            intent.putExtra(NewMessageActivity.USER_KEY, toUsers[userItem.textMessage.fromId])
                            startActivity(intent)
                        }else{
                            view.isEnabled = true
                        }
                    }
                }
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val textMessage = p0.getValue(TextMessage::class.java) ?: return
                if(textMessage.channel.equals("Outgoing")){
                    latestMessageMap.put(textMessage.toId, textMessage)
                }else{
                    latestMessageMap.put(textMessage.fromId, textMessage)
                }

                refreshRecycleView()
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
        val ref = FirebaseDatabase.getInstance().getReference("/users")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val user = p0.getValue(User::class.java)
                if(user?.uid == uid){
                    currentUser = p0.getValue(User::class.java)
                    Log.i("Current User:: ", currentUser.toString())
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

    private fun verifyUserIsLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun logoutUser(menuItem: MenuItem){
        menuItem.setEnabled(false)
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun NewMessage(menuItem: MenuItem){
        menuItem.setEnabled(false)
        val intent = Intent(this, NewMessageActivity::class.java)
        intent.putExtra(NewMessageActivity.USER_KEY, currentUser)
        startActivity(intent)
    }

    private fun ProfilePage(menuItem: MenuItem){
        menuItem.setEnabled(false)
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    class LatestMessageRow(val textMessage: TextMessage, var toUsers: HashMap<String, User>): Item<ViewHolder>(){

        override fun bind(viewHolder: ViewHolder, position: Int) {

            if(textMessage.channel.equals("Outgoing")){
                val db = FirebaseDatabase.getInstance().getReference("/users/${textMessage.toId}/")

                db.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(p0: DataSnapshot) {
                        val user = p0.getValue(User::class.java)
                        if(user != null){
                            viewHolder.itemView.username_textView.text = user.username
                            val uri = user.profileImageUrl
                            val imageView = viewHolder.itemView.message_dp
                            Picasso.get().load(uri).into(imageView)
                            toUsers.put(user.uid, user)
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {

                    }
                })
            }else if(textMessage.channel.equals("Incoming")){
                val db = FirebaseDatabase.getInstance().getReference("/users/${textMessage.fromId}/")

                db.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(p0: DataSnapshot) {
                        val user = p0.getValue(User::class.java)
                        if(user != null){
                            viewHolder.itemView.username_textView.text = user.username
                            val uri = user.profileImageUrl
                            val imageView = viewHolder.itemView.message_dp
                            Picasso.get().load(uri).into(imageView)
                            toUsers.put(user.uid, user)
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {

                    }
                })
            }

            viewHolder.itemView.latest_message_textview.text = textMessage.text
            viewHolder.itemView.time_stamp.text = TimeStampManagement().processTimeStamp(textMessage.timeStamp)
        }

        override fun getLayout(): Int {
            return R.layout.new_message_row
        }
    }
}
