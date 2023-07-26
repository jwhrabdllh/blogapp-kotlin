package com.abdi.blogapp.UI

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
import com.abdi.blogapp.Utils.ApiConfig
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class SignUpPhotoActivity : AppCompatActivity() {
    private lateinit var tvWelcome: TextView
    private lateinit var btnContinue: Button
    private lateinit var ivAddProfile: ImageView
    private lateinit var circleImageView: CircleImageView
    private var bitmap: Bitmap? = null
    private lateinit var userPref: SharedPreferences
    private lateinit var dialog: ProgressDialog
    private val GALLERY_ADD_PROFILE = 1

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
        userPref = applicationContext.getSharedPreferences("user", MODE_PRIVATE)

        val greeting = "Selamat Datang,\n${userPref.getString("name", "")} ${userPref.getString("lastname", "")}"
        tvWelcome.text = greeting

        // Ambil foto dari galeri
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
        dialog.setMessage("Loading")
        dialog.show()

        val request = object : StringRequest(Method.POST, ApiConfig.ADD_SIGN_UP_PHOTO, { response ->
                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.getBoolean("success")) {
                        val editor = userPref.edit()
                        editor.putString("photo", jsonObject.getString("photo"))
                        editor.apply()
                        startActivity(Intent(this@SignUpPhotoActivity, HomeActivity::class.java))
                        Toast.makeText(this@SignUpPhotoActivity, "Sukses", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(this@SignUpPhotoActivity, "Gagal", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    e.printStackTrace()
                }
                dialog.dismiss()
            },
            { error ->
                error.printStackTrace()
                dialog.dismiss()
                Toast.makeText(this@SignUpPhotoActivity, "Koneksi terputus", Toast.LENGTH_SHORT).show()
            }) {

            override fun getHeaders(): MutableMap<String, String> {
                val token = userPref.getString("token", "") ?: ""
                val map = HashMap<String, String>()
                map["Authorization"] = "Bearer $token"
                return map
            }

            override fun getParams(): MutableMap<String, String> {
                val map = HashMap<String, String>()
                bitmap?.let { bitmap -> map["photo"] = bitmapToString(bitmap) }
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

            override fun retry(error: VolleyError) {

            }
        }

        val queue = Volley.newRequestQueue(this@SignUpPhotoActivity)
        queue.add(request)
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
}