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
import com.it342.g3.mobile.api.CreateHealthRecordRequest
import com.it342.g3.mobile.api.HealthRecord
import com.it342.g3.mobile.api.PigSummary
import com.it342.g3.mobile.auth.AuthStore
import com.it342.g3.mobile.util.UiFormat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HealthRecordsFragment : Fragment(R.layout.fragment_health_records) {
    private var pigs: List<PigSummary> = emptyList()
    private var records: List<HealthRecord> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.btnAddHealth).setOnClickListener { openHealthDialog() }
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val message = view?.findViewById<TextView>(R.id.healthMessage) ?: return
        message.text = ""

        val token = AuthStore.getToken(requireContext())
        if (token.isBlank()) {
            message.text = "Not logged in"
            return
        }

        ApiClient.service.getPigs("Bearer $token").enqueue(object : Callback<ApiResponse<List<PigSummary>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<PigSummary>>>,
                response: Response<ApiResponse<List<PigSummary>>>
            ) {
                pigs = response.body()?.data ?: emptyList()
                loadRecords(token)
            }

            override fun onFailure(call: Call<ApiResponse<List<PigSummary>>>, t: Throwable) {
                message.text = "Unable to load pigs"
            }
        })
    }

    private fun loadRecords(token: String) {
        val message = view?.findViewById<TextView>(R.id.healthMessage) ?: return
        val container = view?.findViewById<LinearLayout>(R.id.healthContainer) ?: return

        ApiClient.service.getHealthRecords("Bearer $token")
            .enqueue(object : Callback<ApiResponse<List<HealthRecord>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<HealthRecord>>>,
                    response: Response<ApiResponse<List<HealthRecord>>>
                ) {
                    val body = response.body()
                    if (!response.isSuccessful || body?.success != true) {
                        message.text = body?.message ?: "Unable to load health records"
                        return
                    }
                    records = body.data ?: emptyList()
                    renderRecords(container)
                }

                override fun onFailure(call: Call<ApiResponse<List<HealthRecord>>>, t: Throwable) {
                    message.text = "Network error"
                }
            })
    }

    private fun renderRecords(container: LinearLayout) {
        container.removeAllViews()
        if (records.isEmpty()) {
            val emptyView = TextView(requireContext())
            emptyView.text = "No health records yet"
            emptyView.setTextColor(resources.getColor(R.color.text_muted, null))
            container.addView(emptyView)
            return
        }

        records.forEach { record ->
            val card = layoutInflater.inflate(R.layout.view_record_card, container, false)
            val title = card.findViewById<TextView>(R.id.recordTitle)
            val subtitle = card.findViewById<TextView>(R.id.recordSubtitle)
            val meta = card.findViewById<TextView>(R.id.recordMeta)
            val status = card.findViewById<TextView>(R.id.recordStatus)

            title.text = record.pigIdentifier ?: "Pig"
            subtitle.text = record.healthCondition ?: "Health check"
            meta.text = "Checkup ${UiFormat.displayDateTime(record.checkupDate)}"
            status.text = record.nextTreatmentType ?: "On track"

            container.addView(card)
            val params = card.layoutParams as LinearLayout.LayoutParams
            params.bottomMargin = resources.getDimensionPixelSize(R.dimen.list_gap)
            card.layoutParams = params
        }
    }

    private fun openHealthDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_health_record_form, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerPig)
        val inputWeight = dialogView.findViewById<EditText>(R.id.inputHealthWeight)
        val inputTemp = dialogView.findViewById<EditText>(R.id.inputHealthTemp)
        val inputCondition = dialogView.findViewById<EditText>(R.id.inputHealthCondition)
        val inputTreatment = dialogView.findViewById<EditText>(R.id.inputHealthTreatment)
        val inputMedication = dialogView.findViewById<EditText>(R.id.inputHealthMedication)
        val inputNextDate = dialogView.findViewById<EditText>(R.id.inputHealthNextDate)
        val inputNextType = dialogView.findViewById<EditText>(R.id.inputHealthNextType)
        val inputCheckup = dialogView.findViewById<EditText>(R.id.inputHealthCheckup)
        val inputNotes = dialogView.findViewById<EditText>(R.id.inputHealthNotes)
        val message = view?.findViewById<TextView>(R.id.healthMessage)

        val labels = pigs.map { it.identifier ?: "Pig" }
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, labels)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add Health Record")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val pigIndex = spinner.selectedItemPosition
                val pigId = pigs.getOrNull(pigIndex)?.id
                if (pigId == null) {
                    message?.text = "Select a pig"
                    return@setOnClickListener
                }

                val weight = inputWeight.text.toString().trim().toDoubleOrNull()
                val temp = inputTemp.text.toString().trim().toDoubleOrNull()

                val token = AuthStore.getToken(requireContext())
                if (token.isBlank()) {
                    message?.text = "Not logged in"
                    return@setOnClickListener
                }

                val request = CreateHealthRecordRequest(
                    pigId,
                    null,
                    weight,
                    inputCondition.text.toString().trim().ifBlank { null },
                    temp,
                    inputTreatment.text.toString().trim().ifBlank { null },
                    inputMedication.text.toString().trim().ifBlank { null },
                    inputNextDate.text.toString().trim().ifBlank { null },
                    inputNextType.text.toString().trim().ifBlank { null },
                    inputCheckup.text.toString().trim().ifBlank { null },
                    inputNotes.text.toString().trim().ifBlank { null }
                )

                ApiClient.service.createHealthRecord("Bearer $token", request)
                    .enqueue(object : Callback<ApiResponse<HealthRecord>> {
                        override fun onResponse(
                            call: Call<ApiResponse<HealthRecord>>,
                            response: Response<ApiResponse<HealthRecord>>
                        ) {
                            val body = response.body()
                            if (!response.isSuccessful || body?.success != true) {
                                message?.text = body?.message ?: "Unable to save record"
                                return
                            }
                            dialog.dismiss()
                            loadRecords(token)
                        }

                        override fun onFailure(call: Call<ApiResponse<HealthRecord>>, t: Throwable) {
                            message?.text = "Network error"
                        }
                    })
            }
        }

        dialog.show()
    }
}
