package com.abdi.blogapp.UI

import android.app.ProgressDialog
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.abdi.blogapp.Fragment.HomeFragment
import com.abdi.blogapp.Model.Post
import com.abdi.blogapp.R
import com.abdi.blogapp.Utils.ApiConfig
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class EditPostActivity : AppCompatActivity() {
    private var position = 0
    private var id = 0
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
        val request = object : StringRequest(Method.PUT, ApiConfig.UPDATE_POST, Response.Listener { response ->
                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.getBoolean("success")) {
                        val post: Post = HomeFragment.arrayList[position]
                        post.title = edtTitle.text.toString()
                        post.desc = edtDesc.text.toString()

                        HomeFragment.arrayList[position] = post
                        HomeFragment.recyclerView.adapter?.notifyItemChanged(position)
                        HomeFragment.recyclerView.adapter?.notifyDataSetChanged()
                        Toast.makeText(this, "Sukses", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    dialog.dismiss()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener(Throwable::printStackTrace)
        ) {

            override fun getHeaders(): MutableMap<String, String> {
                val token = sharedPref.getString("token", "") ?: ""
                val map = HashMap<String, String>()
                map["Authorization"] = "Bearer $token"
                return map
            }

            override fun getParams(): MutableMap<String, String> {
                val map = HashMap<String, String>()
                map["id"] = id.toString()
                map["title"] = edtTitle.text.toString()
                map["desc"] = edtDesc.text.toString()
                return map
            }
        }

        request.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 30000
            }

            override fun getCurrentRetryCount(): Int {
                return 2
            }

            override fun retry(error: VolleyError) {}
        }

        val queue = Volley.newRequestQueue(this)
        queue.add(request)
    }

    fun cancelEdit(view: View) {
        super.onBackPressed()
    }
}