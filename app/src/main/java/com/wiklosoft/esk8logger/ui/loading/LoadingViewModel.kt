package com.wiklosoft.esk8logger.ui.loading

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.polidea.rxandroidble2.RxBleConnection
import com.wiklosoft.esk8logger.App

class LoadingViewModel : AndroidViewModel {
    val connectionState = MutableLiveData<RxBleConnection.RxBleConnectionState>()

    constructor(application: Application) : super(application) {
        val client = getApplication<App>().bleClient

        client.getDevice().observeConnectionStateChanges().subscribe {
            connectionState.postValue(it)
        }
        connectionState.postValue(client.getDevice().connectionState)
    }
}
