package com.wiklosoft.esk8logger.ui.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.wiklosoft.esk8logger.App
import com.wiklosoft.esk8logger.BleClient
import java.nio.ByteBuffer
import java.nio.ByteOrder


class SettingsViewModel : AndroidViewModel {
    val TAG = "SettingsViewModel"

    var bleClient: BleClient = getApplication<App>().bleClient

    var storageInfo = MutableLiveData<String>()
    var wifiSsid = MutableLiveData<String>()
    var wifiPass = MutableLiveData<String>()
    var wifiState = MutableLiveData<Boolean>()
    var startRideManually = MutableLiveData<Boolean>()

    constructor(application: Application) : super(application) {
        getStorageInfo()
        getWifiPass()
        updateWifiSSID()
        getWifiState()
        getManualRideStart()
    }

    private fun updateWifiSSID() {
        bleClient.getWifiSSID()?.subscribe({ data ->
            wifiSsid.postValue(String(data.filter({ it.toInt() != 0}).toByteArray()))
        }, {
            Log.e(TAG, it.message);
        })
    }

    fun setWifiSSID(ssid: String) {
        bleClient.setWifiSSID(ssid)?.subscribe({

        }, {
            Log.e(TAG, it.message)
        })
    }

    private fun getWifiPass() {
        bleClient.getWifiPass()?.subscribe({ data ->
            wifiPass.postValue(String(data.filter({ it.toInt() != 0}).toByteArray()))
        }, {
            Log.e(TAG, it.message);
        })
    }

    fun setWifiPass(password: String) {
        bleClient.setWifiPass(password)?.subscribe({

        }, {
            Log.e(TAG, it.message)
        })
    }

    private fun getWifiState() {
        bleClient.getWifiState()?.subscribe({ data ->
            wifiState.postValue(data[0].toInt() == 1)
        }, {
            Log.e(TAG, it.message);
        })
    }

    fun setWifiState(value: Byte) {
        bleClient.setWifiState(value)?.subscribe({

        }, {
            Log.e(TAG, it.message)
        })
    }

    fun setManualRideStart(value: Byte) {
        bleClient.setManualRideStart(value)?.subscribe({

        }, {
            Log.e(TAG, it.message)
        })
    }

    private fun getManualRideStart() {
        bleClient.getManualRideStart()?.subscribe({ data ->
            startRideManually.postValue(data[0].toInt() == 1)
        }, {
            Log.e(TAG, it.message);
        })
    }

    private fun getStorageInfo() { //TODO refactor it
        var freeStorage = 0
        var totalStorage = 0
        bleClient.getFreeStorage()?.subscribe({ data ->
            freeStorage = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).int
            storageInfo.postValue("%d/%d MB".format(freeStorage, totalStorage))
        }, {
            Log.e(TAG, it.message);
        })
        bleClient.getTotalStorage()?.subscribe({ data ->
            totalStorage = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).int
            storageInfo.postValue("%d/%d MB".format(freeStorage, totalStorage))
        }, {
            Log.e(TAG, it.message)
        })
    }
}