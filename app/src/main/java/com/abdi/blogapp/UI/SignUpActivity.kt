package com.abdi.blogapp.UI

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

class SignUpActivity : AppCompatActivity() {
    private lateinit var view: View
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
            // Pindah ke SignInActivity
            startActivity(Intent(this, SignInActivity::class.java))
        }

        btnDaftar.setOnClickListener {
            if (validate()) {
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
        if (edtName.text.toString().isEmpty()) {
            layoutName.error = "Nama depan harus diisi"
            return false
        }
        if (edtLastname.text.toString().isEmpty()) {
            layoutLastname.error = "Nama belakang harus diisi"
            return false
        }
        if (edtEmail.text.toString().trim().isEmpty()) {
            layoutEmail.error = "Email harus diisi"
            return false
        }
        if (edtPassword.text.toString().length < 6) {
            layoutPassword.error = "Password minimal 6 karakter"
            return false
        }
        return true
    }

    private fun register() {
        dialog.setMessage("Loading..")
        dialog.show()

        val request = object : StringRequest(Method.POST, ApiConfig.REGISTER,
            Response.Listener { response ->
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
                map["name"] = edtName.text.toString().trim()
                map["lastname"] = edtLastname.text.toString().trim()
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
