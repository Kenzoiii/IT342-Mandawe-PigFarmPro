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
import com.it342.g3.mobile.api.CreateSaleRequest
import com.it342.g3.mobile.api.PigSummary
import com.it342.g3.mobile.api.SaleRecord
import com.it342.g3.mobile.api.UpdateSaleRequest
import com.it342.g3.mobile.auth.AuthStore
import com.it342.g3.mobile.util.UiFormat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SalesFragment : Fragment(R.layout.fragment_sales) {
    private var pigs: List<PigSummary> = emptyList()
    private var sales: List<SaleRecord> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.btnAddSale).setOnClickListener { openSaleDialog(null) }
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val message = view?.findViewById<TextView>(R.id.salesMessage) ?: return
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
                loadSales(token)
            }

            override fun onFailure(call: Call<ApiResponse<List<PigSummary>>>, t: Throwable) {
                message.text = "Unable to load pigs"
            }
        })
    }

    private fun loadSales(token: String) {
        val message = view?.findViewById<TextView>(R.id.salesMessage) ?: return
        val container = view?.findViewById<LinearLayout>(R.id.salesContainer) ?: return

        ApiClient.service.getSales("Bearer $token")
            .enqueue(object : Callback<ApiResponse<List<SaleRecord>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<SaleRecord>>>,
                    response: Response<ApiResponse<List<SaleRecord>>>
                ) {
                    val body = response.body()
                    if (!response.isSuccessful || body?.success != true) {
                        message.text = body?.message ?: "Unable to load sales"
                        return
                    }
                    sales = body.data ?: emptyList()
                    renderSales(container)
                }

                override fun onFailure(call: Call<ApiResponse<List<SaleRecord>>>, t: Throwable) {
                    message.text = "Network error"
                }
            })
    }

    private fun renderSales(container: LinearLayout) {
        container.removeAllViews()
        if (sales.isEmpty()) {
            val emptyView = TextView(requireContext())
            emptyView.text = "No sales yet"
            emptyView.setTextColor(resources.getColor(R.color.text_muted, null))
            container.addView(emptyView)
            return
        }

        sales.forEach { sale ->
            val card = layoutInflater.inflate(R.layout.view_record_card, container, false)
            val title = card.findViewById<TextView>(R.id.recordTitle)
            val subtitle = card.findViewById<TextView>(R.id.recordSubtitle)
            val meta = card.findViewById<TextView>(R.id.recordMeta)
            val status = card.findViewById<TextView>(R.id.recordStatus)
            val primary = card.findViewById<Button>(R.id.btnPrimary)

            title.text = "Pig ${sale.pigIdentifier ?: "-"}"
            subtitle.text = "Buyer: ${sale.buyerName ?: "-"}"
            meta.text = "Sale ${UiFormat.displayDate(sale.saleDate)} | ${sale.salePrice ?: 0.0}"
            status.text = sale.status ?: "Pending"

            primary.visibility = View.VISIBLE
            primary.text = "Edit"
            primary.setOnClickListener { openSaleDialog(sale) }

            container.addView(card)
            val params = card.layoutParams as LinearLayout.LayoutParams
            params.bottomMargin = resources.getDimensionPixelSize(R.dimen.list_gap)
            card.layoutParams = params
        }
    }

    private fun openSaleDialog(sale: SaleRecord?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_sale_form, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerSalePig)
        val inputBuyer = dialogView.findViewById<EditText>(R.id.inputSaleBuyer)
        val inputContact = dialogView.findViewById<EditText>(R.id.inputSaleContact)
        val inputPrice = dialogView.findViewById<EditText>(R.id.inputSalePrice)
        val inputDate = dialogView.findViewById<EditText>(R.id.inputSaleDate)
        val inputExpected = dialogView.findViewById<EditText>(R.id.inputSaleExpected)
        val inputActual = dialogView.findViewById<EditText>(R.id.inputSaleActual)
        val inputStatus = dialogView.findViewById<EditText>(R.id.inputSaleStatus)
        val inputPayment = dialogView.findViewById<EditText>(R.id.inputSalePayment)
        val inputNotes = dialogView.findViewById<EditText>(R.id.inputSaleNotes)
        val message = view?.findViewById<TextView>(R.id.salesMessage)

        val labels = pigs.map { it.identifier ?: "Pig" }
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, labels)

        if (sale != null) {
            val index = pigs.indexOfFirst { it.id == sale.pigId }
            if (index >= 0) {
                spinner.setSelection(index)
            }
            inputBuyer.setText(sale.buyerName ?: "")
            inputContact.setText(sale.buyerContact ?: "")
            inputPrice.setText(sale.salePrice?.toString() ?: "")
            inputDate.setText(sale.saleDate ?: "")
            inputExpected.setText(sale.expectedPickupDate ?: "")
            inputActual.setText(sale.actualPickupDate ?: "")
            inputStatus.setText(sale.status ?: "")
            inputPayment.setText(sale.paymentStatus ?: "")
            inputNotes.setText(sale.notes ?: "")
        }

        val dialogTitle = if (sale == null) "Record Sale" else "Update Sale"
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val pigIndex = spinner.selectedItemPosition
                val pigId = pigs.getOrNull(pigIndex)?.id
                val buyer = inputBuyer.text.toString().trim()
                val price = inputPrice.text.toString().trim().toDoubleOrNull()

                if (sale == null && pigId == null) {
                    message?.text = "Select a pig"
                    return@setOnClickListener
                }
                if (buyer.isBlank()) {
                    inputBuyer.error = "Buyer name is required"
                    return@setOnClickListener
                }
                if (price == null || price <= 0) {
                    inputPrice.error = "Sale price must be greater than zero"
                    return@setOnClickListener
                }

                val token = AuthStore.getToken(requireContext())
                if (token.isBlank()) {
                    message?.text = "Not logged in"
                    return@setOnClickListener
                }

                if (sale == null) {
                    val request = CreateSaleRequest(
                        pigId,
                        null,
                        buyer,
                        inputContact.text.toString().trim().ifBlank { null },
                        price,
                        inputDate.text.toString().trim().ifBlank { null },
                        inputExpected.text.toString().trim().ifBlank { null },
                        inputActual.text.toString().trim().ifBlank { null },
                        inputStatus.text.toString().trim().ifBlank { null },
                        inputPayment.text.toString().trim().ifBlank { null },
                        inputNotes.text.toString().trim().ifBlank { null }
                    )

                    ApiClient.service.createSale("Bearer $token", request)
                        .enqueue(object : Callback<ApiResponse<SaleRecord>> {
                            override fun onResponse(
                                call: Call<ApiResponse<SaleRecord>>,
                                response: Response<ApiResponse<SaleRecord>>
                            ) {
                                val body = response.body()
                                if (!response.isSuccessful || body?.success != true) {
                                    message?.text = body?.message ?: "Unable to save sale"
                                    return
                                }
                                dialog.dismiss()
                                loadSales(token)
                            }

                            override fun onFailure(call: Call<ApiResponse<SaleRecord>>, t: Throwable) {
                                message?.text = "Network error"
                            }
                        })
                } else {
                    val request = UpdateSaleRequest(
                        buyer,
                        inputContact.text.toString().trim().ifBlank { null },
                        price,
                        inputDate.text.toString().trim().ifBlank { null },
                        inputExpected.text.toString().trim().ifBlank { null },
                        inputActual.text.toString().trim().ifBlank { null },
                        inputStatus.text.toString().trim().ifBlank { null },
                        inputPayment.text.toString().trim().ifBlank { null },
                        inputNotes.text.toString().trim().ifBlank { null }
                    )

                    ApiClient.service.updateSale("Bearer $token", sale.id ?: 0, request)
                        .enqueue(object : Callback<ApiResponse<SaleRecord>> {
                            override fun onResponse(
                                call: Call<ApiResponse<SaleRecord>>,
                                response: Response<ApiResponse<SaleRecord>>
                            ) {
                                val body = response.body()
                                if (!response.isSuccessful || body?.success != true) {
                                    message?.text = body?.message ?: "Unable to update sale"
                                    return
                                }
                                dialog.dismiss()
                                loadSales(token)
                            }

                            override fun onFailure(call: Call<ApiResponse<SaleRecord>>, t: Throwable) {
                                message?.text = "Network error"
                            }
                        })
                }
            }
        }

        dialog.show()
    }
}
