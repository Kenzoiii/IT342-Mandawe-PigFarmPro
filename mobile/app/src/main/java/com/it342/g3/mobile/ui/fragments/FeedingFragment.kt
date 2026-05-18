package com.it342.g3.mobile.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.it342.g3.mobile.R
import com.it342.g3.mobile.api.ApiClient
import com.it342.g3.mobile.api.ApiResponse
import com.it342.g3.mobile.api.CreateFeedingRequest
import com.it342.g3.mobile.api.DashboardPayload
import com.it342.g3.mobile.api.FeedingRecord
import com.it342.g3.mobile.api.PenSummary
import com.it342.g3.mobile.api.UpdateFeedingRequest
import com.it342.g3.mobile.auth.AuthStore
import com.it342.g3.mobile.util.UiFormat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedingFragment : Fragment(R.layout.fragment_feeding) {
    private var pens: List<PenSummary> = emptyList()
    private var feedings: List<FeedingRecord> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.btnAddFeeding).setOnClickListener { openFeedingDialog(null) }
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val message = view?.findViewById<TextView>(R.id.feedingMessage) ?: return
        message.text = ""

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
                pens = response.body()?.data?.pens ?: emptyList()
                loadFeedings(token)
            }

            override fun onFailure(call: Call<ApiResponse<DashboardPayload>>, t: Throwable) {
                message.text = "Unable to load pens"
            }
        })
    }

    private fun loadFeedings(token: String) {
        val message = view?.findViewById<TextView>(R.id.feedingMessage) ?: return
        val container = view?.findViewById<LinearLayout>(R.id.feedingContainer) ?: return

        ApiClient.service.getFeedings("Bearer $token").enqueue(object : Callback<ApiResponse<List<FeedingRecord>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<FeedingRecord>>>,
                response: Response<ApiResponse<List<FeedingRecord>>>
            ) {
                val body = response.body()
                if (!response.isSuccessful || body?.success != true) {
                    message.text = body?.message ?: "Unable to load feeding history"
                    return
                }
                feedings = body.data ?: emptyList()
                renderFeedings(container)
            }

            override fun onFailure(call: Call<ApiResponse<List<FeedingRecord>>>, t: Throwable) {
                message.text = "Network error"
            }
        })
    }

    private fun renderFeedings(container: LinearLayout) {
        container.removeAllViews()
        if (feedings.isEmpty()) {
            val emptyView = TextView(requireContext())
            emptyView.text = "No feeding records yet"
            emptyView.setTextColor(resources.getColor(R.color.text_muted, null))
            container.addView(emptyView)
            return
        }

        feedings.forEach { record ->
            val card = layoutInflater.inflate(R.layout.view_record_card, container, false)
            val title = card.findViewById<TextView>(R.id.recordTitle)
            val subtitle = card.findViewById<TextView>(R.id.recordSubtitle)
            val meta = card.findViewById<TextView>(R.id.recordMeta)
            val status = card.findViewById<TextView>(R.id.recordStatus)
            val primary = card.findViewById<Button>(R.id.btnPrimary)
            val secondary = card.findViewById<Button>(R.id.btnSecondary)

            title.text = record.feedType ?: "Feeding"
            subtitle.text = "Pen: ${record.penName ?: record.penIdentifier ?: "-"}"
            meta.text = "Qty ${record.quantity ?: 0.0} ${record.unit ?: "kg"} | ${UiFormat.displayDateTime(record.feedingTime ?: record.createdAt)}"
            status.text = record.recordedBy ?: "Recorded"

            primary.visibility = View.VISIBLE
            primary.text = "Edit"
            primary.setOnClickListener { openFeedingDialog(record) }

            secondary.visibility = View.VISIBLE
            secondary.text = "Delete"
            secondary.setOnClickListener { deleteFeeding(record) }

            container.addView(card)
            val params = card.layoutParams as LinearLayout.LayoutParams
            params.bottomMargin = resources.getDimensionPixelSize(R.dimen.list_gap)
            card.layoutParams = params
        }
    }

    private fun openFeedingDialog(record: FeedingRecord?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_feeding_form, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerPen)
        val inputType = dialogView.findViewById<EditText>(R.id.inputFeedType)
        val inputQty = dialogView.findViewById<EditText>(R.id.inputFeedQuantity)
        val inputUnit = dialogView.findViewById<EditText>(R.id.inputFeedUnit)
        val inputCost = dialogView.findViewById<EditText>(R.id.inputFeedCost)
        val inputTime = dialogView.findViewById<EditText>(R.id.inputFeedTime)
        val inputNotes = dialogView.findViewById<EditText>(R.id.inputFeedNotes)
        val message = view?.findViewById<TextView>(R.id.feedingMessage)

        val labels = pens.map { it.name ?: it.identifier ?: "Pen" }
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, labels)

        if (record != null) {
            val index = pens.indexOfFirst { it.id == record.penId }
            if (index >= 0) {
                spinner.setSelection(index)
            }
            inputType.setText(record.feedType ?: "")
            inputQty.setText(record.quantity?.toString() ?: "")
            inputUnit.setText(record.unit ?: "kg")
            inputCost.setText(record.cost?.toString() ?: "")
            inputTime.setText(record.feedingTime ?: "")
            inputNotes.setText(record.notes ?: "")
        }

        val dialogTitle = if (record == null) "Record Feeding" else "Edit Feeding"
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val penIndex = spinner.selectedItemPosition
                val penId = pens.getOrNull(penIndex)?.id
                val feedType = inputType.text.toString().trim()
                val qty = inputQty.text.toString().trim().toDoubleOrNull()
                val cost = inputCost.text.toString().trim().toDoubleOrNull()

                if (penId == null) {
                    message?.text = "Select a pen"
                    return@setOnClickListener
                }
                if (feedType.isBlank()) {
                    inputType.error = "Feed type is required"
                    return@setOnClickListener
                }
                if (qty == null || qty <= 0) {
                    inputQty.error = "Quantity must be greater than zero"
                    return@setOnClickListener
                }

                val token = AuthStore.getToken(requireContext())
                if (token.isBlank()) {
                    message?.text = "Not logged in"
                    return@setOnClickListener
                }

                if (record == null) {
                    val request = CreateFeedingRequest(
                        penId,
                        feedType,
                        qty,
                        inputUnit.text.toString().trim().ifBlank { null },
                        cost,
                        inputTime.text.toString().trim().ifBlank { null },
                        inputNotes.text.toString().trim().ifBlank { null }
                    )

                    ApiClient.service.createFeeding("Bearer $token", request)
                        .enqueue(object : Callback<ApiResponse<FeedingRecord>> {
                            override fun onResponse(
                                call: Call<ApiResponse<FeedingRecord>>,
                                response: Response<ApiResponse<FeedingRecord>>
                            ) {
                                val body = response.body()
                                if (!response.isSuccessful || body?.success != true) {
                                    message?.text = body?.message ?: "Unable to save feeding"
                                    return
                                }
                                dialog.dismiss()
                                loadFeedings(token)
                            }

                            override fun onFailure(call: Call<ApiResponse<FeedingRecord>>, t: Throwable) {
                                message?.text = "Network error"
                            }
                        })
                } else {
                    val request = UpdateFeedingRequest(
                        penId,
                        feedType,
                        qty,
                        inputUnit.text.toString().trim().ifBlank { null },
                        cost,
                        inputTime.text.toString().trim().ifBlank { null },
                        inputNotes.text.toString().trim().ifBlank { null }
                    )

                    ApiClient.service.updateFeeding("Bearer $token", record.id ?: 0, request)
                        .enqueue(object : Callback<ApiResponse<FeedingRecord>> {
                            override fun onResponse(
                                call: Call<ApiResponse<FeedingRecord>>,
                                response: Response<ApiResponse<FeedingRecord>>
                            ) {
                                val body = response.body()
                                if (!response.isSuccessful || body?.success != true) {
                                    message?.text = body?.message ?: "Unable to update feeding"
                                    return
                                }
                                dialog.dismiss()
                                loadFeedings(token)
                            }

                            override fun onFailure(call: Call<ApiResponse<FeedingRecord>>, t: Throwable) {
                                message?.text = "Network error"
                            }
                        })
                }
            }
        }

        dialog.show()
    }

    private fun deleteFeeding(record: FeedingRecord) {
        val message = view?.findViewById<TextView>(R.id.feedingMessage) ?: return
        val token = AuthStore.getToken(requireContext())
        if (token.isBlank()) {
            message.text = "Not logged in"
            return
        }

        ApiClient.service.deleteFeeding("Bearer $token", record.id ?: 0)
            .enqueue(object : Callback<ApiResponse<Map<String, Long>>> {
                override fun onResponse(
                    call: Call<ApiResponse<Map<String, Long>>>,
                    response: Response<ApiResponse<Map<String, Long>>>
                ) {
                    val body = response.body()
                    if (!response.isSuccessful || body?.success != true) {
                        message.text = body?.message ?: "Unable to delete feeding"
                        return
                    }
                    loadFeedings(token)
                }

                override fun onFailure(call: Call<ApiResponse<Map<String, Long>>>, t: Throwable) {
                    message.text = "Network error"
                }
            })
    }
}
