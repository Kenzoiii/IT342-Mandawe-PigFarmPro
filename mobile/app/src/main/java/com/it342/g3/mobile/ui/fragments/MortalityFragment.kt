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
import com.it342.g3.mobile.api.CreateMortalityRecordRequest
import com.it342.g3.mobile.api.MortalityRecord
import com.it342.g3.mobile.api.PigSummary
import com.it342.g3.mobile.auth.AuthStore
import com.it342.g3.mobile.util.UiFormat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MortalityFragment : Fragment(R.layout.fragment_mortality) {
    private var pigs: List<PigSummary> = emptyList()
    private var records: List<MortalityRecord> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.btnAddMortality).setOnClickListener { openMortalityDialog() }
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val message = view?.findViewById<TextView>(R.id.mortalityMessage) ?: return
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
        val message = view?.findViewById<TextView>(R.id.mortalityMessage) ?: return
        val container = view?.findViewById<LinearLayout>(R.id.mortalityContainer) ?: return

        ApiClient.service.getMortality("Bearer $token")
            .enqueue(object : Callback<ApiResponse<List<MortalityRecord>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<MortalityRecord>>>,
                    response: Response<ApiResponse<List<MortalityRecord>>>
                ) {
                    val body = response.body()
                    if (!response.isSuccessful || body?.success != true) {
                        message.text = body?.message ?: "Unable to load mortality records"
                        return
                    }
                    records = body.data ?: emptyList()
                    renderRecords(container)
                }

                override fun onFailure(call: Call<ApiResponse<List<MortalityRecord>>>, t: Throwable) {
                    message.text = "Network error"
                }
            })
    }

    private fun renderRecords(container: LinearLayout) {
        container.removeAllViews()
        if (records.isEmpty()) {
            val emptyView = TextView(requireContext())
            emptyView.text = "No mortality records yet"
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
            subtitle.text = record.causeOfDeath ?: "Cause not specified"
            meta.text = "Date ${UiFormat.displayDate(record.dateOfDeath)}"
            status.text = record.recordedBy ?: "Recorded"

            container.addView(card)
            val params = card.layoutParams as LinearLayout.LayoutParams
            params.bottomMargin = resources.getDimensionPixelSize(R.dimen.list_gap)
            card.layoutParams = params
        }
    }

    private fun openMortalityDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_mortality_form, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerMortalityPig)
        val inputDate = dialogView.findViewById<EditText>(R.id.inputDeathDate)
        val inputAge = dialogView.findViewById<EditText>(R.id.inputDeathAge)
        val inputCause = dialogView.findViewById<EditText>(R.id.inputDeathCause)
        val inputWeight = dialogView.findViewById<EditText>(R.id.inputDeathWeight)
        val inputSymptoms = dialogView.findViewById<EditText>(R.id.inputDeathSymptoms)
        val inputActions = dialogView.findViewById<EditText>(R.id.inputDeathActions)
        val inputNotes = dialogView.findViewById<EditText>(R.id.inputDeathNotes)
        val message = view?.findViewById<TextView>(R.id.mortalityMessage)

        val labels = pigs.map { it.identifier ?: "Pig" }
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, labels)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Record Mortality")
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

                val age = inputAge.text.toString().trim().toIntOrNull()
                val weight = inputWeight.text.toString().trim().toDoubleOrNull()

                val token = AuthStore.getToken(requireContext())
                if (token.isBlank()) {
                    message?.text = "Not logged in"
                    return@setOnClickListener
                }

                val request = CreateMortalityRecordRequest(
                    pigId,
                    null,
                    inputDate.text.toString().trim().ifBlank { null },
                    age,
                    inputCause.text.toString().trim().ifBlank { null },
                    weight,
                    inputSymptoms.text.toString().trim().ifBlank { null },
                    inputActions.text.toString().trim().ifBlank { null },
                    inputNotes.text.toString().trim().ifBlank { null }
                )

                ApiClient.service.createMortality("Bearer $token", request)
                    .enqueue(object : Callback<ApiResponse<MortalityRecord>> {
                        override fun onResponse(
                            call: Call<ApiResponse<MortalityRecord>>,
                            response: Response<ApiResponse<MortalityRecord>>
                        ) {
                            val body = response.body()
                            if (!response.isSuccessful || body?.success != true) {
                                message?.text = body?.message ?: "Unable to save record"
                                return
                            }
                            dialog.dismiss()
                            loadRecords(token)
                        }

                        override fun onFailure(call: Call<ApiResponse<MortalityRecord>>, t: Throwable) {
                            message?.text = "Network error"
                        }
                    })
            }
        }

        dialog.show()
    }
}
