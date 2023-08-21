package com.abdi.blogapp.ui.activity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.abdi.blogapp.R
import com.abdi.blogapp.data.api.ApiConfig
import com.abdi.blogapp.utils.Constant
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class SignUpActivity : AppCompatActivity() {
    private lateinit var layoutName: TextInputLayout
    private lateinit var layoutLastname: TextInputLayout
    private lateinit var layoutEmail: TextInputLayout
    private lateinit var layoutPassword: TextInputLayout
    private lateinit var edtName: TextInputEditText
    private lateinit var edtLastname: TextInputEditText
    private lateinit var edtEmail: TextInputEditText
    private lateinit var edtPassword: TextInputEditText
    private lateinit var btnDaftar: Button
    private lateinit var tvLogin: TextView
    private lateinit var dialog: ProgressDialog
    private val errorValidation: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        init()
    }

    private fun init() {
        layoutName = findViewById(R.id.txtLayoutName)
        layoutLastname = findViewById(R.id.txtLayoutLastname)
        layoutEmail = findViewById(R.id.txtLayoutEmailDaftar)
        layoutPassword = findViewById(R.id.txtLayoutPasswordDaftar)
        edtName = findViewById(R.id.txtEdtName)
        edtLastname = findViewById(R.id.txtEdtLastname)
        edtEmail = findViewById(R.id.txtEdtEmailDaftar)
        edtPassword = findViewById(R.id.txtEdtPasswordDaftar)
        btnDaftar = findViewById(R.id.btnDaftar)
        tvLogin = findViewById(R.id.tvLogin)
        dialog = ProgressDialog(this)
        dialog.setCancelable(false)

        tvLogin.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        btnDaftar.setOnClickListener {
            validate()
            if (errorValidation.isEmpty()) {
                register()
            }
        }

        edtName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!edtName.text.toString().isEmpty()) {
                    layoutName.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        edtLastname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!edtLastname.text.toString().isEmpty()) {
                    layoutLastname.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

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
                if (edtPassword.text.toString().length >= 6) {
                    layoutPassword.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
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
        if (errorValidation.contains("Nama depan")) {
            layoutName.error = "Nama depan tidak boleh kosong"
        }
        if (errorValidation.contains("Nama belakang")) {
            layoutLastname.error = "Nama belakang tidak boleh kosong"
        }
        if (errorValidation.contains("Email")) {
            layoutEmail.error = "Email tidak boleh kosong"
        }
        if (errorValidation.contains("Password")) {
            layoutPassword.error = "Password minimal 6 karakter"
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun register() {
        dialog.setMessage("Loading")
        dialog.show()

        val name = edtName.text.toString().trim()
        val lastname = edtLastname.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val password = edtPassword.text.toString().trim()

        if (!isEmailValid(email)) {
            Toast.makeText(this@SignUpActivity, "Email anda tidak valid", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiConfig.apiService.register(name, lastname, email, password)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val registerResponse = response.body()
                        if (registerResponse != null && registerResponse.success) {
                            val user = registerResponse.user

                            val userPref = getSharedPreferences("user", MODE_PRIVATE)
                            val editor = userPref.edit()
                            editor.putInt("id", user.id)
                            editor.putString("token", registerResponse.token)
                            editor.putString("name", user.name)
                            editor.putString("lastname", user.lastname)
                            editor.putString("email", user.email)
                            editor.putString("photo", user.photo)
                            editor.putBoolean("isLogin", true)
                            editor.apply()

                            startActivity(Intent(this@SignUpActivity, SignUpPhotoActivity::class.java))
                            finish()
                            Toast.makeText(this@SignUpActivity, "Register sukses", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@SignUpActivity, "Register gagal", Toast.LENGTH_SHORT).show()
                        }
                    } else if (response.code() == 401 || response.code() == 422 ){
                        Toast.makeText(this@SignUpActivity, "Email sudah terdaftar", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SignUpActivity, "Kesalahan koneksi", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }
    }
}
