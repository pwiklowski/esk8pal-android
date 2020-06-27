package com.wiklosoft.esk8logger

import android.content.Context
import android.os.Handler
import android.util.Log
import com.polidea.rxandroidble2.*

import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import java.util.concurrent.TimeUnit

private const val TAG = "BleClient"

private const val CHAR_STATE = "0000fd01-0000-1000-8000-00805f9b34fb"

private const val CHAR_VOLTAGE = "0000ff01-0000-1000-8000-00805f9b34fb"
private const val CHAR_CURRENT = "0000ff02-0000-1000-8000-00805f9b34fb"
private const val CHAR_USED_ENERGY = "0000ff03-0000-1000-8000-00805f9b34fb"
private const val CHAR_TOTAL_ENERGY = "0000ff04-0000-1000-8000-00805f9b34fb"


private const val CHAR_APP_STATE = "0000ffff-0000-1000-8000-00805f9b34fb"

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
    RIDING(1),
    CHARGING(2);

    companion object {
        fun of(value: Byte) = values().find { it.value == value}
            ?: throw IllegalAccessException("")
    }
};

enum class ConnectionState {
    CONNECTING("CONNECTING"),
    CONNECTED("CONNECTED"),
    INITIALIZED("INITIALIZED"),
    DISCONNECTED("DISCONNECTED"),
    DISCONNECTING( "DISCONNECTING");

    private var description: String? = null

    constructor(description: String?) {
        this.description = description
    }

    override fun toString(): String {
        return "RxBleConnectionState{$description}"
    }
}

class BleClient {
    private var bleClient: RxBleClient
    lateinit var connectionSub: Disposable
    private var appStateSub: Disposable? = null
    private var connection: RxBleConnection? = null
    private var scanSubscription: Disposable? = null

    var connectionState = BehaviorSubject.create<ConnectionState>()

    var voltage = BehaviorSubject.create<Double>()
    var current = BehaviorSubject.create<Double>()
    val usedEnergy = BehaviorSubject.create<Double>()
    var totalEnergy = BehaviorSubject.create<Double>()

    var speed = BehaviorSubject.create<Double>()
    var tripDistance = BehaviorSubject.create<Double>()
    var altitude = BehaviorSubject.create<Double>()

    var latitude = BehaviorSubject.create<Double>()
    var longitude = BehaviorSubject.create<Double>()

    var state = BehaviorSubject.create<Esk8palState>()

    var gpsFixStatus = BehaviorSubject.create<Byte>()
    var gpsSatelliteCount = BehaviorSubject.create<Byte>()

    var ridingTime = BehaviorSubject.create<Int>()

    constructor(context: Context) {
        bleClient = RxBleClient.create(context)

        handleConnectionState(getDevice().connectionState)
        getDevice().observeConnectionStateChanges().subscribe({
            handleConnectionState(it)
        }, {
            Log.e(TAG, it.toString());
        })
    }

    private fun handleConnectionState(state: RxBleConnection.RxBleConnectionState) {
        when (state) {
            RxBleConnection.RxBleConnectionState.CONNECTED -> connectionState.onNext(ConnectionState.CONNECTED)
            RxBleConnection.RxBleConnectionState.DISCONNECTED-> connectionState.onNext(ConnectionState.DISCONNECTED)
            RxBleConnection.RxBleConnectionState.DISCONNECTING-> connectionState.onNext(ConnectionState.DISCONNECTING)
            RxBleConnection.RxBleConnectionState.CONNECTING-> connectionState.onNext(ConnectionState.CONNECTING)
        }
    }

    private fun getDeviceMac(): String { //TODO store it somewhere, add activity when scan options
        return "24:0A:C4:C5:92:2A"
    }

    fun connect() {
        Log.d(TAG, "connect");
        scanSubscription = bleClient.scanBleDevices(
            ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build(),
            ScanFilter.Builder().setDeviceAddress(getDeviceMac()).build()
        ).subscribe(
            {
                Log.d(TAG, "result ${it.bleDevice.macAddress}");
                scanSubscription?.dispose()
                Handler().postDelayed({
                    connectToDevice()
                },500)
            },
            {
                // Handle an error here.
                Log.d(TAG, "error $it");
            }
        )
    }

    private fun connectToDevice(){
        connectionSub = bleClient.getBleDevice(getDeviceMac()).establishConnection(true).subscribe({
            Log.d(TAG, "connected $it");
            connection = it
            connection?.requestMtu(500)?.subscribe()
            onConnected()
        }, {
            connection = null
            Log.e(TAG, "connect $it");
        })
    }

    private fun onConnected() {
        observeAppState()
        setTime()
    }

    private fun setTime() {
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
        connectionState.onNext(ConnectionState.DISCONNECTED)
        connectionSub.dispose()
        appStateSub?.dispose()
        connection = null
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

    private fun observeAppState() {
        appStateSub = connection?.setupNotification(UUID.fromString(CHAR_APP_STATE), NotificationSetupMode.QUICK_SETUP)?.subscribe({ observable ->
                observable.subscribe({ data ->
                    current.onNext(processData(data.copyOfRange(0, 8)))
                    voltage.onNext(processData(data.copyOfRange(8, 16)))
                    usedEnergy.onNext(processData(data.copyOfRange(16, 24)))
                    totalEnergy.onNext(processData(data.copyOfRange(24, 32)))

                    speed.onNext(processData(data.copyOfRange(32, 40)))
                    latitude.onNext(processData(data.copyOfRange(40, 48)))
                    longitude.onNext(processData(data.copyOfRange(48, 56)))

                    tripDistance.onNext(processData(data.copyOfRange(56, 64)))

                    altitude.onNext(processData(data.copyOfRange(64, 72)))

                    ridingTime.onNext(ByteBuffer.wrap(data.copyOfRange(72, 76)).order(ByteOrder.LITTLE_ENDIAN).int)

                    gpsFixStatus.onNext(data[76])
                    gpsSatelliteCount.onNext(data[77])

                    if(state.value != Esk8palState.of(data[78])){
                        state.onNext(Esk8palState.of(data[78]))
                    }

                    //freeStorage: uint32_t
                    //totalStorage: uint32_t

                    if (connectionState.value != ConnectionState.INITIALIZED){
                        connectionState.onNext(ConnectionState.INITIALIZED)
                    }
                }, {
                    Log.e(TAG, it.message);
                })
            }, {
                Log.e(TAG, it.message);
            })
    }
}