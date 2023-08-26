package com.abdi.blogapp.ui.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abdi.blogapp.R
import com.abdi.blogapp.data.api.ApiConfig
import com.abdi.blogapp.model.Post
import com.abdi.blogapp.model.User
import com.abdi.blogapp.ui.activity.EditProfileActivity
import com.abdi.blogapp.ui.activity.HomeActivity
import com.abdi.blogapp.ui.activity.SignInActivity
import com.abdi.blogapp.ui.adapter.ProfileAdapter
import com.abdi.blogapp.utils.Constant
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {
    private lateinit var view: View
    private lateinit var toolbar: MaterialToolbar
    private lateinit var ivProfile: CircleImageView
    private lateinit var tvName: TextView
    private lateinit var tvPostCount: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPref: SharedPreferences
    private lateinit var adapter: ProfileAdapter
    private lateinit var arrayList: ArrayList<Post>
    private lateinit var dialog: ProgressDialog
    private var imgUrl: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.layout_profile, container, false)
        init()
        return view
    }

    private fun init() {
        ivProfile = view.findViewById(R.id.ivPhotoProfile)
        tvName = view.findViewById(R.id.tvNameProfile)
        tvPostCount = view.findViewById(R.id.tvPostCount)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        toolbar = view.findViewById(R.id.tbProfile)
        (requireContext() as HomeActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        dialog = ProgressDialog(requireContext())
        dialog.setCancelable(false)
        sharedPref = requireContext().getSharedPreferences("user", MODE_PRIVATE)
        recyclerView = view.findViewById(R.id.rvProfile)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        btnEditProfile.setOnClickListener {
            val i = Intent(requireContext(), EditProfileActivity::class.java)
            i.putExtra("imgUrl", imgUrl)
            startActivity(i)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getProfileUser() {
        val token = sharedPref.getString("token", "") ?: ""
        val authorization = "Bearer $token"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiConfig.apiService.myPost(authorization)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val myPostResponse = response.body()
                        if (myPostResponse != null && myPostResponse.success) {
                            arrayList = ArrayList()
                            val newPosts = myPostResponse.posts
                            val newUser = myPostResponse.user

                            val mUser = User().apply {
                                id = newUser.id
                                name = newUser.name
                                lastname = newUser.lastname
                                photo = newUser.photo
                            }

                            for (posts in newPosts) {
                                val mPost = Post().apply {
                                    id = posts.id
                                    photo = posts.photo
                                    title = posts.title
                                    desc = posts.desc
                                    date = posts.date
                                    user = mUser
                                    likes = posts.likes
                                    selfLike = posts.selfLike
                                    comments = posts.comments
                                }
                                arrayList.add(mPost)
                            }

                            tvName.text = mUser.name + " " + mUser.lastname
                            tvPostCount.text = arrayList.size.toString()
                            Glide.with(requireContext())
                                .load(Constant.BASE_URL + "storage/profiles/" + mUser.photo)
                                .into(ivProfile)

                            adapter = ProfileAdapter(requireContext(), arrayList)
                            recyclerView.adapter = adapter
                            imgUrl = Constant.BASE_URL + "storage/profiles/" + mUser.photo
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
                    Toast.makeText(requireContext(), "Koneksi terputus", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.logout_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.item_logout) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Apakah anda ingin logout?")
            builder.setPositiveButton("Logout") { _, _ -> logout() }
            builder.setNegativeButton("Batal", null)
            builder.show()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logout() {
        dialog.setMessage("Logging out..")
        dialog.show()

        val token = sharedPref.getString("token", "") ?: ""
        val authorization = "Bearer $token"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiConfig.apiService.logout(authorization)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val logoutResponse = response.body()
                        if (logoutResponse != null && logoutResponse.success) {
                            val editor = sharedPref.edit()
                            editor.clear()
                            editor.apply()

                            startActivity(Intent(requireContext(), SignInActivity::class.java))
                            requireActivity().finish()
                        } else {
                            Toast.makeText(requireContext(), "Gagal logout", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                    } else if (response.code() == 422 || response.code() == 401) {
                        val snackbar = Snackbar.make(view, "Session berakhir. Silahkan login kembali.", Snackbar.LENGTH_INDEFINITE)
                        snackbar.setAction("Login") {
                            redirectToLogin()
                        }
                        snackbar.show()
                    }
                    dialog.dismiss()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Koneksi gagal", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            getProfileUser()
        }
        super.onHiddenChanged(hidden)
    }

    override fun onResume() {
        super.onResume()
        getProfileUser()
    }

    private fun redirectToLogin() {
        sharedPref.edit().remove("token").apply()

        val intent = Intent(requireContext(), SignInActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}