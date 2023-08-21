package com.abdi.blogapp.ui.activity

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.abdi.blogapp.R
import com.abdi.blogapp.data.api.ApiConfig
import com.abdi.blogapp.model.Like
import com.abdi.blogapp.ui.adapter.LikeAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LikeActivity : AppCompatActivity() {
    private var postId = 0
    private lateinit var view: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var arrayList: ArrayList<Like>
    private lateinit var adapter: LikeAdapter
    private lateinit var dialog: ProgressDialog
    private lateinit var sharedPref: SharedPreferences
    private lateinit var refreshLayout: SwipeRefreshLayout

    companion object {
        var postPosition = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like)
        init()
    }

    private fun init() {
        refreshLayout = findViewById(R.id.swipeLike)
        recyclerView = findViewById(R.id.rvLikes)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        dialog = ProgressDialog(this)
        dialog.setCancelable(false)
        postId = intent.getIntExtra("postId", 0)
        postPosition = intent.getIntExtra("postPosition", -1)
        sharedPref = applicationContext.getSharedPreferences("user", MODE_PRIVATE)

        arrayList = ArrayList()
        adapter = LikeAdapter(this, arrayList)
        recyclerView.adapter = adapter

        getLikes(postId)

        refreshLayout.setOnClickListener {
            getLikes(postId)
        }
    }

    private fun getLikes(postId: Int) {
        refreshLayout.isRefreshing = true

        val token = sharedPref.getString("token", "") ?: ""
        val authorization = "Bearer $token"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiConfig.apiService.getLikes(postId, authorization)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val likeResponse = response.body()
                        if (likeResponse != null && likeResponse.success) {
                            val getLike = likeResponse.likes
                            arrayList.clear()
                            arrayList.addAll(getLike)
                            adapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(this@LikeActivity, "Operasi gagal, silahkan coba lagi", Toast.LENGTH_SHORT).show()
                        }
                    } else if (response.code() == 401) {
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
                    Toast.makeText(this@LikeActivity, "Kesalahan koneksi", Toast.LENGTH_SHORT).show()
                }
            } finally {
                refreshLayout.isRefreshing = false
            }
        }
    }

    private fun redirectToLogin() {
        sharedPref.edit()
            .remove("token")
            .apply()

        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun goBack(view: View) {
        super.onBackPressed()
    }
}