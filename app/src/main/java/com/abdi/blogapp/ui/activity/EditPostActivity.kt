package com.abdi.blogapp.ui.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.abdi.blogapp.ui.fragment.HomeFragment
import com.abdi.blogapp.model.Post
import com.abdi.blogapp.R
import com.abdi.blogapp.data.api.ApiConfig
import com.abdi.blogapp.utils.Constant
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class EditPostActivity : AppCompatActivity() {
    private var position = 0
    private var id = 0
    private lateinit var view: View
    private lateinit var edtDesc: EditText
    private lateinit var edtTitle: EditText
    private lateinit var btnSave: Button
    private lateinit var dialog: ProgressDialog
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_post)
        init()
    }

    private fun init() {
        edtTitle = findViewById(R.id.edtTitleEditPost)
        edtDesc = findViewById(R.id.edtDescEditPost)
        btnSave = findViewById(R.id.btnEditPost)
        dialog = ProgressDialog(this)
        dialog.setCancelable(false)
        id = intent.getIntExtra("postId", 0)
        position = intent.getIntExtra("position", 0)
        sharedPref = application.getSharedPreferences("user", MODE_PRIVATE)

        edtTitle.setText(intent.getStringExtra("title"))
        edtDesc.setText(intent.getStringExtra("desc"))

        btnSave.setOnClickListener {
            if (!edtDesc.text.toString().isEmpty() || !edtTitle.text.toString().isEmpty()) {
                updatePost()
            }
        }
    }

    private fun updatePost() {
        dialog.setMessage("Loading")
        dialog.show()

        val postId = id
        val title = edtTitle.text.toString().trim()
        val desc = edtDesc.text.toString().trim()

        val token = sharedPref.getString("token", "") ?: ""
        val authorization = "Bearer $token"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiConfig.apiService.editPost(authorization, postId, title, desc)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val responsePost = response.body()
                        if (responsePost != null && responsePost.success) {
                            val newPost = HomeFragment.arrayList[position]
                            newPost.title = title
                            newPost.desc = desc

                            HomeFragment.arrayList[position] = newPost
                            HomeFragment.recyclerView.adapter?.notifyItemChanged(position)
                            HomeFragment.recyclerView.adapter?.notifyDataSetChanged()
                            Toast.makeText(this@EditPostActivity, "Sukses", Toast.LENGTH_SHORT).show()

                            val intent = Intent()
                            intent.putExtra("editedTitle", title)
                            intent.putExtra("editedDesc", desc)
                            setResult(Activity.RESULT_OK, intent)
                            finish()

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
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditPostActivity, "Kesalahan koneksi", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }
    }

    fun cancelEdit(view: View) {
        super.onBackPressed()
    }

    private fun redirectToLogin() {
        sharedPref.edit().remove("token").apply()

        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
}