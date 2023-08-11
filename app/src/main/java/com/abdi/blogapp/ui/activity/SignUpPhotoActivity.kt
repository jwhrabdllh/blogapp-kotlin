package com.abdi.blogapp.ui.activity

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.abdi.blogapp.R
import com.abdi.blogapp.data.api.ApiConfig
import com.abdi.blogapp.utils.Constant
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
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
            val gallery = Intent(Intent.ACTION_PICK)
            gallery.type = "image/*"
            startActivityForResult(gallery, GALLERY_ADD_PROFILE)
        }

        btnContinue.setOnClickListener {
            savePhotoProfile()
        }
    }

    private fun savePhotoProfile() {
        dialog.setMessage("Loading..")
        dialog.show()

        val token = sharedPref.getString("token", "") ?: ""
        if (token.isEmpty()) {
            Toast.makeText(this, "Session berakhir. Silahkan login kembali.", Toast.LENGTH_SHORT).show()
            redirectToLogin()
            return
        }
        val authorization = "Bearer $token"
        val photo = bitmapToString(bitmap)

        CoroutineScope(Dispatchers.IO).launch {
            try {
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
                    } else {
                        Toast.makeText(this@SignUpPhotoActivity, "Terjadi kesalahan pada server", Toast.LENGTH_SHORT).show()
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

    @Deprecated("Deprecated in Kotlin")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_ADD_PROFILE && resultCode == RESULT_OK) {
            data?.data?.let { imgUri ->
                circleImageView.setImageURI(imgUri)
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imgUri)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
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

    private fun redirectToLogin() {
        sharedPref.edit()
            .remove("token")
            .apply()

        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
}