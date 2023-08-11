package com.abdi.blogapp.ui.activity

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.DiscretePathEffect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.abdi.blogapp.R
import com.abdi.blogapp.data.api.ApiConfig
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import java.io.ByteArrayOutputStream
import java.io.IOException

class EditProfileActivity : AppCompatActivity() {
    private lateinit var layoutName: TextInputLayout
    private lateinit var layoutLastname: TextInputLayout
    private lateinit var layoutEmail: TextInputLayout
    private lateinit var edtName: TextInputEditText
    private lateinit var edtLastname: TextInputEditText
    private lateinit var edtEmail: TextInputEditText
    private lateinit var circleImageView: CircleImageView
    private lateinit var tvSelectPhoto: TextView
    private lateinit var btnSave: Button
    private lateinit var sharedPref: SharedPreferences
    private lateinit var dialog: ProgressDialog
    private var bitmap: Bitmap? = null

    companion object {
        private const val GALLERY_CHANGE_PROFILE = 5
        private const val PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        init()
    }

    private fun init() {
        circleImageView = findViewById(R.id.ivPhoto)
        tvSelectPhoto = findViewById(R.id.tvSelectPhoto)
        layoutName = findViewById(R.id.txtEditLayoutNameProfile)
        layoutLastname = findViewById(R.id.txtEditLayoutLastnameProfile)
        layoutEmail = findViewById(R.id.txtEditLayoutEmailProfile)
        edtName = findViewById(R.id.txtEditNameProfile)
        edtLastname = findViewById(R.id.txtEditLastnameProfile)
        edtEmail = findViewById(R.id.txtEditEmailProfile)
        btnSave = findViewById(R.id.btnSimpanEditProfile)

        dialog = ProgressDialog(this)
        dialog.setCancelable(false)
        sharedPref = applicationContext.getSharedPreferences("user", MODE_PRIVATE)

        Picasso.get().load(intent.getStringExtra("imgUrl")).into(circleImageView)
        edtName.setText(sharedPref.getString("name", ""))
        edtLastname.setText(sharedPref.getString("lastname", ""))
        edtEmail.setText(sharedPref.getString("email", ""))

        tvSelectPhoto.setOnClickListener {
            checkPermissionAndSelectPhoto()
        }

        btnSave.setOnClickListener {
            if (validate()) {
                updateProfile()
            }
        }

        edtEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!edtEmail.text.toString().isEmpty()) {
                    layoutEmail.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {
                val email = edtEmail.text.toString()

                if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    layoutEmail.error = null
                } else {
                    layoutEmail.error = "Email tidak valid"
                }
            }
        })
    }
    private fun validate(): Boolean {
        if (edtName.text.toString().isEmpty()) {
            layoutName.error = "Nama depan harus diisi"
            return false
        }
        if (edtLastname.text.toString().isEmpty()) {
            layoutLastname.error = "Nama belakang harus diisi"
            return false
        }
        if (edtEmail.text.toString().isEmpty()) {
            layoutEmail.error = "Email harus diisi"
            return false
        }
        return true
    }

    private fun updateProfile() {
        dialog.setMessage("Loading..")
        dialog.show()

        val name = edtName.text.toString().trim()
        val lastname = edtLastname.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val photo = bitmapToString(bitmap)

        val token = sharedPref.getString("token", "") ?: ""
        if (token.isEmpty()) {
            Toast.makeText(this@EditProfileActivity, "Session berakhir. Silahkan login kembali.", Toast.LENGTH_SHORT).show()
            redirectToLogin()
            return
        }
        val authorization = "Bearer $token"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiConfig.apiService.editProfile(authorization, name, lastname, email, photo)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val editProfileResp = response.body()
                        if (editProfileResp != null && editProfileResp.success) {
                            val updateProfile = editProfileResp.user
                            val editor = sharedPref.edit()
                            editor.putString("name", updateProfile.name)
                            editor.putString("lastname", updateProfile.lastname)
                            editor.putString("email", updateProfile.email)
                            editor.apply()

                            Toast.makeText(this@EditProfileActivity, "Sukses", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            dialog.dismiss()
                            Toast.makeText(this@EditProfileActivity, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        dialog.dismiss()
                        Toast.makeText(this@EditProfileActivity, "Terjadi kesalahan pada server", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    e.printStackTrace()
                    Toast.makeText(this@EditProfileActivity, "Kesalahan koneksi", Toast.LENGTH_SHORT).show()
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
        if (requestCode == GALLERY_CHANGE_PROFILE && resultCode == RESULT_OK) {
            val uri: Uri = data?.data ?: return

            circleImageView.setImageURI(uri)

            try {
                val contentResolver = contentResolver
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
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
        startActivityForResult(gallery, GALLERY_CHANGE_PROFILE)
    }

    fun goBack(view: View) {
        super.onBackPressed()
    }

    private fun redirectToLogin() {
        sharedPref.edit().remove("token").apply()

        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
}