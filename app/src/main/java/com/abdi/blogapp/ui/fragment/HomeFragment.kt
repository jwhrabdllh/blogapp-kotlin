package com.abdi.blogapp.ui.fragment

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.abdi.blogapp.ui.adapter.PostGridAdapter
import com.abdi.blogapp.model.Post
import com.abdi.blogapp.R
import com.abdi.blogapp.data.api.ApiConfig
import com.abdi.blogapp.data.response.PostsPaginationResponse
import com.abdi.blogapp.ui.activity.HomeActivity
import com.abdi.blogapp.ui.activity.PostLinearActivity
import com.abdi.blogapp.ui.activity.SignInActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var edtSearch: EditText
    private lateinit var view: View
    private lateinit var progressBar: ProgressBar
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var postGridAdapter: PostGridAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var toolbar: MaterialToolbar
    private lateinit var sharedPref: SharedPreferences
    private lateinit var originalList: ArrayList<Post>
    private var page = 1
    private var totalPage = 1
    private var isLoading = false

    companion object {
        lateinit var recyclerView: RecyclerView
        lateinit var arrayList: ArrayList<Post>
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstancAeState: Bundle?): View {
        view = inflater.inflate(R.layout.layout_home, container, false)
        init()
        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        sharedPref = requireContext().applicationContext.getSharedPreferences("user", MODE_PRIVATE)
        edtSearch = view.findViewById(R.id.edtSearch)
        recyclerView = view.findViewById(R.id.rvHomeFragment)
        recyclerView.setHasFixedSize(true)
        layoutManager = GridLayoutManager(requireContext(), 2)
        refreshLayout = view.findViewById(R.id.swipeHomeFragment)
        refreshLayout.setOnRefreshListener(this)
        progressBar = ProgressBar(requireContext())
        toolbar = view.findViewById(R.id.tbHomeFrag)
        (requireContext() as HomeActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)
        originalList = ArrayList()

        setupRecyclerView()
        getPosts(false)

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

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                    if (page < totalPage) {
                        isLoading = true
                        page++
                        getPosts(false)
                    }
                }
            }
        })
    }

    private fun setupRecyclerView() {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        arrayList = ArrayList()
        postGridAdapter = PostGridAdapter(requireContext(), arrayList)
        recyclerView.adapter = postGridAdapter
    }

    private fun getPosts(isOnRefresh: Boolean) {
        isLoading = true
        if (!isOnRefresh) progressBar.visibility = View.VISIBLE

        val token = sharedPref.getString("token", "") ?: ""
        val authorization = "Bearer $token"

        val params = HashMap<String, String>()
        params["page"] = page.toString()

        ApiConfig.apiService.getPosts(authorization, params).enqueue(object : Callback<PostsPaginationResponse> {
            override fun onResponse(call: Call<PostsPaginationResponse>, response: Response<PostsPaginationResponse>) {
                if (response.code() == 422 || response.code() == 401) {
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
                        arrayList.clear()
                    }
                    originalList.addAll(postsResponse)
                    arrayList.addAll(postsResponse)
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
                Toast.makeText(requireContext(), "Kesalahan koneksi", Toast.LENGTH_SHORT).show()
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
                val username = post.user.name + " " + post.user.lastname
                if (username.toLowerCase().contains(query.toLowerCase())
                ) {
                    filteredList.add(post)
                }
            }
        }
        arrayList.clear()
        arrayList.addAll(filteredList)
        postGridAdapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.post_grid_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.list_post -> {
                val intent = Intent(requireContext(), PostLinearActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        edtSearch.text.clear()
        updateFilteredList()
    }

    override fun onRefresh() {
        edtSearch.text.clear()
        page = 1
        getPosts(true)
    }

    private fun redirectToLogin() {
        sharedPref.edit().remove("token").apply()

        val intent = Intent(requireContext(), SignInActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}