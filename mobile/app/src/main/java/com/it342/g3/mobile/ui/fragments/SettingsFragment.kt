package com.it342.g3.mobile.ui.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.it342.g3.mobile.R
import com.it342.g3.mobile.api.ApiClient
import com.it342.g3.mobile.api.ApiResponse
import com.it342.g3.mobile.api.UpdatePasswordRequest
import com.it342.g3.mobile.api.UpdateProfileRequest
import com.it342.g3.mobile.api.UserProfile
import com.it342.g3.mobile.auth.AuthStore
import com.it342.g3.mobile.util.UiFormat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val fullName = view.findViewById<EditText>(R.id.inputFullName)
        val username = view.findViewById<EditText>(R.id.inputUsername)
        val email = view.findViewById<EditText>(R.id.inputEmail)
        val profileMessage = view.findViewById<TextView>(R.id.profileMessage)
        val profileMeta = view.findViewById<TextView>(R.id.profileMeta)
        val saveProfile = view.findViewById<Button>(R.id.btnSaveProfile)

        val currentPassword = view.findViewById<EditText>(R.id.inputCurrentPassword)
        val newPassword = view.findViewById<EditText>(R.id.inputNewPassword)
        val passwordMessage = view.findViewById<TextView>(R.id.passwordMessage)
        val savePassword = view.findViewById<Button>(R.id.btnSavePassword)

        profileMessage.text = ""
        passwordMessage.text = ""

        val token = AuthStore.getToken(requireContext())
        if (token.isBlank()) {
            profileMessage.text = "Not logged in"
            return
        }

        ApiClient.service.me("Bearer $token").enqueue(object : Callback<UserProfile> {
            override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                val body = response.body()
                if (!response.isSuccessful || body == null) {
                    profileMessage.text = "Unable to load profile"
                    return
                }

                fullName.setText(body.fullName ?: "")
                username.setText(body.username ?: "")
                email.setText(body.email ?: "")
                profileMeta.text = if (!body.createdAt.isNullOrBlank()) {
                    "Joined ${UiFormat.displayDate(body.createdAt)}"
                } else {
                    ""
                }
            }

            override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                profileMessage.text = "Network error"
            }
        })

        saveProfile.setOnClickListener {
            profileMessage.text = ""
            val nameText = fullName.text.toString().trim()
            val usernameText = username.text.toString().trim()
            val emailText = email.text.toString().trim()

            if (emailText.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                email.error = "Invalid email"
                return@setOnClickListener
            }

            val request = UpdateProfileRequest(
                nameText.ifBlank { null },
                usernameText.ifBlank { null },
                emailText.ifBlank { null }
            )

            ApiClient.service.updateProfile("Bearer $token", request)
                .enqueue(object : Callback<ApiResponse<UserProfile>> {
                    override fun onResponse(
                        call: Call<ApiResponse<UserProfile>>,
                        response: Response<ApiResponse<UserProfile>>
                    ) {
                        val body = response.body()
                        if (!response.isSuccessful || body?.success != true) {
                            profileMessage.text = body?.message ?: "Unable to update profile"
                            return
                        }

                        val updated = body.data
                        AuthStore.updateProfile(requireContext(), updated?.fullName, updated?.username, updated?.email)
                        profileMessage.text = "Profile updated"
                    }

                    override fun onFailure(call: Call<ApiResponse<UserProfile>>, t: Throwable) {
                        profileMessage.text = "Network error"
                    }
                })
        }

        savePassword.setOnClickListener {
            passwordMessage.text = ""
            val currentText = currentPassword.text.toString()
            val newText = newPassword.text.toString()

            if (currentText.isBlank() || newText.isBlank()) {
                passwordMessage.text = "Current and new password are required"
                return@setOnClickListener
            }

            val request = UpdatePasswordRequest(currentText, newText)
            ApiClient.service.updatePassword("Bearer $token", request)
                .enqueue(object : Callback<ApiResponse<Any>> {
                    override fun onResponse(
                        call: Call<ApiResponse<Any>>,
                        response: Response<ApiResponse<Any>>
                    ) {
                        val body = response.body()
                        if (!response.isSuccessful || body?.success != true) {
                            passwordMessage.text = body?.message ?: "Unable to update password"
                            return
                        }
                        passwordMessage.text = "Password updated"
                        currentPassword.text.clear()
                        newPassword.text.clear()
                    }

                    override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                        passwordMessage.text = "Network error"
                    }
                })
        }
    }
}
