package com.wiklosoft.esk8logger

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.polidea.rxandroidble2.RxBleClient


class App : Application() {
    lateinit var bleClient: BleClient

    val CHANNEL_ID: String = "ride_channel"
    var NOTIFICTION_ID = 1

    override fun onCreate() {
        super.onCreate()
        bleClient = BleClient(applicationContext)

        createNotificationChannel()

        bleClient.state.subscribe( {
            showNotification(it, 0.0, 0, 0.0)
        },{

        })

    }

    private fun showNotification(state: Esk8palState, trip: Double, time: Int, speed: Double) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSound(null)

        if (state == Esk8palState.PARKED) {

            with(NotificationManagerCompat.from(this)) {
                cancel(NOTIFICTION_ID)
            }
        } else {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText("$state - $trip km - $time min - $speed km/h"))
            with(NotificationManagerCompat.from(this)) {
                notify(NOTIFICTION_ID, builder.build())
            }
        }

    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}