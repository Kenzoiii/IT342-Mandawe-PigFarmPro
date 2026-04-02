package com.it342.g3.mobile.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.it342.g3.mobile.api.ApiClient
import com.it342.g3.mobile.api.MessageResponse
import com.it342.g3.mobile.api.UserProfile
import com.it342.g3.mobile.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val username = findViewById<TextView>(R.id.textUsername)
        val email = findViewById<TextView>(R.id.textEmail)
        val role = findViewById<TextView>(R.id.textRole)
        val message = findViewById<TextView>(R.id.message)
        val logoutBtn = findViewById<Button>(R.id.btnLogout)
        val greeting = findViewById<TextView>(R.id.textGreeting)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        val savedName = prefs.getString("fullName", "") ?: ""
        val savedUsername = prefs.getString("username", "") ?: ""
        if (token.isEmpty()) {
            message.text = "Not logged in"
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        greeting.text = if (savedName.isNotBlank()) {
            "Welcome, $savedName"
        } else {
            "Welcome, $savedUsername"
        }

        ApiClient.service.me("Bearer $token").enqueue(object : Callback<UserProfile> {
            override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    username.text = "Username: ${body.username ?: savedUsername}"
                    email.text = "Email: ${body.email ?: prefs.getString("email", "")}" 
                    role.text = "Role: ${body.role ?: "USER"}"
                    if (!body.fullName.isNullOrBlank()) {
                        greeting.text = "Welcome, ${body.fullName}"
                    }
                } else {
                    // Keep local info visible even if /me fails.
                    username.text = "Username: $savedUsername"
                    email.text = "Email: ${prefs.getString("email", "")}" 
                    role.text = "Role: USER"
                    message.text = "Unable to refresh profile"
                }
            }
            override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                username.text = "Username: $savedUsername"
                email.text = "Email: ${prefs.getString("email", "")}" 
                role.text = "Role: USER"
                message.text = "Network error"
            }
        })

        logoutBtn.setOnClickListener {
            ApiClient.service.logout("Bearer $token").enqueue(object : Callback<MessageResponse> {
                override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                    prefs.edit().remove("token").apply()
                    startActivity(Intent(this@DashboardActivity, LoginActivity::class.java))
                    finish()
                }
                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    prefs.edit().remove("token").apply()
                    startActivity(Intent(this@DashboardActivity, LoginActivity::class.java))
                    finish()
                }
            })
        }
    }
}
