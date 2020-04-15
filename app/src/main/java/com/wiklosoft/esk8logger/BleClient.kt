package com.wiklosoft.esk8logger

import android.content.Context
import android.util.Log
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
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


private const val CHAR_MANUAL_RIDE_START = "0000fd02-0000-1000-8000-00805f9b34fb"
private const val CHAR_WIFI_SSID = "0000fd03-0000-1000-8000-00805f9b34fb"
private const val CHAR_WIFI_PASS = "0000fd04-0000-1000-8000-00805f9b34fb"
private const val CHAR_WIFI_ENABLED = "0000fd05-0000-1000-8000-00805f9b34fb"

private const val CHAR_FREE_STORAGE = "0000fd06-0000-1000-8000-00805f9b34fb"
private const val CHAR_TOTAL_STORAGE = "0000fd07-0000-1000-8000-00805f9b34fb"

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

    constructor(context: Context) {
        bleClient = RxBleClient.create(context)
        getDevice().observeConnectionStateChanges().subscribe {
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

    fun connect() {
        connectionSub = bleClient.getBleDevice(getDeviceMac()).establishConnection(true).subscribe({
            Log.d(TAG, "connected");
            connection = it
        }, {
            connection = null
            Log.e(TAG, it.toString());
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

    fun observeVoltage(): Observable<Observable<ByteArray>>? {
        return connection?.setupNotification(UUID.fromString(CHAR_VOLTAGE))
    }

    fun observeCurrent(): Observable<Observable<ByteArray>>? {
        return connection?.setupNotification(UUID.fromString(CHAR_CURRENT))
    }

    fun observeUsedEnergy(): Observable<Observable<ByteArray>>? {
        return connection?.setupNotification(UUID.fromString(CHAR_USED_ENERGY))
    }

    fun observeTotalEnergy(): Observable<Observable<ByteArray>>? {
        return connection?.setupNotification(UUID.fromString(CHAR_TOTAL_ENERGY))
    }

    fun observeSpeed(): Observable<Observable<ByteArray>>? {
        return connection?.setupNotification(UUID.fromString(CHAR_SPEED))
    }

    fun observeLatitude(): Observable<Observable<ByteArray>>? {
        return connection?.setupNotification(UUID.fromString(CHAR_LATITUDE))
    }

    fun observeLongitude(): Observable<Observable<ByteArray>>? {
        return connection?.setupNotification(UUID.fromString(CHAR_LONGITUDE))
    }

    fun observeState(): Observable<Observable<ByteArray>>? {
        return connection?.setupNotification(UUID.fromString(CHAR_STATE))
    }
}