package com.it342.g3.mobile.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.it342.g3.mobile.R
import com.it342.g3.mobile.api.ApiClient
import com.it342.g3.mobile.api.ApiResponse
import com.it342.g3.mobile.api.DashboardActivityItem
import com.it342.g3.mobile.api.DashboardMetrics
import com.it342.g3.mobile.api.DashboardPayload
import com.it342.g3.mobile.auth.AuthStore
import java.text.NumberFormat
import java.util.Locale
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val title = view.findViewById<TextView>(R.id.dashboardTitle)
        val subtitle = view.findViewById<TextView>(R.id.dashboardSubtitle)
        val message = view.findViewById<TextView>(R.id.dashboardMessage)
        val kpiContainer = view.findViewById<LinearLayout>(R.id.kpiContainer)
        val activityContainer = view.findViewById<LinearLayout>(R.id.activityContainer)

        title.text = "Dashboard Overview"
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
                val body = response.body()
                if (!response.isSuccessful || body?.success != true || body.data == null) {
                    message.text = body?.message ?: "Unable to load dashboard"
                    return
                }

                val data = body.data
                val displayName = data.profile?.fullName ?: data.profile?.username ?: "Farm Manager"
                subtitle.text = "Welcome back, $displayName"

                kpiContainer.removeAllViews()
                buildKpis(data.metrics).forEach { item ->
                    val card = layoutInflater.inflate(R.layout.view_kpi_card, kpiContainer, false)
                    card.findViewById<TextView>(R.id.kpiLabel).text = item.label
                    card.findViewById<TextView>(R.id.kpiValue).text = item.value
                    card.findViewById<TextView>(R.id.kpiNote).text = item.note
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.bottomMargin = resources.getDimensionPixelSize(R.dimen.list_gap)
                    kpiContainer.addView(card, params)
                }

                activityContainer.removeAllViews()
                val activities = data.activities ?: emptyList()
                if (activities.isEmpty()) {
                    val emptyView = TextView(requireContext())
                    emptyView.text = "No recent activity"
                    emptyView.setTextColor(resources.getColor(R.color.text_muted, null))
                    activityContainer.addView(emptyView)
                } else {
                    activities.forEach { activity ->
                        activityContainer.addView(buildActivityCard(activity))
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<DashboardPayload>>, t: Throwable) {
                message.text = "Network error"
            }
        })
    }

    private fun buildKpis(metrics: DashboardMetrics?): List<KpiItem> {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        val pendingValue = metrics?.pendingSalesValue ?: 0.0
        val pendingValueText = formatter.format(pendingValue)

        return listOf(
            KpiItem(
                "Total Pigs",
                (metrics?.totalPigs ?: 0).toString(),
                "+${metrics?.addedThisMonth ?: 0} this month"
            ),
            KpiItem(
                "Active Pens",
                (metrics?.activePens ?: 0).toString(),
                "${metrics?.pensAtCapacity ?: 0} at capacity"
            ),
            KpiItem(
                "Pending Sales",
                (metrics?.pendingSales ?: 0).toString(),
                pendingValueText
            ),
            KpiItem(
                "Health Alerts",
                (metrics?.healthAlerts ?: 0).toString(),
                "${metrics?.healthDueToday ?: 0} due today"
            )
        )
    }

    private fun buildActivityCard(activity: DashboardActivityItem): View {
        val card = layoutInflater.inflate(R.layout.view_record_card, null)
        val title = card.findViewById<TextView>(R.id.recordTitle)
        val subtitle = card.findViewById<TextView>(R.id.recordSubtitle)
        val meta = card.findViewById<TextView>(R.id.recordMeta)
        val status = card.findViewById<TextView>(R.id.recordStatus)

        title.text = activity.title ?: "Activity"
        subtitle.text = activity.timeAgo ?: ""
        meta.text = ""
        status.text = activity.tone ?: "Update"
        return card
    }

    private data class KpiItem(
        val label: String,
        val value: String,
        val note: String
    )
}
