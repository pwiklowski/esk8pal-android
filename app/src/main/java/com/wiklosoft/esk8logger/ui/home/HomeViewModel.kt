package com.wiklosoft.esk8logger.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.polidea.rxandroidble2.RxBleConnection
import com.wiklosoft.esk8logger.App
import com.wiklosoft.esk8logger.BleClient
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

    var state = MutableLiveData<Int>()

    constructor(application: Application) : super(application) {
        with(bleClient) {
            observeVoltage()?.subscribe({ observable ->
                observable.subscribe { data ->
                    voltage.postValue(processData(data))
                }
            }, {
                Log.e(TAG, it.message);
            })

            observeCurrent()?.subscribe({ observable ->
                observable.subscribe { data ->
                    current.postValue(processData(data))
                }
            }, {
                Log.e(TAG, it.message);
            })

            observeUsedEnergy()?.subscribe({ observable ->
                observable.subscribe { data ->
                    usedEnergy.postValue(processData(data))
                }
            }, {
                Log.e(TAG, it.message);
            })

            observeTotalEnergy()?.subscribe({ observable ->
                observable.subscribe { data ->
                    totalEnergy.postValue(processData(data))
                }
            }, {
                Log.e(TAG, it.message);
            })

            observeLatitude()?.subscribe({ observable ->
                observable.subscribe { data ->
                    latitude.postValue(processData(data))
                }
            }, {
                Log.e(TAG, it.message);
            })

            observeLongitude()?.subscribe({ observable ->
                observable.subscribe { data ->
                    longitude.postValue(processData(data))
                }
            }, {
                Log.e(TAG, it.message);
            })

            observeSpeed()?.subscribe({ observable ->
                observable.subscribe { data ->
                    speed.postValue(processData(data))
                }
            }, {
                Log.e(TAG, it.message);
            })

            observeState()?.subscribe({ observable ->
                observable.subscribe { data ->
                    Log.i(TAG, "state update " + data[0].toInt());
                    state.postValue(data[0].toInt())
                }
            }, {
                Log.e(TAG, it.message);
            })
        }
    }

    fun setState(value: Byte) {
        Log.i(TAG, "setState $value");
        bleClient.setState(value)?.subscribe({

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