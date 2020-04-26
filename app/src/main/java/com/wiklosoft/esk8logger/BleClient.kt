package com.wiklosoft.esk8logger

import android.content.Context
import android.util.Log
import com.polidea.rxandroidble2.NotificationSetupMode
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.LocalDateTime
import java.util.*

private const val TAG = "BleClient"

private const val CHAR_STATE = "0000fd01-0000-1000-8000-00805f9b34fb"

private const val CHAR_VOLTAGE = "0000ff01-0000-1000-8000-00805f9b34fb"
private const val CHAR_CURRENT = "0000ff02-0000-1000-8000-00805f9b34fb"
private const val CHAR_USED_ENERGY = "0000ff03-0000-1000-8000-00805f9b34fb"
private const val CHAR_TOTAL_ENERGY = "0000ff04-0000-1000-8000-00805f9b34fb"

private const val CHAR_LATITUDE = "0000fe01-0000-1000-8000-00805f9b34fb"
private const val CHAR_LONGITUDE = "0000fe02-0000-1000-8000-00805f9b34fb"
private const val CHAR_SPEED = "0000fe03-0000-1000-8000-00805f9b34fb"
private const val CHAR_TRIP_DISTANCE = "0000fe04-0000-1000-8000-00805f9b34fb"

private const val CHAR_GPS_FIX = "0000fe05-0000-1000-8000-00805f9b34fb"
private const val CHAR_GPS_SATELLITE_COUNT = "0000fe06-0000-1000-8000-00805f9b34fb"

private const val CHAR_MANUAL_RIDE_START = "0000fd02-0000-1000-8000-00805f9b34fb"
private const val CHAR_WIFI_SSID = "0000fd03-0000-1000-8000-00805f9b34fb"
private const val CHAR_WIFI_PASS = "0000fd04-0000-1000-8000-00805f9b34fb"
private const val CHAR_WIFI_ENABLED = "0000fd05-0000-1000-8000-00805f9b34fb"

private const val CHAR_FREE_STORAGE = "0000fd06-0000-1000-8000-00805f9b34fb"
private const val CHAR_TOTAL_STORAGE = "0000fd07-0000-1000-8000-00805f9b34fb"

private const val CHAR_TIME = "0000fd08-0000-1000-8000-00805f9b34fb"

enum class Esk8palState(val value: Byte){
    PARKED(0),
    RIDING(1);

    companion object {
        fun of(value: Byte) = values().find { it.value == value}
            ?: throw IllegalAccessException("")
    }
};


class BleClient {
    private var bleClient: RxBleClient
    lateinit var connectionSub: Disposable
    private var connection: RxBleConnection? = null

    var connectionState = BehaviorSubject.create<RxBleConnection.RxBleConnectionState>()

    var voltage = BehaviorSubject.create<Double>()
    var current = BehaviorSubject.create<Double>()
    val usedEnergy = BehaviorSubject.create<Double>()
    var totalEnergy = BehaviorSubject.create<Double>()

    var speed = BehaviorSubject.create<Double>()
    var tripDistance = BehaviorSubject.create<Double>()

    var latitude = BehaviorSubject.create<Double>()
    var longitude = BehaviorSubject.create<Double>()

    var state = BehaviorSubject.create<Esk8palState>()

    var gpsFixStatus = BehaviorSubject.create<Byte>()
    var gpsSatelliteCount = BehaviorSubject.create<Byte>()

    constructor(context: Context) {
        bleClient = RxBleClient.create(context)
        getDevice().observeConnectionStateChanges().subscribe {
            connectionState.onNext(it)
            if (it == RxBleConnection.RxBleConnectionState.DISCONNECTED) {
                connect()
            }
        }

        if (getDevice().connectionState == RxBleConnection.RxBleConnectionState.DISCONNECTED) {
            connect()
        }
    }

    private fun getDeviceMac(): String { //TODO store it somewhere, add activity when scan options
        return "30:AE:A4:4C:D2:52"
    }

    private fun connect() {
        connectionSub = bleClient.getBleDevice(getDeviceMac()).establishConnection(true).subscribe({
            Log.d(TAG, "connected");
            connection = it
            onConnected()
        }, {
            connection = null
            Log.e(TAG, it.toString());
        })
    }

    private fun onConnected() {
        observeVoltage()
        observeCurrent()
        observeUsedEnergy()
        observeTotalEnergy()

        observeLatitude()
        observeLongitude()
        observeSpeed()

        observeTripDistance()

        observeState()
        observeGPSState()

        setTime()
    }

    fun setTime() {
        val cal = Calendar.getInstance()

        val data = ByteArray(6)
        data[0] = (cal.get(Calendar.YEAR) - 2000).toByte()
        data[1] = cal.get(Calendar.MONTH).toByte()
        data[2] = cal.get(Calendar.DAY_OF_MONTH).toByte()

        data[3] = cal.get(Calendar.HOUR_OF_DAY).toByte()
        data[4] = cal.get(Calendar.MINUTE).toByte()
        data[5] = cal.get(Calendar.SECOND).toByte()

        connection?.writeCharacteristic(UUID.fromString(CHAR_TIME), data)?.subscribe({

        }, {

        })
    }

    fun disconnect() {
        connectionSub.dispose()
    }

    fun getDevice(): RxBleDevice {
        return bleClient.getBleDevice(getDeviceMac())
    }

    fun getWifiSSID(): Single<ByteArray>? {
        return connection?.readCharacteristic(UUID.fromString(CHAR_WIFI_SSID))
    }

    fun setWifiSSID(ssid: String): Single<ByteArray>? {
        return connection?.writeCharacteristic(UUID.fromString(CHAR_WIFI_SSID), ssid.toByteArray())
    }

    fun getWifiPass(): Single<ByteArray>? {
        return connection?.readCharacteristic(UUID.fromString(CHAR_WIFI_PASS))
    }

    fun setWifiPass(password: String): Single<ByteArray>? {
        return connection?.writeCharacteristic(
            UUID.fromString(CHAR_WIFI_PASS),
            password.toByteArray()
        )
    }

    fun getWifiState(): Single<ByteArray>? {
        return connection?.readCharacteristic(UUID.fromString(CHAR_WIFI_ENABLED))
    }

    fun setWifiState(value: Byte): Single<ByteArray>? {
        return connection?.writeCharacteristic(
            UUID.fromString(CHAR_WIFI_ENABLED),
            ByteArray(1) { value })
    }

    fun setManualRideStart(value: Byte): Single<ByteArray>? {
        return connection?.writeCharacteristic(
            UUID.fromString(CHAR_MANUAL_RIDE_START),
            ByteArray(1) { value })
    }

    private fun processData(data: ByteArray) : Double {
        return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).double
    }

    fun getManualRideStart(): Single<ByteArray>? {
        return connection?.readCharacteristic(UUID.fromString(CHAR_MANUAL_RIDE_START))
    }

    fun getFreeStorage(): Single<ByteArray>? {
        return connection?.readCharacteristic(UUID.fromString(CHAR_FREE_STORAGE))
    }

    fun getTotalStorage(): Single<ByteArray>? {
        return connection?.readCharacteristic(UUID.fromString(CHAR_TOTAL_STORAGE))
    }

    fun setState(value: Byte): Single<ByteArray>? {
        return connection?.writeCharacteristic(UUID.fromString(CHAR_STATE), ByteArray(1) { value })
    }

    private fun observeVoltage() {
        connection?.setupNotification(UUID.fromString(CHAR_VOLTAGE), NotificationSetupMode.QUICK_SETUP)?.subscribe({ observable ->
            observable.subscribe { data ->
                voltage.onNext(processData(data))
            }
        }, {
            Log.e(TAG, it.message);
        })
    }

    private fun observeCurrent() {
        connection?.setupNotification(UUID.fromString(CHAR_CURRENT), NotificationSetupMode.QUICK_SETUP)?.subscribe({ observable ->
            observable.subscribe { data ->
                current.onNext(processData(data))
            }
        }, {
            Log.e(TAG, it.message);
        })
    }

    private fun observeUsedEnergy() {
        connection?.setupNotification(UUID.fromString(CHAR_USED_ENERGY), NotificationSetupMode.QUICK_SETUP)?.subscribe({ observable ->
            observable.subscribe { data ->
                usedEnergy.onNext(processData(data))
            }
        }, {
            Log.e(TAG, it.message);
        })
    }

    private fun observeTotalEnergy() {
        connection?.setupNotification(UUID.fromString(CHAR_TOTAL_ENERGY), NotificationSetupMode.QUICK_SETUP)?.subscribe({ observable ->
            observable.subscribe { data ->
                totalEnergy.onNext(processData(data))
            }
        }, {
            Log.e(TAG, it.message);
        })
    }

    private fun observeSpeed() {
        connection?.setupNotification(UUID.fromString(CHAR_SPEED), NotificationSetupMode.QUICK_SETUP)?.subscribe({ observable ->
            observable.subscribe { data ->
                speed.onNext(processData(data))
            }
        }, {
            Log.e(TAG, it.message);
        })
    }

    private fun observeTripDistance() {
        connection?.setupNotification(UUID.fromString(CHAR_TRIP_DISTANCE), NotificationSetupMode.QUICK_SETUP)?.subscribe({ observable ->
            observable.subscribe { data ->
                tripDistance.onNext(processData(data))
            }
        }, {
            Log.e(TAG, it.message);
        })
    }

    private fun observeLatitude() {
        connection?.setupNotification(UUID.fromString(CHAR_LATITUDE), NotificationSetupMode.QUICK_SETUP)?.subscribe({ observable ->
            observable.subscribe { data ->
                latitude.onNext(processData(data))
            }
        }, {
            Log.e(TAG, it.message);
        })
    }

    private fun observeLongitude() {
        connection?.setupNotification(UUID.fromString(CHAR_LONGITUDE), NotificationSetupMode.QUICK_SETUP)?.subscribe({ observable ->
            observable.subscribe { data ->
                longitude.onNext(processData(data))
            }
        }, {
            Log.e(TAG, it.message);
        })
    }

    private fun observeState() {
        connection?.setupNotification(UUID.fromString(CHAR_STATE), NotificationSetupMode.QUICK_SETUP)?.subscribe({ observable ->
            observable.subscribe { data ->
                state.onNext(Esk8palState.of(data[0]))
            }
        }, {
            Log.e(TAG, it.message);
        })
    }

    private fun observeGPSState() {
        connection?.setupNotification(UUID.fromString(CHAR_GPS_FIX), NotificationSetupMode.QUICK_SETUP)?.subscribe({ observable ->
            observable.subscribe { data ->
                gpsFixStatus.onNext(data[0])
            }
        }, {
            Log.e(TAG, it.message);
        })

        connection?.setupNotification(UUID.fromString(CHAR_GPS_SATELLITE_COUNT), NotificationSetupMode.QUICK_SETUP)?.subscribe({ observable ->
            observable.subscribe { data ->
                gpsSatelliteCount.onNext(data[0])
            }
        }, {
            Log.e(TAG, it.message);
        })
    }
}