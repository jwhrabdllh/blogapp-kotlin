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
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.abdi.blogapp.R
import com.abdi.blogapp.data.api.ApiConfig
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

class EditProfileActivity : AppCompatActivity() {
    private lateinit var view: View
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
    private val errorValidation: MutableList<String> = mutableListOf()

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
            checkPermissionPhoto()
        }

        btnSave.setOnClickListener {
            validate()
            if (errorValidation.isEmpty()) {
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
            }
        })
    }
    private fun validate(): Boolean {
        errorValidation.clear()

        if (edtName.text.toString().isEmpty()) {
            errorValidation.add("Nama depan")
        }
        if (edtLastname.text.toString().isEmpty()) {
            errorValidation.add("Nama belakang")
        }
        if (edtEmail.text.toString().isEmpty()) {
            errorValidation.add("Email")
        }

        if (errorValidation.isNotEmpty()) {
            showMessageErrorValidation()
            return false
        }
        return true
    }

    private fun showMessageErrorValidation() {
        if (errorValidation.contains("Nama depan")) {
            layoutName.error = "Nama depan tidak boleh kosong"
        }
        if (errorValidation.contains("Nama belakang")) {
            layoutLastname.error = "Nama belakang tidak boleh kosong"
        }
        if (errorValidation.contains("Email")) {
            layoutEmail.error = "Email tidak boleh kosong"
        }
    }

    private fun updateProfile() {
        dialog.setMessage("Loading..")
        dialog.show()

        val name = edtName.text.toString().trim()
        val lastname = edtLastname.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val photo = bitmapToString(bitmap)

        val token = sharedPref.getString("token", "") ?: ""
        val authorization = "Bearer $token"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isImageChanged = bitmap != null
                if (isImageChanged) {
                    val isImageSizeValid = validateImageSize(bitmap!!)
                    if (!isImageSizeValid) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@EditProfileActivity, "Ukuran gambar terlalu besar", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        return@launch
                    }
                }

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
                    } else if (response.code() == 422) {
                        Toast.makeText(this@EditProfileActivity, "Email sudah terdaftar", Toast.LENGTH_SHORT).show()
                    } else if (response.code() == 401) {
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
                    e.printStackTrace()
                    Toast.makeText(this@EditProfileActivity, "Kesalahan koneksi", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateImageSize(bitmap: Bitmap): Boolean {
        val maxSizeKB = 1024
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val array = byteArrayOutputStream.toByteArray()
        val fileSizeInKB = array.size / 1024
        return fileSizeInKB <= maxSizeKB
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

            CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL) // Ubah bentuk pemangkasan sesuai kebutuhan Anda
                .start(this)
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val croppedUri = result.uri
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, croppedUri)
                    circleImageView.setImageBitmap(bitmap)
                    this.bitmap = bitmap
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissionPhoto() {
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