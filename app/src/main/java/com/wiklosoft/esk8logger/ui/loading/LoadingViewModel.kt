package com.wiklosoft.esk8logger.ui.loading

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.polidea.rxandroidble2.RxBleConnection
import com.wiklosoft.esk8logger.App
import com.wiklosoft.esk8logger.ConnectionState
import com.wiklosoft.esk8logger.Esk8palState

class LoadingViewModel : AndroidViewModel {
    val connectionState = MutableLiveData<ConnectionState>()
    val client = getApplication<App>().bleClient

    var voltage = MutableLiveData<Float>()
    var current = MutableLiveData<Float>()
    var state = MutableLiveData<Esk8palState>()

    constructor(application: Application) : super(application) {

        client.connectionState.subscribe {
            connectionState.postValue(it)
        }

        client.advVoltage.subscribe {
            voltage.postValue(it)
        }

        client.advCurrent.subscribe {
            current.postValue(it)
        }

        client.advState.subscribe {
            state.postValue(it)
        }

    }

    fun connect() {
        client.connect()
    }
}
