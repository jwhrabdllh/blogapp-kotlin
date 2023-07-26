package com.abdi.blogapp.UI

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.abdi.blogapp.Fragment.HomeFragment
import com.abdi.blogapp.Model.Post
import com.abdi.blogapp.Model.User
import com.abdi.blogapp.R
import com.abdi.blogapp.Utils.ApiConfig
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class AddPostActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var arrayList: ArrayList<Post>
    private lateinit var dialog: ProgressDialog
    private lateinit var preferences: SharedPreferences
    private lateinit var btnPost: Button
    private lateinit var imgPost: ImageView
    private lateinit var edtDesc: EditText
    private lateinit var edtTitle: EditText
    private val GALLERY_CHANGE_POST = 3
    private var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)
        init()
    }

    private fun init() {
        preferences = applicationContext.getSharedPreferences("user", MODE_PRIVATE)
        imgPost = findViewById(R.id.ivAddPost)
        edtTitle = findViewById(R.id.edtTitleAddPost)
        edtDesc = findViewById(R.id.edtDescAddPost)
        btnPost = findViewById(R.id.btnUpload)
        dialog = ProgressDialog(this)
        dialog.setCancelable(false)

        btnPost.setOnClickListener {
            if (!edtDesc.text.toString().isEmpty() && !edtTitle.text.toString().isEmpty()) {
                post()
            } else {
                Toast.makeText(this, "Kolom harus diisi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun post() {
        dialog.setMessage("Loading..")
        dialog.show()
        val request = object : StringRequest(Method.POST, ApiConfig.ADD_POST, Response.Listener { response ->
                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.getBoolean("success")) {
                        val postObject = jsonObject.getJSONObject("post")
                        val userObject = postObject.getJSONObject("user")

                        val user = User().apply {
                            id = userObject.getInt("id")
                            username = "${userObject.getString("name")} ${userObject.getString("lastname")}"
                            photo = userObject.getString("photo")
                        }

                        val post = Post().apply {
                            this.user = user
                            id = postObject.getInt("id")
                            photo = postObject.getString("photo")
                            title = postObject.getString("title")
                            desc = postObject.getString("desc")
                            date = postObject.getString("created_at")
                            comments = 0
                            likes = 0
                            selfLike = false
                        }

                        HomeFragment.arrayList.add(0, post)
                        HomeFragment.recyclerView.adapter?.notifyItemInserted(0)
                        HomeFragment.recyclerView.adapter?.notifyDataSetChanged()
                        Toast.makeText(this, "Sukses", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                dialog.dismiss()
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
                dialog.dismiss()
            }) {

            override fun getHeaders(): MutableMap<String, String> {
                val token = preferences.getString("token", "") ?: ""
                val map = HashMap<String, String>()
                map["Authorization"] = "Bearer $token"
                return map
            }

            override fun getParams(): MutableMap<String, String> {
                val map = HashMap<String, String>()
                map["title"] = edtTitle.text.toString().trim()
                map["desc"] = edtDesc.text.toString().trim()
                map["photo"] = bitmapToString(bitmap)
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

    private fun bitmapToString(bitmap: Bitmap?): String {
        if (bitmap != null) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val array = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(array, Base64.DEFAULT)
        }
        return ""
    }

    @Deprecated("Deprecated in Kotlin")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_CHANGE_POST && resultCode == RESULT_OK) {
            val imgUri: Uri = data?.data ?: return
            imgPost.setImageURI(imgUri)
            try {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imgUri)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun addPhoto(view: View) {
        val i = Intent(Intent.ACTION_PICK)
        i.type = "image/*"
        startActivityForResult(i, GALLERY_CHANGE_POST)
    }

    fun goBack(view: View) {
        super.onBackPressed()
    }
}