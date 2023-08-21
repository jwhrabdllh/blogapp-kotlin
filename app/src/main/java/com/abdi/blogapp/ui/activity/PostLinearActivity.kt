package com.abdi.blogapp.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.abdi.blogapp.R
import com.abdi.blogapp.data.api.ApiConfig
import com.abdi.blogapp.data.response.GetPostsResponse
import com.abdi.blogapp.data.response.PostsPaginationResponse
import com.abdi.blogapp.model.Post
import com.abdi.blogapp.ui.adapter.PostLinearAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostLinearActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var btnCreatePost: FloatingActionButton
    private lateinit var edtSearch: EditText
    private lateinit var view: View
    private lateinit var progressBar: ProgressBar
    private lateinit var arrayList: ArrayList<Post>
    private lateinit var rvPostLinear: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var originalList: ArrayList<Post>
    private lateinit var sharedPref: SharedPreferences
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var adapter: PostLinearAdapter
    private var page = 1
    private var totalPage = 1
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_linear)
        init()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        btnCreatePost = findViewById(R.id.btnCreatePost)
        edtSearch = findViewById(R.id.edtSearchPost)
        rvPostLinear = findViewById(R.id.rvPostLinear)
        layoutManager = LinearLayoutManager(this)
        refreshLayout = findViewById(R.id.swipePostLinear)
        refreshLayout.setOnRefreshListener(this)
        progressBar = findViewById(R.id.progressBar)
        sharedPref = applicationContext.getSharedPreferences("user", MODE_PRIVATE)
        originalList = ArrayList()

        btnCreatePost.setOnClickListener {
            val intent = Intent(this@PostLinearActivity, AddPostActivity::class.java)
            startActivity(intent)
        }

        setupRecyclerView()
        getLinearPosts(false)

        rvPostLinear.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && (visibleItemCount + firstVisibleItem) >= totalItemCount && firstVisibleItem >= 0) {
                    if (page < totalPage) {
                        isLoading = true
                        page++
                        getLinearPosts(false)
                    }
                }
            }
        })

        edtSearch.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = edtSearch.compoundDrawables[2]
                if (drawableEnd != null && event.rawX >= (edtSearch.right - drawableEnd.bounds.width())) {
                    val query = edtSearch.text.toString()
                    filterPosts(query)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun setupRecyclerView() {
        rvPostLinear.setHasFixedSize(true)
        rvPostLinear.layoutManager = layoutManager
        arrayList = ArrayList()
        adapter = PostLinearAdapter(this, arrayList)
        rvPostLinear.adapter = adapter
    }

    private fun getLinearPosts(isOnRefresh: Boolean) {
        isLoading = true
        if (!isOnRefresh) progressBar.visibility = View.VISIBLE

        val params = HashMap<String, String>()
        params["page"] = page.toString()
        val token = sharedPref.getString("token", "") ?: ""
        val authorization = "Bearer $token"

        ApiConfig.apiService.getPagination(authorization, params).enqueue(object : Callback<PostsPaginationResponse>{
            override fun onResponse(call: Call<PostsPaginationResponse>, response: Response<PostsPaginationResponse>) {
                if (response.code() == 422 && response.code() == 401) {
                    val snackbar = Snackbar.make(view, "Session berakhir. Silahkan login kembali.", Snackbar.LENGTH_INDEFINITE)
                    snackbar.setAction("Login") {
                        redirectToLogin()
                    }
                    snackbar.show()
                }

                totalPage = response.body()?.totalPages!!
                val postsResponse = response.body()?.data
                if (postsResponse != null) {
                    if (isOnRefresh) {
                        originalList.clear()
                    }
                    originalList.addAll(postsResponse)
                    updateFilteredList()
                }

                if (page == totalPage) {
                    progressBar.visibility = View.GONE
                } else {
                    progressBar.visibility = View.INVISIBLE
                }

                isLoading = false
                refreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<PostsPaginationResponse>, t: Throwable) {
                Toast.makeText(this@PostLinearActivity, "Kesalahan koneksi", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.INVISIBLE
                isLoading = false
            }
        })
    }

    private fun updateFilteredList() {
        val query = edtSearch.text.toString()
        filterPosts(query)
    }

    private fun filterPosts(query: String) {
        val filteredList = ArrayList<Post>()
        if (query.isEmpty()) {
            filteredList.addAll(originalList)
        } else {
            for (post in originalList) {
                if (post.title.toLowerCase().contains(query.toLowerCase()) ||
                    post.user.name.toLowerCase().contains(query.toLowerCase())
                ) {
                    filteredList.add(post)
                }
            }
        }
        arrayList.clear()
        arrayList.addAll(filteredList)
        adapter.notifyDataSetChanged()
    }

    private fun redirectToLogin() {
        sharedPref.edit().remove("token").apply()

        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        edtSearch.text.clear()
        updateFilteredList()
    }

    override fun onRefresh() {
        edtSearch.text.clear()
        page = 1
        getLinearPosts(true)
    }
}