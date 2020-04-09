package com.wiklosoft.esk8logger.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.wiklosoft.esk8logger.App
import com.wiklosoft.esk8logger.BleClient
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

private const val CHAR_VOLTAGE = "0000ff01-0000-1000-8000-00805f9b34fb"
private const val CHAR_CURRENT = "0000ff02-0000-1000-8000-00805f9b34fb"
private const val CHAR_USED_ENERGY = "0000ff03-0000-1000-8000-00805f9b34fb"
private const val CHAR_TOTAL_ENERGY = "0000ff04-0000-1000-8000-00805f9b34fb"

private const val CHAR_LATITUDE = "0000fe01-0000-1000-8000-00805f9b34fb"
private const val CHAR_LONGITUDE = "0000fe02-0000-1000-8000-00805f9b34fb"
private const val CHAR_SPEED = "0000fe03-0000-1000-8000-00805f9b34fb"
private const val TAG = "HomeViewModel"

class HomeViewModel : AndroidViewModel {
    var bleClient: BleClient = getApplication<App>().bleClient
    var connectionState = bleClient.connectionState

    var voltage = MutableLiveData<Double>()
    var current = MutableLiveData<Double>()
    var usedEnergy = MutableLiveData<Double>()
    var totalEnergy = MutableLiveData<Double>()

    var speed = MutableLiveData<Double>()
    var latitude = MutableLiveData<Double>()
    var longitude = MutableLiveData<Double>()

    constructor(application: Application) : super(application) {
        bleClient.connection.observeForever {

            if (it != null) {
                it.readCharacteristic(UUID.fromString(CHAR_VOLTAGE)).subscribe({ onSuccess ->
                    voltage.postValue(processData(onSuccess))
                }, {
                    Log.e(TAG, it.message);
                })

                it.readCharacteristic(UUID.fromString(CHAR_CURRENT)).subscribe({ onSuccess ->
                    current.postValue(processData(onSuccess))
                }, {
                    Log.e(TAG, it.message);
                })
                it.readCharacteristic(UUID.fromString(CHAR_USED_ENERGY)).subscribe({ onSuccess ->
                    usedEnergy.postValue(processData(onSuccess))
                }, {
                    Log.e(TAG, it.message);
                })
                it.readCharacteristic(UUID.fromString(CHAR_TOTAL_ENERGY)).subscribe({ onSuccess ->
                    totalEnergy.postValue(processData(onSuccess))
                }, {
                    Log.e(TAG, it.message);
                })

                it.readCharacteristic(UUID.fromString(CHAR_LATITUDE)).subscribe({ onSuccess ->
                    latitude.postValue(processData(onSuccess))
                }, {
                    Log.e(TAG, it.message);
                })

                it.readCharacteristic(UUID.fromString(CHAR_LONGITUDE)).subscribe({ onSuccess ->
                    longitude.postValue(processData(onSuccess))
                }, {
                    Log.e(TAG, it.message);
                })

                it.readCharacteristic(UUID.fromString(CHAR_SPEED)).subscribe({ onSuccess ->
                    speed.postValue(processData(onSuccess))
                }, {
                    Log.e(TAG, it.message);
                })
            } else {
                voltage.postValue(null);
                current.postValue(null);
                usedEnergy.postValue(null);
                totalEnergy.postValue(null);

                latitude.postValue(null)
                longitude.postValue(null)
                speed.postValue(null)
            }
        }
    }

    private fun processData(data: ByteArray) : Double {
        return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).double
    }

    override fun onCleared() {
        super.onCleared()
    }
}