package com.wiklosoft.esk8logger

import android.app.Application
import com.polidea.rxandroidble2.RxBleClient


class App : Application() {
    lateinit var bleClient: BleClient

    override fun onCreate() {
        super.onCreate()
        bleClient = BleClient(applicationContext)
    }
}