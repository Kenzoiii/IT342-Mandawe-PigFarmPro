package com.it342.g3.mobile.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.it342.g3.mobile.R
import com.it342.g3.mobile.api.ApiClient
import com.it342.g3.mobile.api.MessageResponse
import com.it342.g3.mobile.auth.AuthStore
import com.it342.g3.mobile.ui.fragments.DashboardFragment
import com.it342.g3.mobile.ui.fragments.FeedingFragment
import com.it342.g3.mobile.ui.fragments.HealthRecordsFragment
import com.it342.g3.mobile.ui.fragments.MortalityFragment
import com.it342.g3.mobile.ui.fragments.PensFragment
import com.it342.g3.mobile.ui.fragments.SalesFragment
import com.it342.g3.mobile.ui.fragments.SettingsFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AuthStore.getToken(this).isBlank()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_dashboard)

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navView)
        toolbar = findViewById(R.id.topAppBar)

        toolbar.setNavigationIcon(android.R.drawable.ic_menu_sort_by_size)
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val header = navView.getHeaderView(0)
        val userNameView = header.findViewById<android.widget.TextView>(R.id.navUser)
        userNameView.text = AuthStore.getDisplayName(this)

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> openFragment(DashboardFragment(), "Dashboard")
                R.id.nav_pens -> openFragment(PensFragment(), "Pens")
                R.id.nav_feeding -> openFragment(FeedingFragment(), "Feeding")
                R.id.nav_health -> openFragment(HealthRecordsFragment(), "Health Records")
                R.id.nav_sales -> openFragment(SalesFragment(), "Sales")
                R.id.nav_mortality -> openFragment(MortalityFragment(), "Mortality")
                R.id.nav_settings -> openFragment(SettingsFragment(), "Settings")
                R.id.nav_logout -> handleLogout()
            }
            drawerLayout.closeDrawers()
            true
        }

        if (savedInstanceState == null) {
            navView.setCheckedItem(R.id.nav_dashboard)
            openFragment(DashboardFragment(), "Dashboard")
        }
    }

    private fun openFragment(fragment: androidx.fragment.app.Fragment, title: String) {
        toolbar.title = title
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun handleLogout() {
        val token = AuthStore.getToken(this)
        if (token.isBlank()) {
            AuthStore.clear(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        ApiClient.service.logout("Bearer $token").enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                AuthStore.clear(this@DashboardActivity)
                startActivity(Intent(this@DashboardActivity, LoginActivity::class.java))
                finish()
            }

            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                AuthStore.clear(this@DashboardActivity)
                startActivity(Intent(this@DashboardActivity, LoginActivity::class.java))
                finish()
            }
        })
    }
}
