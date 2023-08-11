package com.abdi.blogapp.ui.fragment

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.abdi.blogapp.ui.adapter.PostAdapter
import com.abdi.blogapp.model.Post
import com.abdi.blogapp.R
import com.abdi.blogapp.data.api.ApiConfig
import com.abdi.blogapp.ui.activity.HomeActivity
import com.abdi.blogapp.ui.activity.SignInActivity
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {
    private lateinit var edtSearch: EditText
    private lateinit var view: View
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var postAdapter: PostAdapter
    private lateinit var toolbar: MaterialToolbar
    private lateinit var sharedPref: SharedPreferences
    private lateinit var originalList: ArrayList<Post>

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
        edtSearch = view.findViewById(R.id.edtSearch)
        sharedPref = requireContext().applicationContext.getSharedPreferences("user", MODE_PRIVATE)
        recyclerView = view.findViewById(R.id.rvHomeFragment)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        refreshLayout = view.findViewById(R.id.swipeHomeFragment)
        toolbar = view.findViewById(R.id.tbHomeFrag)
        (requireContext() as HomeActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        getPosts()

        refreshLayout.setOnRefreshListener {
            getPosts()
        }

        edtSearch.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = edtSearch.compoundDrawables[2]
                if (drawableEnd != null && event.rawX >= (edtSearch.right - drawableEnd.bounds.width())) {
                    // Klik pada drawable ic_search, lakukan proses filter/pencarian
                    val query = edtSearch.text.toString()
                    filterPosts(query)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun getPosts() {
        arrayList = ArrayList()
        refreshLayout.isRefreshing = true

        val token = sharedPref.getString("token", "") ?: ""
        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Session berakhir. Silahkan login kembali.", Toast.LENGTH_SHORT).show()
            redirectToLogin()
            return
        }
        val authorization = "Bearer $token"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiConfig.apiService.getPosts(authorization)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val responsePosts = response.body()
                        if (responsePosts != null && responsePosts.success) {
                            val newPosts = responsePosts.posts
                            originalList = ArrayList(newPosts)
                            arrayList.addAll(newPosts)
                            postAdapter = PostAdapter(requireContext(), arrayList)
                            recyclerView.adapter = postAdapter
                        } else {
                            if (response.code() == 401) {
                                redirectToLogin()
                                Toast.makeText(requireContext(), "Session berakhir. Silahkan login kembali.", Toast.LENGTH_LONG).show()
                                return@withContext
                            } else {
                                Toast.makeText(requireContext(), "Gagal memuat postingan", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Terjadi kesalahan pada server", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Kesalahan koneksi", Toast.LENGTH_SHORT).show()
                }
            } finally {
                refreshLayout.isRefreshing = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getPosts()
    }

    private fun redirectToLogin() {
        sharedPref.edit()
            .remove("token")
            .apply()

        val intent = Intent(requireContext(), SignInActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
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
        postAdapter.notifyDataSetChanged()
    }
}