package fgapps.com.br.iassistant2.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import fgapps.com.br.iassistant2.R
import fgapps.com.br.iassistant2.activities.NotificationActivity

object Notifications {

    private val NOTIF_ID = 22

    fun createNotification(context: Context, musicName: String, isPlaying: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationLayout = RemoteViews(context.packageName, R.layout.notification_model)
//            val notificationExpLayout = RemoteViews(context.packageName, R.layout.notification_model)

        createNotificationActions(context, notificationLayout, musicName, isPlaying)
//            createNotificationActions(context, notificationExpLayout, musicName, isPlaying)

        val builder = NotificationCompat.Builder(context, "PC")
                .setSmallIcon(R.drawable.app_icon)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
//                    .setCustomBigContentView(notificationExpLayout)
                .setColor(context.resources.getColor(R.color.colorAccent, null))
                .setSubText("Player inicializado")
                .setShowWhen(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("PC", "Player control", importance).apply {
                description = "Allow user control music player through the notification."
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(NOTIF_ID, builder.build())
    }

    private fun createNotificationActions(context: Context, notificationLayout: RemoteViews,
                                          musicName: String, isPlaying: Boolean) {

        // Set Music name, Play or Pause Action Image
        notificationLayout.setTextViewText(R.id.notifMusic_txt, musicName)
        if (isPlaying) notificationLayout.setImageViewResource(R.id.notifPP_btn, R.drawable.ic_pause)
        else notificationLayout.setImageViewResource(R.id.notifPP_btn, R.drawable.ic_play)

        // Set the button's Actions
        val assistIntent = Intent(context, NotificationActivity::class.java)
        assistIntent.putExtra("notify", "assist")
        notificationLayout.setOnClickPendingIntent(R.id.notifAssist_btn,
                PendingIntent.getService(context, 0, assistIntent, PendingIntent.FLAG_UPDATE_CURRENT))

        val prevIntent = Intent(context, NotificationActivity::class.java)
        prevIntent.putExtra("notify", "prev")
        notificationLayout.setOnClickPendingIntent(R.id.notifPrev_btn,
                PendingIntent.getService(context, 1, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT))

        val ppIntent = Intent(context, NotificationActivity::class.java)
        ppIntent.putExtra("notify", "play/pause")
        notificationLayout.setOnClickPendingIntent(R.id.notifPP_btn,
                PendingIntent.getService(context, 2, ppIntent, PendingIntent.FLAG_UPDATE_CURRENT))

        val nextIntent = Intent(context, NotificationActivity::class.java)
        nextIntent.putExtra("notify", "next")
        notificationLayout.setOnClickPendingIntent(R.id.notifNext_btn,
                PendingIntent.getService(context, 3, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT))
    }

    fun cancelNotification(context: Context){
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIF_ID)
    }

}