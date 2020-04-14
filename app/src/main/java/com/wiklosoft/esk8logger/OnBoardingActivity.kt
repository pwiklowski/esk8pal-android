package com.wiklosoft.esk8logger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.wiklosoft.esk8logger.ui.loading.LoadingFragment

class OnBoardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.on_boarding_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, LoadingFragment.newInstance())
                .commitNow()
        }
    }
}