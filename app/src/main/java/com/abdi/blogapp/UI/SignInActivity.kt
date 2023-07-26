package com.abdi.blogapp.UI

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
import androidx.constraintlayout.motion.widget.TransitionBuilder.validate
import com.abdi.blogapp.R
import com.abdi.blogapp.Utils.ApiConfig
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONException
import org.json.JSONObject

class SignInActivity : AppCompatActivity() {
    private lateinit var layoutEmail: TextInputLayout
    private lateinit var layoutPassword: TextInputLayout
    private lateinit var edtEmail: TextInputEditText
    private lateinit var edtPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var tvDaftar: TextView
    private lateinit var dialog: ProgressDialog

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
            if (validate()) {
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
                val email = edtEmail.text.toString()

                if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    layoutEmail.error = null
                } else {
                    layoutEmail.error = "Email tidak valid"
                }
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
        if (edtEmail.text.toString().isEmpty()) {
            layoutEmail.error = "Email harus diisi"
            return false
        }
        if (edtPassword.text.toString().length < 6) {
            layoutPassword.error = "Password minimal 6 karakter"
            return false
        }
        return true
    }

    private fun login() {
        dialog.setMessage("Loading")
        dialog.show()

        val request = object : StringRequest(Method.POST, ApiConfig.LOGIN, Response.Listener { response ->
                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.getBoolean("success")) {
                        val user = jsonObject.getJSONObject("user")

                        val userPref = getSharedPreferences("user", MODE_PRIVATE)
                        val editor = userPref.edit()
                        editor.putInt("id", user.getInt("id"))
                        editor.putString("token", jsonObject.getString("token"))
                        editor.putString("name", user.getString("name"))
                        editor.putString("lastname", user.getString("lastname"))
                        editor.putString("photo", user.getString("photo"))
                        editor.putBoolean("isLogin", true)
                        editor.apply()

                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                        Toast.makeText(this, "Sukses", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(this, "Gagal", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    e.printStackTrace()
                }
                dialog.dismiss()
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
                dialog.dismiss()
                Toast.makeText(this, "Koneksi gagal", Toast.LENGTH_SHORT).show()
            }) {

            override fun getParams(): MutableMap<String, String> {
                val map = HashMap<String, String>()
                map["email"] = edtEmail.text.toString().trim()
                map["password"] = edtPassword.text.toString().trim()
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

        val queue = Volley.newRequestQueue(this)
        queue.add(request)
    }
}