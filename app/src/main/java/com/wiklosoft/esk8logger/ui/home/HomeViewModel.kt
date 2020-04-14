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

private const val CHAR_STATE = "0000fd01-0000-1000-8000-00805f9b34fb"

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

    var state = MutableLiveData<Int>()

    constructor(application: Application) : super(application) {
        bleClient.connection.observeForever {

            if (it != null) {
                it.setupNotification(UUID.fromString(CHAR_VOLTAGE)).subscribe({ observable ->
                    observable.subscribe { data ->
                        voltage.postValue(processData(data))
                    }
                }, {
                    Log.e(TAG, it.message);
                })

                it.setupNotification(UUID.fromString(CHAR_CURRENT)).subscribe({ observable ->
                    observable.subscribe { data ->
                        current.postValue(processData(data))
                    }
                }, {
                    Log.e(TAG, it.message);
                })
                it.setupNotification(UUID.fromString(CHAR_USED_ENERGY)).subscribe({ observable ->
                    observable.subscribe { data ->
                        usedEnergy.postValue(processData(data))
                    }
                }, {
                    Log.e(TAG, it.message);
                })
                it.setupNotification(UUID.fromString(CHAR_TOTAL_ENERGY)).subscribe({ observable ->
                    observable.subscribe { data ->
                        totalEnergy.postValue(processData(data))
                    }
                }, {
                    Log.e(TAG, it.message);
                })

                it.setupNotification(UUID.fromString(CHAR_LATITUDE)).subscribe({ observable ->
                    observable.subscribe { data ->
                        latitude.postValue(processData(data))
                    }
                }, {
                    Log.e(TAG, it.message);
                })

                it.setupNotification(UUID.fromString(CHAR_LONGITUDE)).subscribe({ observable ->
                    observable.subscribe { data ->
                        longitude.postValue(processData(data))
                    }
                }, {
                    Log.e(TAG, it.message);
                })

                it.setupNotification(UUID.fromString(CHAR_SPEED)).subscribe({ observable ->
                    observable.subscribe { data ->
                        speed.postValue(processData(data))
                    }
                }, {
                    Log.e(TAG, it.message);
                })

                it.setupNotification(UUID.fromString(CHAR_STATE)).subscribe({ observable ->
                    observable.subscribe { data ->
                        Log.i(TAG, "state update " + data[0].toInt());
                        state.postValue(data[0].toInt())
                    }
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

    fun setState(value: Byte) {
        Log.i(TAG, "setState $value");
        bleClient.connection.value?.writeCharacteristic(UUID.fromString(CHAR_STATE), ByteArray(1) {value})?.subscribe({

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