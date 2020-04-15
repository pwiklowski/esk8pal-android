package com.wiklosoft.esk8logger.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.polidea.rxandroidble2.RxBleConnection
import com.wiklosoft.esk8logger.App
import com.wiklosoft.esk8logger.BleClient
import com.wiklosoft.esk8logger.Esk8palState
import kotlinx.coroutines.suspendCancellableCoroutine
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val TAG = "HomeViewModel"

class HomeViewModel : AndroidViewModel {
    var bleClient: BleClient = getApplication<App>().bleClient
    var connectionState = MutableLiveData<RxBleConnection.RxBleConnectionState>()

    var voltage = MutableLiveData<Double>()
    var current = MutableLiveData<Double>()
    var usedEnergy = MutableLiveData<Double>()
    var totalEnergy = MutableLiveData<Double>()

    var speed = MutableLiveData<Double>()
    var latitude = MutableLiveData<Double>()
    var longitude = MutableLiveData<Double>()

    var state = MutableLiveData<Esk8palState>()

    constructor(application: Application) : super(application) {
        bleClient.usedEnergy.subscribe {
            usedEnergy.postValue(it)
        }

        bleClient.voltage.subscribe {
            voltage.postValue(it)
        }

        bleClient.current.subscribe {
            current.postValue(it)
        }

        bleClient.totalEnergy.subscribe {
            totalEnergy.postValue(it)
        }

        bleClient.latitude.subscribe {
            latitude.postValue(it)
        }

        bleClient.longitude.subscribe {
            longitude.postValue(it)
        }

        bleClient.speed.subscribe {
            speed.postValue(it)
        }

        bleClient.state.subscribe {
            state.postValue(it)
        }
    }

    fun setState(value: Esk8palState) {
        Log.i(TAG, "setState $value");
        bleClient.setState(value.value)?.subscribe({

        }, {
            Log.e(TAG, it.message)
        })
    }

    private fun processData(data: ByteArray) : Double {
        return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).double
    }

    override fun onCleared() {
        super.onCleared()
    }
}