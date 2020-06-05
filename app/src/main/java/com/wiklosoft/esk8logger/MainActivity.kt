package com.wiklosoft.esk8logger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.disposables.Disposable


class MainActivity() : AppCompatActivity() {
    private lateinit var connectionSub: Disposable;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_settings))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        connectionSub = (application as App).bleClient.connectionState.subscribe {
            Log.d("LoadingFragment", "connection status  ${it.toString()}")
            if (it == ConnectionState.DISCONNECTED) {
                goToLoadingScreen()
            }
        }
    }

    fun goToLoadingScreen() {
        val intent = Intent(this, OnBoardingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    override fun onPause() {
        connectionSub.dispose()

        with((application as App).bleClient) {
            if (state.value == Esk8palState.PARKED) {
                disconnect()
                goToLoadingScreen()
            }
        }
        super.onPause()
    }
}
