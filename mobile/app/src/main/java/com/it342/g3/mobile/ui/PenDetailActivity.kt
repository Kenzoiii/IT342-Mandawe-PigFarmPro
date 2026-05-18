package com.it342.g3.mobile.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.it342.g3.mobile.R
import com.it342.g3.mobile.api.ApiClient
import com.it342.g3.mobile.api.ApiResponse
import com.it342.g3.mobile.api.CreatePigRequest
import com.it342.g3.mobile.api.PenDetailsPayload
import com.it342.g3.mobile.api.PenSummary
import com.it342.g3.mobile.api.PigSummary
import com.it342.g3.mobile.api.UpdatePenRequest
import com.it342.g3.mobile.api.UpdatePigRequest
import com.it342.g3.mobile.auth.AuthStore
import com.it342.g3.mobile.util.UiFormat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PenDetailActivity : AppCompatActivity() {
    private var penId: Long = 0
    private var pen: PenSummary? = null
    private var pigs: List<PigSummary> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pen_detail)

        penId = intent.getLongExtra(EXTRA_PEN_ID, 0)
        if (penId == 0L) {
            finish()
            return
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.penToolbar)
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert)
        toolbar.setNavigationOnClickListener { finish() }

        findViewById<Button>(R.id.btnEditPen).setOnClickListener { openPenDialog(pen) }
        findViewById<Button>(R.id.btnAddPig).setOnClickListener { openPigDialog(null) }

        loadDetails()
    }

    private fun loadDetails() {
        val message = findViewById<TextView>(R.id.penMessage)
        message.text = ""

        val token = AuthStore.getToken(this)
        if (token.isBlank()) {
            message.text = "Not logged in"
            return
        }

        ApiClient.service.getPenDetails("Bearer $token", penId)
            .enqueue(object : Callback<ApiResponse<PenDetailsPayload>> {
                override fun onResponse(
                    call: Call<ApiResponse<PenDetailsPayload>>,
                    response: Response<ApiResponse<PenDetailsPayload>>
                ) {
                    val body = response.body()
                    if (!response.isSuccessful || body?.success != true || body.data == null) {
                        message.text = body?.message ?: "Unable to load pen"
                        return
                    }

                    pen = body.data.pen
                    pigs = body.data.pigs ?: emptyList()
                    renderPen()
                    renderPigs()
                }

                override fun onFailure(call: Call<ApiResponse<PenDetailsPayload>>, t: Throwable) {
                    message.text = "Network error"
                }
            })
    }

    private fun renderPen() {
        val title = findViewById<TextView>(R.id.penTitle)
        val subtitle = findViewById<TextView>(R.id.penSubtitle)
        val stats = findViewById<TextView>(R.id.penStats)

        title.text = pen?.name ?: pen?.identifier ?: "Pen"
        subtitle.text = pen?.identifier ?: ""

        val capacity = pen?.capacity ?: 0
        val occupied = pen?.occupied ?: 0
        val available = pen?.available ?: (capacity - occupied)
        val utilization = pen?.utilization ?: 0.0

        stats.text = "Capacity $occupied / $capacity | Available $available | Utilization ${utilization.toInt()}%"
    }

    private fun renderPigs() {
        val container = findViewById<LinearLayout>(R.id.pigContainer)
        container.removeAllViews()

        if (pigs.isEmpty()) {
            val emptyView = TextView(this)
            emptyView.text = "No pigs yet"
            emptyView.setTextColor(resources.getColor(R.color.text_muted, null))
            container.addView(emptyView)
            return
        }

        pigs.forEach { pig ->
            val card = layoutInflater.inflate(R.layout.view_record_card, container, false)
            val title = card.findViewById<TextView>(R.id.recordTitle)
            val subtitle = card.findViewById<TextView>(R.id.recordSubtitle)
            val meta = card.findViewById<TextView>(R.id.recordMeta)
            val status = card.findViewById<TextView>(R.id.recordStatus)
            val primary = card.findViewById<Button>(R.id.btnPrimary)
            val secondary = card.findViewById<Button>(R.id.btnSecondary)

            title.text = pig.identifier ?: "Pig"
            subtitle.text = pig.breed ?: ""
            val weightText = if (pig.weight != null) "${pig.weight} ${pig.weightUnit ?: "kg"}" else "No weight"
            meta.text = "Added ${UiFormat.displayDateTime(pig.addedAt)} | $weightText"
            status.text = pig.status ?: "Active"

            primary.visibility = View.VISIBLE
            primary.text = "Edit"
            primary.setOnClickListener { openPigDialog(pig) }

            secondary.visibility = View.VISIBLE
            secondary.text = "Delete"
            secondary.setOnClickListener { deletePig(pig) }

            container.addView(card)
            val params = card.layoutParams as LinearLayout.LayoutParams
            params.bottomMargin = resources.getDimensionPixelSize(R.dimen.list_gap)
            card.layoutParams = params
        }
    }

    private fun openPenDialog(current: PenSummary?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_pen_form, null)
        val inputIdentifier = dialogView.findViewById<EditText>(R.id.inputPenIdentifier)
        val inputName = dialogView.findViewById<EditText>(R.id.inputPenName)
        val inputCapacity = dialogView.findViewById<EditText>(R.id.inputPenCapacity)
        val inputDescription = dialogView.findViewById<EditText>(R.id.inputPenDescription)

        inputIdentifier.setText(current?.identifier ?: "")
        inputName.setText(current?.name ?: "")
        inputCapacity.setText(current?.capacity?.toString() ?: "")
        inputDescription.setText(current?.description ?: "")

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Pen")
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

                val token = AuthStore.getToken(this)
                if (token.isBlank()) {
                    return@setOnClickListener
                }

                val request = UpdatePenRequest(
                    inputIdentifier.text.toString().trim().ifBlank { null },
                    name,
                    inputDescription.text.toString().trim().ifBlank { null },
                    capacity
                )

                ApiClient.service.updatePen("Bearer $token", penId, request)
                    .enqueue(object : Callback<ApiResponse<PenSummary>> {
                        override fun onResponse(
                            call: Call<ApiResponse<PenSummary>>,
                            response: Response<ApiResponse<PenSummary>>
                        ) {
                            val body = response.body()
                            if (!response.isSuccessful || body?.success != true) {
                                return
                            }
                            dialog.dismiss()
                            loadDetails()
                        }

                        override fun onFailure(call: Call<ApiResponse<PenSummary>>, t: Throwable) {
                        }
                    })
            }
        }

        dialog.show()
    }

    private fun openPigDialog(pig: PigSummary?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_pig_form, null)
        val inputIdentifier = dialogView.findViewById<EditText>(R.id.inputPigIdentifier)
        val inputBreed = dialogView.findViewById<EditText>(R.id.inputPigBreed)
        val inputGender = dialogView.findViewById<EditText>(R.id.inputPigGender)
        val inputBirthdate = dialogView.findViewById<EditText>(R.id.inputPigBirthdate)
        val inputWeight = dialogView.findViewById<EditText>(R.id.inputPigWeight)
        val inputWeightUnit = dialogView.findViewById<EditText>(R.id.inputPigWeightUnit)
        val inputStatus = dialogView.findViewById<EditText>(R.id.inputPigStatus)
        val inputNotes = dialogView.findViewById<EditText>(R.id.inputPigNotes)

        if (pig != null) {
            inputIdentifier.setText(pig.identifier ?: "")
            inputBreed.setText(pig.breed ?: "")
            inputGender.setText(pig.gender ?: "")
            inputBirthdate.setText(pig.birthdate ?: "")
            inputWeight.setText(pig.weight?.toString() ?: "")
            inputWeightUnit.setText(pig.weightUnit ?: "kg")
            inputStatus.setText(pig.status ?: "Active")
            inputNotes.setText(pig.notes ?: "")
        }

        val dialogTitle = if (pig == null) "Add Pig" else "Edit Pig"
        val dialog = AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val token = AuthStore.getToken(this)
                if (token.isBlank()) {
                    return@setOnClickListener
                }

                val weight = inputWeight.text.toString().trim().toDoubleOrNull()

                if (pig == null) {
                    val request = CreatePigRequest(
                        inputIdentifier.text.toString().trim().ifBlank { null },
                        inputBreed.text.toString().trim().ifBlank { null },
                        inputGender.text.toString().trim().ifBlank { null },
                        inputBirthdate.text.toString().trim().ifBlank { null },
                        weight,
                        inputWeightUnit.text.toString().trim().ifBlank { null },
                        inputStatus.text.toString().trim().ifBlank { null },
                        inputNotes.text.toString().trim().ifBlank { null }
                    )

                    ApiClient.service.createPig("Bearer $token", penId, request)
                        .enqueue(object : Callback<ApiResponse<PigSummary>> {
                            override fun onResponse(
                                call: Call<ApiResponse<PigSummary>>,
                                response: Response<ApiResponse<PigSummary>>
                            ) {
                                val body = response.body()
                                if (!response.isSuccessful || body?.success != true) {
                                    return
                                }
                                dialog.dismiss()
                                loadDetails()
                            }

                            override fun onFailure(call: Call<ApiResponse<PigSummary>>, t: Throwable) {
                            }
                        })
                } else {
                    val request = UpdatePigRequest(
                        inputIdentifier.text.toString().trim().ifBlank { null },
                        inputBreed.text.toString().trim().ifBlank { null },
                        inputGender.text.toString().trim().ifBlank { null },
                        inputBirthdate.text.toString().trim().ifBlank { null },
                        weight,
                        inputWeightUnit.text.toString().trim().ifBlank { null },
                        inputStatus.text.toString().trim().ifBlank { null },
                        inputNotes.text.toString().trim().ifBlank { null }
                    )

                    ApiClient.service.updatePig("Bearer $token", pig.id ?: 0, request)
                        .enqueue(object : Callback<ApiResponse<PigSummary>> {
                            override fun onResponse(
                                call: Call<ApiResponse<PigSummary>>,
                                response: Response<ApiResponse<PigSummary>>
                            ) {
                                val body = response.body()
                                if (!response.isSuccessful || body?.success != true) {
                                    return
                                }
                                dialog.dismiss()
                                loadDetails()
                            }

                            override fun onFailure(call: Call<ApiResponse<PigSummary>>, t: Throwable) {
                            }
                        })
                }
            }
        }

        dialog.show()
    }

    private fun deletePig(pig: PigSummary) {
        val token = AuthStore.getToken(this)
        if (token.isBlank()) {
            return
        }

        ApiClient.service.deletePig("Bearer $token", pig.id ?: 0)
            .enqueue(object : Callback<ApiResponse<Map<String, Long>>> {
                override fun onResponse(
                    call: Call<ApiResponse<Map<String, Long>>>,
                    response: Response<ApiResponse<Map<String, Long>>>
                ) {
                    val body = response.body()
                    if (!response.isSuccessful || body?.success != true) {
                        return
                    }
                    loadDetails()
                }

                override fun onFailure(call: Call<ApiResponse<Map<String, Long>>>, t: Throwable) {
                }
            })
    }

    companion object {
        const val EXTRA_PEN_ID = "penId"
    }
}
