package com.muizzer07.thunderstormmessenger.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import android.widget.ViewAnimator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.muizzer07.thunderstormmessenger.R
import com.muizzer07.thunderstormmessenger.messages.LatestMessagesActivity
import com.muizzer07.thunderstormmessenger.models.User
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        registerBtn.setOnClickListener {
            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            if(email.isEmpty() || password.isEmpty() ) {
                Toast.makeText(this, "Please enter email address and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerBtn.visibility = View.INVISIBLE
            already_have_an_account_text.visibility = View.INVISIBLE
            perform_registration(email, password)
        }

        already_have_an_account_text.setOnClickListener {
            already_have_an_account_text.isEnabled = false
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        profilePic.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
            Log.d("RegisterActivity", "Select Photo")
        }
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            profile_image.setImageBitmap(bitmap)
            profilePic.alpha = 0f
            Log.d("RegisterActivity", "Photo Selected")
        }
    }


    private fun startProgressLoader(){
        Thread(Runnable {
            this@RegisterActivity.runOnUiThread(java.lang.Runnable {
                register_progressLoader.visibility = View.VISIBLE
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
    }

    private fun stopProgressLoader(){
        this@RegisterActivity.runOnUiThread(java.lang.Runnable {
            register_progressLoader.visibility = View.GONE
        })
    }

    private fun perform_registration(email: String, password:String){
        startProgressLoader()
        val auth =  FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if(!it.isSuccessful){
                        Log.d("RegisterActivity", "Registration Failed!")
                        registerBtn.visibility = View.VISIBLE
                        already_have_an_account_text.visibility = View.VISIBLE
                        return@addOnCompleteListener
                    }

                    Toast.makeText(this, "Successfully Signed Up!", Toast.LENGTH_SHORT).show()
                    Log.d("RegisterActivity", "Successfully created user!")

                    uploadImageToFirebaseStorage()
                }
                .addOnFailureListener{
                    Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                    stopProgressLoader()
                }
    }

    private fun uploadImageToFirebaseStorage(){
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val storage = FirebaseStorage.getInstance().getReference("/images/$filename")

        storage.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d("RegisterActivity", "Successfully uploaded image!");

                    storage.downloadUrl.addOnSuccessListener {
                        Log.d("RegisterActivity", "File location ${it}");

                        saveUserToDatabase(it.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    registerBtn.visibility = View.VISIBLE
                    already_have_an_account_text.visibility = View.VISIBLE

                    stopProgressLoader()
                }
    }

    private fun saveUserToDatabase(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val db = FirebaseDatabase.getInstance().getReference("users/$uid")

        val user = User(uid, usernameText.text.toString(), profileImageUrl)
        db.setValue(user)
                .addOnSuccessListener {
                    Log.d("RegisterActivity", "Database updated");

                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Log.d("RegisterActivity", it.message);
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()

                    registerBtn.visibility = View.VISIBLE
                    already_have_an_account_text.visibility = View.VISIBLE

                    stopProgressLoader()
                }
    }
}