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
    var tripDistance = MutableLiveData<Double>()
    var latitude = MutableLiveData<Double>()
    var longitude = MutableLiveData<Double>()
    var altitude = MutableLiveData<Double>()

    var state = MutableLiveData<Esk8palState>()

    var gpsSatelliteCount = MutableLiveData<Int>()
    var gpsFixStatus = MutableLiveData<Int>()

    var ridingTime = MutableLiveData<Int>()

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

        bleClient.tripDistance.subscribe {
            tripDistance.postValue(it)
        }

        bleClient.state.subscribe {
            state.postValue(it)
        }

        bleClient.gpsFixStatus.subscribe {
            gpsFixStatus.postValue(it.toInt())
        }

        bleClient.gpsSatelliteCount.subscribe {
            gpsSatelliteCount.postValue(it.toInt())
        }

        bleClient.altitude.subscribe{
            altitude.postValue(it)
        }

        bleClient.ridingTime.subscribe {
            ridingTime.postValue(it)
        }
    }

    fun setState(value: Esk8palState) {
        Log.i(TAG, "setState $value");
        bleClient.setState(value.value)?.subscribe({

        }, {
            Log.e(TAG, it.message)
        })
    }
}