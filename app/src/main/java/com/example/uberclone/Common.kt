package com.example.uberclone

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.uberclone.Model.DriverInfoModel

object Common {

    val DRIVERS_LOCATION_REFERENCE: String = "DriversLocation"
    val TOKEN_REFERENCE: String = "Token"
    var currentUser: DriverInfoModel? = null
    val DRIVER_INFO_REFERENCE: String = "DriverInfo"
    val NOTI_BODY:String = "body"
    val NOTI_TITLE:String = "title"


    fun showNotification(context: Context, id: Int, title: String?, body: String?, intent: Intent?){
        var pendingIntent : PendingIntent? = null
        if (intent != null)
        {
            pendingIntent = PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT)
            val NOTTIFICATION_CHANNEL_ID = "edmt_dev_uber_remake"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                val notificationChannel = NotificationChannel(NOTTIFICATION_CHANNEL_ID, "Uber Clone",
                NotificationManager.IMPORTANCE_HIGH)
                notificationChannel.description = "Uber Clone"
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = Color.RED
                notificationChannel.vibrationPattern = longArrayOf(0,1000,500,1000)
                notificationChannel.enableVibration(true)

                notificationManager.createNotificationChannel(notificationChannel)
            }
            val builder = NotificationCompat.Builder(context,NOTTIFICATION_CHANNEL_ID)
            builder.setContentTitle(title)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.baseline_directions_car_24)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources,R.drawable.baseline_directions_car_24))
            if (pendingIntent != null)
                builder.setContentIntent(pendingIntent)
            val notification = builder.build()
            notificationManager.notify(id,notification)
        }
    }

    fun buildWelcomeMessage(): String {
        return StringBuilder("Добро пожаловать, ")
            .append(currentUser!!.firstName)
            .append(" ")
            .append(currentUser!!.lastName)
            .toString()
    }
}