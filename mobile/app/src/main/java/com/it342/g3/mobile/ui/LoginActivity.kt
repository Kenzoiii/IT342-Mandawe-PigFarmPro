package com.it342.g3.mobile.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.it342.g3.mobile.api.ApiClient
import com.it342.g3.mobile.api.LoginRequest
import com.it342.g3.mobile.api.ApiResponse
import com.it342.g3.mobile.api.AuthData
import com.it342.g3.mobile.R
import retrofit2.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val email = findViewById<EditText>(R.id.inputEmail)
        val password = findViewById<EditText>(R.id.inputPassword)
        val message = findViewById<TextView>(R.id.message)
        val loginBtn = findViewById<Button>(R.id.btnLogin)
        val toRegister = findViewById<Button>(R.id.btnToRegister)

        loginBtn.setOnClickListener {
            message.text = ""
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString()
            if (emailText.isEmpty() || passwordText.isEmpty()) {
                message.text = "Email and password are required"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                message.text = "Please enter a valid email"
                return@setOnClickListener
            }

            val req = LoginRequest(emailText, passwordText)
            ApiClient.service.login(req).enqueue(object : Callback<ApiResponse<AuthData>> {
                override fun onResponse(call: Call<ApiResponse<AuthData>>, response: Response<ApiResponse<AuthData>>) {
                    val body = response.body()
                    val token = body?.data?.token
                    if (response.isSuccessful && body?.success == true && !token.isNullOrBlank()) {
                        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                        prefs.edit()
                            .putString("token", token)
                            .putString("username", body.data.username ?: "")
                            .putString("email", body.data.email ?: "")
                            .putString("fullName", body.data.fullName ?: "")
                            .apply()

                        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                        finish()
                    } else {
                        message.text = body?.error?.message ?: body?.message ?: "Login failed"
                    }
                }

                override fun onFailure(call: Call<ApiResponse<AuthData>>, t: Throwable) {
                    message.text = "Network error"
                }
            })
        }

        toRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
