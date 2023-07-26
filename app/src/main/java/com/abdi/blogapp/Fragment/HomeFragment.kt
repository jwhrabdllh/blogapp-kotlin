package com.abdi.blogapp.Fragment

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.abdi.blogapp.Adapter.PostAdapter
import com.abdi.blogapp.Model.Post
import com.abdi.blogapp.Model.User
import com.abdi.blogapp.R
import com.abdi.blogapp.UI.HomeActivity
import com.abdi.blogapp.Utils.ApiConfig
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.appbar.MaterialToolbar
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class HomeFragment : Fragment() {
    private lateinit var view: View
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var postAdapter: PostAdapter
    private lateinit var toolbar: MaterialToolbar
    private lateinit var sharedPref: SharedPreferences

    companion object {
        lateinit var recyclerView: RecyclerView
        lateinit var arrayList: ArrayList<Post>
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstancAeState: Bundle?): View {
        view = inflater.inflate(R.layout.layout_home, container, false)
        init()
        return view
    }

    private fun init() {
        sharedPref = requireContext().applicationContext.getSharedPreferences("user", MODE_PRIVATE)
        recyclerView = view.findViewById(R.id.rvHomeFragment)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        refreshLayout = view.findViewById(R.id.swipeHomeFragment)
        toolbar = view.findViewById(R.id.toolbarHomeFrag)
        (requireContext() as HomeActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        getPosts()

        refreshLayout.setOnRefreshListener {
            getPosts()
        }
    }

    private fun getPosts() {
        arrayList = ArrayList()
        refreshLayout.isRefreshing = true

        val request = object : StringRequest(Method.GET, ApiConfig.GET_POST, Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)
                if (jsonObject.getBoolean("success")) {
                    val jsonArray = JSONArray(jsonObject.getString("posts"))
                    for (i in 0 until jsonArray.length()) {
                        val postObject = jsonArray.getJSONObject(i)
                        val userObject = postObject.getJSONObject("user")

                        val user = User().apply {
                            id = userObject.getInt("id")
                            username = "${userObject.getString("name")} ${userObject.getString("lastname")}"
                            photo = userObject.getString("photo")
                        }

                        val post = Post().apply {
                            id = postObject.getInt("id")
                            this.user = user
                            likes = postObject.getInt("likesCount")
                            comments = postObject.getInt("commentsCount")
                            date = postObject.getString("created_at")
                            title = postObject.getString("title")
                            desc = postObject.getString("desc")
                            photo = postObject.getString("photo")
                            selfLike = postObject.getBoolean("selfLike")
                        }

                        arrayList.add(post)
                    }
                    postAdapter = PostAdapter(requireContext(), arrayList)
                    recyclerView.adapter = postAdapter
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            refreshLayout.isRefreshing = false
        }, Response.ErrorListener { error ->
            error.printStackTrace()
            refreshLayout.isRefreshing = false
        }) {

            override fun getHeaders(): MutableMap<String, String> {
                val token =sharedPref.getString("token", "") ?: ""
                val map = HashMap<String, String>()
                map["Authorization"] = "Bearer $token"
                return map
            }
        }

        request.retryPolicy = DefaultRetryPolicy(50000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        val queue = Volley.newRequestQueue(requireContext())
        queue.add(request)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val item = menu.findItem(R.id.search_posts)
        val searchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                postAdapter.getFilter().filter(newText)
                return false
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }
}