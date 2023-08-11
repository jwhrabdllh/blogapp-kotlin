package com.abdi.blogapp.ui.activity

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.abdi.blogapp.ui.fragment.HomeFragment
import com.abdi.blogapp.R
import com.abdi.blogapp.data.api.ApiConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

class AddPostActivity : AppCompatActivity() {
    private lateinit var dialog: ProgressDialog
    private lateinit var sharedPref: SharedPreferences
    private lateinit var btnPost: Button
    private lateinit var imgPost: ImageView
    private lateinit var edtDesc: EditText
    private lateinit var edtTitle: EditText
    private var bitmap: Bitmap? = null

    companion object {
        private const val GALLERY_CHANGE_POST = 3
        private const val PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)
        init()
    }

    private fun init() {
        sharedPref = applicationContext.getSharedPreferences("user", MODE_PRIVATE)
        imgPost = findViewById(R.id.ivAddPost)
        edtTitle = findViewById(R.id.edtTitleAddPost)
        edtDesc = findViewById(R.id.edtDescAddPost)
        btnPost = findViewById(R.id.btnUpload)
        dialog = ProgressDialog(this)
        dialog.setCancelable(false)

        btnPost.setOnClickListener {
            val desc = edtDesc.text.toString().trim()
            val title = edtTitle.text.toString().trim()

            if (desc.isEmpty() || title.isEmpty()) {
                Toast.makeText(this, "Judul dan deskripsi harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (bitmap == null) {
                Toast.makeText(this, "Foto harus diunggah!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            post()
        }
    }

    private fun post() {
        dialog.setMessage("Loading..")
        dialog.show()

        val photo = bitmapToString(bitmap)
        val title = edtTitle.text.toString().trim()
        val desc = edtDesc.text.toString().trim()

        val token = sharedPref.getString("token", "") ?: ""
        if (token.isEmpty()) {
            Toast.makeText(this@AddPostActivity, "Session berakhir. Silahkan login kembali.", Toast.LENGTH_SHORT).show()
            redirectToLogin()
            return
        }
        val authorization = "Bearer $token"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiConfig.apiService.addPost(authorization, photo, title, desc)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val postResponse = response.body()
                        if (postResponse != null && postResponse.success) {
                            val newPost = postResponse.post
                            HomeFragment.arrayList.add(0, newPost)
                            HomeFragment.recyclerView.adapter?.notifyItemChanged(0)
                            HomeFragment.recyclerView.adapter?.notifyDataSetChanged()
                            Toast.makeText(this@AddPostActivity, "Sukses", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@AddPostActivity, "Gagal menambahkan post", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@AddPostActivity, "Terjadi kesalahan pada server", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddPostActivity, "Kesalahan koneksi", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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

    private fun checkPermissionAndSelectPhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_READ_EXTERNAL_STORAGE)
        } else {
            openGallery()
        }
    }

    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK)
        gallery.type = "image/*"
        startActivityForResult(gallery, GALLERY_CHANGE_POST)
    }

    fun addPhoto(view: View) {
        checkPermissionAndSelectPhoto()
    }

    fun goBack(view: View) {
        super.onBackPressed()
    }

    private fun redirectToLogin() {
        sharedPref.edit()
            .remove("token")
            .apply()

        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
}