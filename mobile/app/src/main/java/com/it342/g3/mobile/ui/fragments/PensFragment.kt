package com.it342.g3.mobile.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.it342.g3.mobile.R
import com.it342.g3.mobile.api.ApiClient
import com.it342.g3.mobile.api.ApiResponse
import com.it342.g3.mobile.api.CreatePenRequest
import com.it342.g3.mobile.api.DashboardPayload
import com.it342.g3.mobile.api.PenSummary
import com.it342.g3.mobile.auth.AuthStore
import com.it342.g3.mobile.ui.PenDetailActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PensFragment : Fragment(R.layout.fragment_pens) {
    private var pens: List<PenSummary> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val addButton = view.findViewById<Button>(R.id.btnAddPen)
        addButton.setOnClickListener { openCreateDialog() }
        loadPens()
    }

    override fun onResume() {
        super.onResume()
        loadPens()
    }

    private fun loadPens() {
        val message = view?.findViewById<TextView>(R.id.pensMessage) ?: return
        val container = view?.findViewById<LinearLayout>(R.id.pensContainer) ?: return
        message.text = ""
        container.removeAllViews()

        val token = AuthStore.getToken(requireContext())
        if (token.isBlank()) {
            message.text = "Not logged in"
            return
        }

        ApiClient.service.dashboard("Bearer $token").enqueue(object : Callback<ApiResponse<DashboardPayload>> {
            override fun onResponse(
                call: Call<ApiResponse<DashboardPayload>>,
                response: Response<ApiResponse<DashboardPayload>>
            ) {
                val body = response.body()
                if (!response.isSuccessful || body?.success != true || body.data == null) {
                    message.text = body?.message ?: "Unable to load pens"
                    return
                }

                pens = body.data.pens ?: emptyList()
                renderPens(container)
            }

            override fun onFailure(call: Call<ApiResponse<DashboardPayload>>, t: Throwable) {
                message.text = "Network error"
            }
        })
    }

    private fun renderPens(container: LinearLayout) {
        container.removeAllViews()
        if (pens.isEmpty()) {
            val emptyView = TextView(requireContext())
            emptyView.text = "No pens yet"
            emptyView.setTextColor(resources.getColor(R.color.text_muted, null))
            container.addView(emptyView)
            return
        }

        pens.forEach { pen ->
            val card = layoutInflater.inflate(R.layout.view_record_card, container, false)
            val title = card.findViewById<TextView>(R.id.recordTitle)
            val subtitle = card.findViewById<TextView>(R.id.recordSubtitle)
            val meta = card.findViewById<TextView>(R.id.recordMeta)
            val status = card.findViewById<TextView>(R.id.recordStatus)
            val primary = card.findViewById<Button>(R.id.btnPrimary)

            title.text = pen.name ?: pen.identifier ?: "Pen"
            subtitle.text = "Capacity ${pen.occupied ?: 0} / ${pen.capacity ?: 0}"
            meta.text = pen.description ?: ""
            status.text = pen.status ?: "Active"

            primary.visibility = View.VISIBLE
            primary.text = "View"
            primary.setOnClickListener {
                if (pen.id != null) {
                    val intent = Intent(requireContext(), PenDetailActivity::class.java)
                    intent.putExtra(PenDetailActivity.EXTRA_PEN_ID, pen.id)
                    startActivity(intent)
                }
            }

            container.addView(card)
            val params = card.layoutParams as LinearLayout.LayoutParams
            params.bottomMargin = resources.getDimensionPixelSize(R.dimen.list_gap)
            card.layoutParams = params
        }
    }

    private fun openCreateDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_pen_form, null)
        val inputIdentifier = dialogView.findViewById<EditText>(R.id.inputPenIdentifier)
        val inputName = dialogView.findViewById<EditText>(R.id.inputPenName)
        val inputCapacity = dialogView.findViewById<EditText>(R.id.inputPenCapacity)
        val inputDescription = dialogView.findViewById<EditText>(R.id.inputPenDescription)
        val message = view?.findViewById<TextView>(R.id.pensMessage)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Create Pen")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = inputName.text.toString().trim()
                val capacity = inputCapacity.text.toString().trim().toIntOrNull()

                if (name.isBlank()) {
                    inputName.error = "Pen name is required"
                    return@setOnClickListener
                }
                if (capacity == null || capacity <= 0) {
                    inputCapacity.error = "Capacity must be greater than zero"
                    return@setOnClickListener
                }

                val token = AuthStore.getToken(requireContext())
                if (token.isBlank()) {
                    message?.text = "Not logged in"
                    return@setOnClickListener
                }

                val request = CreatePenRequest(
                    inputIdentifier.text.toString().trim().ifBlank { null },
                    name,
                    inputDescription.text.toString().trim().ifBlank { null },
                    capacity
                )

                ApiClient.service.createPen("Bearer $token", request)
                    .enqueue(object : Callback<ApiResponse<PenSummary>> {
                        override fun onResponse(
                            call: Call<ApiResponse<PenSummary>>,
                            response: Response<ApiResponse<PenSummary>>
                        ) {
                            val body = response.body()
                            if (!response.isSuccessful || body?.success != true) {
                                message?.text = body?.message ?: "Unable to create pen"
                                return
                            }
                            dialog.dismiss()
                            loadPens()
                        }

                        override fun onFailure(call: Call<ApiResponse<PenSummary>>, t: Throwable) {
                            message?.text = "Network error"
                        }
                    })
            }
        }

        dialog.show()
    }
}
