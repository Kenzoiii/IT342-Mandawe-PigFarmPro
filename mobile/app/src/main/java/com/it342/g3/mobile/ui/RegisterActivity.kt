package com.it342.g3.mobile.ui

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.it342.g3.mobile.api.ApiClient
import com.it342.g3.mobile.api.RegisterRequest
import com.it342.g3.mobile.R
import com.it342.g3.mobile.api.ApiResponse
import com.it342.g3.mobile.api.AuthData
import retrofit2.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val fullName = findViewById<EditText>(R.id.inputName)
        val username = findViewById<EditText>(R.id.inputUsername)
        val email = findViewById<EditText>(R.id.inputEmail)
        val password = findViewById<EditText>(R.id.inputPassword)
        val message = findViewById<TextView>(R.id.message)
        val registerBtn = findViewById<Button>(R.id.btnRegister)
        val toLogin = findViewById<Button>(R.id.btnToLogin)

        toLogin.setOnClickListener { finish() }

        registerBtn.setOnClickListener {
            message.text = ""
            val nameText = fullName.text.toString().trim()
            val usernameText = username.text.toString().trim()
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString()

            if (usernameText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty()) {
                message.text = "Username, email, and password are required"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                message.text = "Please enter a valid email"
                return@setOnClickListener
            }

            if (passwordText.length < 8) {
                message.text = "Password must be at least 8 characters"
                return@setOnClickListener
            }

            val req = RegisterRequest(usernameText, emailText, passwordText, nameText)

            ApiClient.service.register(req).enqueue(object : Callback<ApiResponse<AuthData>> {
                override fun onResponse(call: Call<ApiResponse<AuthData>>, response: Response<ApiResponse<AuthData>>) {
                    val body = response.body()
                    if (response.isSuccessful && body?.success == true) {
                        message.text = body.message ?: "Registration successful"
                        fullName.text.clear()
                        email.text.clear()
                        password.text.clear()
                    } else {
                        val fallback = body?.error?.message ?: body?.message ?: "Registration failed"
                        message.text = fallback
                    }
                }

                override fun onFailure(call: Call<ApiResponse<AuthData>>, t: Throwable) {
                    message.text = "Network error"
                }
            })
        }
    }
}
