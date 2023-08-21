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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.abdi.blogapp.R
import com.abdi.blogapp.data.api.ApiConfig
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class SignUpPhotoActivity : AppCompatActivity() {
    private lateinit var tvWelcome: TextView
    private lateinit var btnContinue: Button
    private lateinit var ivAddProfile: ImageView
    private lateinit var circleImageView: CircleImageView
    private lateinit var sharedPref: SharedPreferences
    private lateinit var dialog: ProgressDialog
    private var bitmap: Bitmap? = null

    companion object {
        private const val GALLERY_ADD_PROFILE = 1
        private const val PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 14
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_photo)
        init()
    }

    private fun init() {
        ivAddProfile = findViewById(R.id.ivAddProfile)
        tvWelcome = findViewById(R.id.tvWelcome)
        circleImageView = findViewById(R.id.ivProfile)
        btnContinue = findViewById(R.id.btnContinue)
        dialog = ProgressDialog(this)
        dialog.setCancelable(false)
        sharedPref = applicationContext.getSharedPreferences("user", MODE_PRIVATE)

        val greeting = "Selamat Datang,\n${sharedPref.getString("name", "")} ${sharedPref.getString("lastname", "")}"
        tvWelcome.text = greeting

        ivAddProfile.setOnClickListener {
            checkPermissionPhoto()
        }

        btnContinue.setOnClickListener {
            if (bitmap == null) {
                Toast.makeText(this, "Wajib menambahkan foto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                savePhotoProfile()
            }
        }
    }

    private fun savePhotoProfile() {
        dialog.setMessage("Loading..")
        dialog.show()

        val token = sharedPref.getString("token", "") ?: ""
        val authorization = "Bearer $token"
        val photo = bitmapToString(bitmap)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isImageChanged = bitmap != null
                if (isImageChanged) {
                    val isImageSizeValid = validateImageSize(bitmap!!)
                    if (!isImageSizeValid) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SignUpPhotoActivity, "Ukuran gambar terlalu besar", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        return@launch
                    }
                }

                val response = ApiConfig.apiService.savePhotoProfile(authorization, photo)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val photoResponse = response.body()
                        if (photoResponse != null && photoResponse.success) {
                            val editor = sharedPref.edit()
                            editor.putString("photo", photoResponse.photo)
                            editor.apply()

                            startActivity(Intent(this@SignUpPhotoActivity, HomeActivity::class.java))
                            Toast.makeText(this@SignUpPhotoActivity, "Sukses", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@SignUpPhotoActivity, "Gagal menyimpan foto", Toast.LENGTH_SHORT).show()
                        }
                    } else if (response.code() == 401 || response.code() == 422 ){
                        Toast.makeText(this@SignUpPhotoActivity, "Photo tidak valid", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                dialog.dismiss()
                Toast.makeText(this@SignUpPhotoActivity, "Kesalahan koneksi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateImageSize(bitmap: Bitmap): Boolean {
        val maxSizeKB = 500
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val array = byteArrayOutputStream.toByteArray()
        val fileSizeInKB = array.size / 1024
        return fileSizeInKB <= maxSizeKB
    }

    @Deprecated("Deprecated in Kotlin")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_ADD_PROFILE && resultCode == RESULT_OK) {
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

    private fun bitmapToString(bitmap: Bitmap?): String {
        bitmap?.let {
            val byteArrayOutputStream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val array = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(array, Base64.DEFAULT)
        }
        return ""
    }

    private fun checkPermissionPhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_READ_EXTERNAL_STORAGE
            )
        } else {
            openGallery()
        }
    }

    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK)
        gallery.type = "image/*"
        startActivityForResult(gallery, GALLERY_ADD_PROFILE)
    }
}