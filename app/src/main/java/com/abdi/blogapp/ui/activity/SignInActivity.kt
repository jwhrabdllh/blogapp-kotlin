package com.abdi.blogapp.ui.activity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.abdi.blogapp.R
import com.abdi.blogapp.data.api.ApiConfig
import com.abdi.blogapp.utils.Constant
import com.abdi.blogapp.data.api.ApiService
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SignInActivity : AppCompatActivity() {
    private lateinit var layoutEmail: TextInputLayout
    private lateinit var layoutPassword: TextInputLayout
    private lateinit var edtEmail: TextInputEditText
    private lateinit var edtPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var tvDaftar: TextView
    private lateinit var dialog: ProgressDialog
    private val errorValidation: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        init()
    }

    private fun init() {
        layoutPassword = findViewById(R.id.txtLayoutPasswordLogin)
        layoutEmail = findViewById(R.id.txtLayoutEmailLogin)
        edtPassword = findViewById(R.id.txtPasswordLogin)
        tvDaftar = findViewById(R.id.tvDaftar)
        edtEmail = findViewById(R.id.txtEmailLogin)
        btnLogin = findViewById(R.id.btnLogin)
        dialog = ProgressDialog(this)
        dialog.setCancelable(false)

        tvDaftar.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        btnLogin.setOnClickListener {
            validate()
            if (errorValidation.isEmpty()) {
                login()
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

        edtPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (edtPassword.text.toString().length > 6) {
                    layoutPassword.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validate(): Boolean {
        errorValidation.clear()

        if (edtEmail.text.toString().isEmpty()) {
            errorValidation.add("Email")
        }
        if (edtPassword.text.toString().length < 6) {
            errorValidation.add("Password")
        }

        if (errorValidation.isNotEmpty()) {
            showMessageErrorValidation()
            return false
        }
        return true
    }

    private fun showMessageErrorValidation() {
        if (errorValidation.contains("Email")) {
            layoutEmail.error = "Email tidak boleh kosong"
        }
        if (errorValidation.contains("Password")) {
            layoutPassword.error = "Password minimal 6 karakter"
        }
    }

    private fun login() {
        dialog.setMessage("Loading..")
        dialog.show()

        val email = edtEmail.text.toString().trim()
        val password = edtPassword.text.toString().trim()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiConfig.apiService.login(email, password)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        if (loginResponse != null && loginResponse.success) {
                            val user = loginResponse.user
                            if (user != null) {
                                val userPref = getSharedPreferences("user", MODE_PRIVATE)
                                val editor = userPref.edit()
                                editor.putInt("id", user.id)
                                editor.putString("token", loginResponse.token)
                                editor.putString("name", user.name)
                                editor.putString("lastname", user.lastname)
                                editor.putString("email", user.email)
                                editor.putString("photo", user.photo)
                                editor.putBoolean("isLogin", true)
                                editor.apply()

                                startActivity(Intent(this@SignInActivity, HomeActivity::class.java))
                                finish()
                                Toast.makeText(this@SignInActivity, "Login sukses", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else if (response.code() == 401 || response.code() == 422) {
                        Toast.makeText(this@SignInActivity, "Email atau password salah", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SignInActivity, "Kesalahan koneksi", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }
    }
}