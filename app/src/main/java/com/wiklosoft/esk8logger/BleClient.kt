package com.wiklosoft.esk8logger

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

const val TAG = "BleClient"

class BleClient {
    var bleClient : RxBleClient
    lateinit var connectionSub: Disposable
    var connection = MutableLiveData<RxBleConnection>()
    var connectionState = MutableLiveData<RxBleConnection.RxBleConnectionState>()



    constructor(context: Context) {
        bleClient = RxBleClient.create(context)
        getDevice().observeConnectionStateChanges().subscribe {
            connectionState.postValue(it)
            if (it == RxBleConnection.RxBleConnectionState.DISCONNECTED) {
                connect()
            }
        }
        connectionState.value = getDevice().connectionState

        if (getDevice().connectionState == RxBleConnection.RxBleConnectionState.DISCONNECTED) {
            connect()
        }
    }

    private fun getDeviceMac(): String { //TODO store it somewhere, add activity when scan options
        return "30:AE:A4:4C:D2:52"
    }

    fun connect() {
        connectionSub = bleClient.getBleDevice(getDeviceMac()).establishConnection(true).subscribe({
            Log.d(TAG, "Connection");
            connection.postValue(it)
        }, {
            connection.postValue(null)
            Log.e(TAG, it.toString());
        })
    }

    fun disconnect() {
        connectionSub.dispose()
    }

    fun getDevice() : RxBleDevice {
        return bleClient.getBleDevice(getDeviceMac())
    }
}