package com.ifpr.androidapptemplate.ui.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ifpr.androidapptemplate.MainActivity
import com.ifpr.androidapptemplate.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Payload de DADOS
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            val status = remoteMessage.data["status"]
            val message = remoteMessage.data["message"]

            if (status != null) {
                // NOVA notificação customizada com progresso
                sendStatusNotification(status)
            } else if (message != null) {
                // Fallback pra notificação simples
                sendNotification(message)
            }
        }

        // Payload de NOTIFICAÇÃO (caso exista)
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.body)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // Enviar token para o seu servidor de aplicativos para gerenciar subscrições
    }

    // Notificação simples (já existia)
    private fun sendNotification(messageBody: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "fcm_default_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_status_pedido)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Cria o canal de notificação no Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    // NOVA notificação de status com layout customizado e barra de progresso
    private fun sendStatusNotification(status: String) {
        Log.d(TAG, "Chamou sendStatusNotification com status=$status")

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "status_channel"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Status do Pedido",
                NotificationManager.IMPORTANCE_HIGH // mais destaque
            )
            notificationManager.createNotificationChannel(channel)
        }

        val targetProgress = when (status) {
            "em preparo" -> 33
            "saindo para entrega" -> 66
            "entregue" -> 100
            else -> 0
        }

        val remoteViews = RemoteViews(packageName, R.layout.custom_notification)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_status_pedido) // seu ícone novo aqui
            .setCustomContentView(remoteViews)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // pra aparecer forte
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setAutoCancel(false)
            .setOngoing(true)

        val notificationId = 1001
        var currentProgress = 0

        val handler = android.os.Handler(mainLooper)
        val updateRunnable = object : Runnable {
            override fun run() {
                if (currentProgress <= targetProgress) {
                    remoteViews.setTextViewText(R.id.status_text, "Pedido: $status")
                    remoteViews.setProgressBar(R.id.progress_bar, 100, currentProgress, false)
                    notificationManager.notify(notificationId, builder.build())
                    currentProgress++
                    handler.postDelayed(this, 50L)
                }
            }
        }

        handler.post(updateRunnable)

        if (targetProgress == 100) {
            handler.postDelayed({
                notificationManager.cancel(notificationId)
            }, 3000L)
        }
    }

}
