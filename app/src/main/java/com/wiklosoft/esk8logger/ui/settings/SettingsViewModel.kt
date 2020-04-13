package com.wiklosoft.esk8logger.ui.settings

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.polidea.rxandroidble2.RxBleConnection
import com.wiklosoft.esk8logger.App
import com.wiklosoft.esk8logger.BleClient
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*



private const val CHAR_STATE = "0000fd01-0000-1000-8000-00805f9b34fb"
private const val CHAR_MANUAL_RIDE_START = "0000fd02-0000-1000-8000-00805f9b34fb"
private const val CHAR_WIFI_SSID = "0000fd03-0000-1000-8000-00805f9b34fb"
private const val CHAR_WIFI_PASS = "0000fd04-0000-1000-8000-00805f9b34fb"
private const val CHAR_WIFI_ENABLED = "0000fd05-0000-1000-8000-00805f9b34fb"

private const val CHAR_FREE_STORAGE = "0000fd06-0000-1000-8000-00805f9b34fb"
private const val CHAR_TOTAL_STORAGE = "0000fd07-0000-1000-8000-00805f9b34fb"


class SettingsViewModel : AndroidViewModel {
    val TAG = "SettingsViewModel"

    var bleClient: BleClient = getApplication<App>().bleClient

    var storageInfo = MutableLiveData<String>()
    var wifiSsid = MutableLiveData<String>()
    var wifiPass = MutableLiveData<String>()
    var wifiState = MutableLiveData<Boolean>()
    var startRideManually = MutableLiveData<Boolean>()

    constructor(application: Application) : super(application) {
        bleClient.connection.observeForever { bleConnection ->
            getStorageInfo(bleConnection)
            getWifiPass(bleConnection)
            getWifiSsid(bleConnection)
            getWifiState(bleConnection)
            getManualRideStart(bleConnection)
        }
    }

    private fun getWifiSsid(bleConnection: RxBleConnection): MutableLiveData<String> {
        bleConnection.readCharacteristic(UUID.fromString(CHAR_WIFI_SSID)).subscribe({ data ->
            wifiSsid.postValue(String(data.filter({ it.toInt() != 0}).toByteArray()))
        }, {
            Log.e(TAG, it.message);
        })
        return wifiSsid
    }

    fun setWifiSsid(ssid: String) {
        Log.i(TAG, "setWifiSsid $ssid");
        bleClient.connection.value?.writeCharacteristic(UUID.fromString(CHAR_WIFI_SSID), ssid.toByteArray())?.subscribe({ response ->

        }, {
            Log.e(TAG, it.message)
        })
    }

    private fun getWifiPass(bleConnection: RxBleConnection): MutableLiveData<String> {
        bleConnection.readCharacteristic(UUID.fromString(CHAR_WIFI_PASS)).subscribe({ data ->
            wifiPass.postValue(String(data.filter({ it.toInt() != 0}).toByteArray()))
        }, {
            Log.e(TAG, it.message);
        })
        return wifiPass
    }

    fun setWifiPass(password: String) {
        Log.i(TAG, "setWifiPass $password");
        bleClient.connection.value?.writeCharacteristic(UUID.fromString(CHAR_WIFI_PASS), password.toByteArray())?.subscribe({ response ->

        }, {
            Log.e(TAG, it.message)
        })
    }

    private fun getWifiState(bleConnection: RxBleConnection): MutableLiveData<Boolean> {
        bleConnection.readCharacteristic(UUID.fromString(CHAR_WIFI_ENABLED)).subscribe({ data ->
            wifiState.postValue(data[0].toInt() == 1)
        }, {
            Log.e(TAG, it.message);
        })
        return wifiState
    }

    fun setWifiState(value: Byte) {
        Log.i(TAG, "setWifiState $value");
        bleClient.connection.value?.writeCharacteristic(UUID.fromString(CHAR_WIFI_ENABLED), ByteArray(1) {value})?.subscribe({

        }, {
            Log.e(TAG, it.message)
        })
    }

    fun setManualRideStart(value: Byte) {
        Log.i(TAG, "setManualRideStart $value");
        bleClient.connection.value?.writeCharacteristic(UUID.fromString(CHAR_MANUAL_RIDE_START), ByteArray(1) {value})?.subscribe({

        }, {
            Log.e(TAG, it.message)
        })
    }

    private fun getManualRideStart(bleConnection: RxBleConnection): MutableLiveData<Boolean> {
        bleConnection.readCharacteristic(UUID.fromString(CHAR_MANUAL_RIDE_START)).subscribe({ data ->
            var b = data[0].toInt();

            startRideManually.postValue(data[0].toInt() == 1)
        }, {
            Log.e(TAG, it.message);
        })
        return startRideManually
    }

    private fun getStorageInfo(bleConnection: RxBleConnection) { //TODO refactor it
        var freeStorage = 0
        var totalStorage = 0
        bleConnection.readCharacteristic(UUID.fromString(CHAR_FREE_STORAGE)).subscribe({ data ->
            freeStorage = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).int
            storageInfo.postValue("%d/%d MB".format(freeStorage, totalStorage))
        }, {
            Log.e(TAG, it.message);
        })
        bleConnection.readCharacteristic(UUID.fromString(CHAR_TOTAL_STORAGE)).subscribe({ data ->
            totalStorage = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).int
            storageInfo.postValue("%d/%d MB".format(freeStorage, totalStorage))
        }, {
            Log.e(TAG, it.message)
        })
    }
}