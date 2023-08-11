package com.abdi.blogapp.ui.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.abdi.blogapp.ui.adapter.CommentAdapter
import com.abdi.blogapp.ui.fragment.HomeFragment
import com.abdi.blogapp.model.Comment
import com.abdi.blogapp.model.User
import com.abdi.blogapp.R
import com.abdi.blogapp.data.api.ApiConfig
import com.abdi.blogapp.utils.Constant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommentActivity : AppCompatActivity() {
    private var postId = 0
    private lateinit var edtAddComment: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var arrayList: ArrayList<Comment>
    private lateinit var adapter: CommentAdapter
    private lateinit var dialog: ProgressDialog
    private lateinit var sharedPref: SharedPreferences
    private lateinit var refreshLayout: SwipeRefreshLayout

    companion object {
        var postPosition = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        init()
    }

    private fun init() {
        edtAddComment = findViewById(R.id.edtAddComment)
        refreshLayout = findViewById(R.id.swipeComment)
        recyclerView = findViewById(R.id.rvComments)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        dialog = ProgressDialog(this)
        dialog.setCancelable(false)
        postId = intent.getIntExtra("postId", 0)
        postPosition = intent.getIntExtra("postPosition", -1)
        sharedPref = applicationContext.getSharedPreferences("user", MODE_PRIVATE)

        arrayList = ArrayList()
        adapter = CommentAdapter(this@CommentActivity, arrayList)
        recyclerView.adapter = adapter

        getComments(postId)

        refreshLayout.setOnRefreshListener {
            getComments(postId)
        }
    }

    private fun getComments(postId: Int) {
        refreshLayout.isRefreshing = true

        val token = sharedPref.getString("token", "")
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Session berakhir. Silahkan login kembali.", Toast.LENGTH_LONG).show()
            redirectToLogin()
            return
        }
        val authorization = "Bearer $token"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiConfig.apiService.getComment(postId, authorization)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val commentsResponse = response.body()
                        if (commentsResponse != null && commentsResponse.success) {
                            val newComments = commentsResponse.comments
                            arrayList.clear()
                            arrayList.addAll(newComments)
                            adapter.notifyDataSetChanged()
                        } else {
                            if (response.code() == 401) {
                                redirectToLogin()
                                Toast.makeText(this@CommentActivity, "Session berakhir. Silakan login kembali.", Toast.LENGTH_LONG).show()
                                return@withContext
                            } else {
                                Toast.makeText(this@CommentActivity, "Operasi gagal, silahkan coba lagi", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this@CommentActivity, "Terjadi kesalahan pada server", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CommentActivity, "Kesalahan koneksi", Toast.LENGTH_SHORT).show()
                }
            } finally {
                refreshLayout.isRefreshing = false
            }
        }
    }

    fun addComment(view: View) {
        dialog.setMessage("Loading")
        dialog.show()

        val authorization = "Bearer ${sharedPref.getString("token", "") ?: run {
            Toast.makeText(this, "Session berakhir. Silahkan login kembali.", Toast.LENGTH_LONG).show()
            redirectToLogin()
            return
        }}"

        val idPost = postId
        val getComment = edtAddComment.text.toString().trim()

        if (getComment.isEmpty()) {
            Toast.makeText(this, "Komentar tidak boleh kosong", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = ApiConfig.apiService.addComment(authorization, idPost, getComment)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val commentResponse = response.body()
                            if (commentResponse != null && commentResponse.success) {
                                val newComment = commentResponse.comment
                                val newUser = newComment.user

                                val mUSer = User().apply {
                                    id = newUser.id
                                    name = newUser.name
                                    lastname = newUser.lastname
                                    photo = newUser.photo
                                }

                                val mComment = Comment().apply {
                                    user = mUSer
                                    id = newComment.id
                                    comment = newComment.comment
                                    date = newComment.date
                                }

                                val postComment = HomeFragment.arrayList[postPosition]
                                postComment.comments += 1
                                HomeFragment.arrayList[postPosition] = postComment
                                HomeFragment.recyclerView.adapter?.notifyDataSetChanged()

                                arrayList.add(mComment)
                                recyclerView.adapter?.notifyDataSetChanged()
                                edtAddComment.setText("")

                                val resultIntent = Intent()
                                resultIntent.putExtra("newComment", mComment)
                                setResult(Activity.RESULT_OK, resultIntent)
                            } else {
                                Toast.makeText(this@CommentActivity, "Operasi gagal, silahkan coba lagi", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@CommentActivity, "Terjadi kesalahan pada server", Toast.LENGTH_SHORT).show()
                        }
                        dialog.dismiss()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@CommentActivity, "Kesalahan koneksi", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
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