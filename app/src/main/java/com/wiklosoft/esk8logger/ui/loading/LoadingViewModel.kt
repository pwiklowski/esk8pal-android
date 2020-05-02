package com.wiklosoft.esk8logger.ui.loading

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.polidea.rxandroidble2.RxBleConnection
import com.wiklosoft.esk8logger.App
import com.wiklosoft.esk8logger.ConnectionState

class LoadingViewModel : AndroidViewModel {
    val connectionState = MutableLiveData<ConnectionState>()

    constructor(application: Application) : super(application) {
        val client = getApplication<App>().bleClient

        client.connectionState.subscribe {
            connectionState.postValue(it)
        }
    }
}
