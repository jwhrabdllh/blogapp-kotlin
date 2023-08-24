package com.abdi.blogapp.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abdi.blogapp.R
import com.abdi.blogapp.data.api.ApiConfig
import com.abdi.blogapp.model.Post
import com.abdi.blogapp.ui.adapter.ProfileAdapter
import com.abdi.blogapp.utils.Constant
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserProfileActivity : AppCompatActivity() {
    private lateinit var view: View
    private lateinit var toolbar: MaterialToolbar
    private lateinit var ivUserProfile: CircleImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserPostCount: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProfileAdapter
    private lateinit var sharedPref: SharedPreferences
    private var id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        init()
    }

    private fun init() {
        ivUserProfile = findViewById(R.id.ivPhotoUserProfile)
        tvUserName = findViewById(R.id.tvNameUserProfile)
        tvUserPostCount = findViewById(R.id.tvPostCountUser)
        toolbar = findViewById(R.id.tbUserProfile)
        setSupportActionBar(toolbar)
        recyclerView = findViewById(R.id.rvUserProfile)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        sharedPref = applicationContext.getSharedPreferences("user", MODE_PRIVATE)
        id = intent.getIntExtra("userId", 0)

        getUserProfile()
    }

    @SuppressLint("SetTextI18n")
    private fun getUserProfile() {
        val userId = id
        val token = sharedPref.getString("token", "") ?: ""
        val authorization = "Bearer $token"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiConfig.apiService.getUserProfile(userId, authorization)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val userProfileResponse = response.body()
                        if (userProfileResponse != null && userProfileResponse.success) {
                            val newUser = userProfileResponse.user
                            val userPosts = userProfileResponse.posts

                            Picasso.get().load(Constant.BASE_URL + "storage/profiles/" + newUser.photo).into(ivUserProfile)
                            tvUserName.text = newUser.name + " " + newUser.lastname
                            tvUserPostCount.text = userPosts.size.toString()

                            adapter = ProfileAdapter(this@UserProfileActivity, userPosts)
                            recyclerView.adapter = adapter
                        }
                    } else if (response.code() == 422 || response.code() == 401) {
                        val snackbar = Snackbar.make(view, "Session berakhir. Silahkan login kembali.", Snackbar.LENGTH_INDEFINITE)
                        snackbar.setAction("Login") {
                            redirectToLogin()
                        }
                        snackbar.show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UserProfileActivity, "Koneksi terputus", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun redirectToLogin() {
        sharedPref.edit().remove("token").apply()

        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        getUserProfile()
    }

    fun goBack(view: View) {
        onBackPressed()
    }
}