package com.example.redbutton

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.concurrent.thread

// Фоновый сервис: держит серверы на портах 8300, 8400, 8500 работающими,
// даже когда окна приложения закрыты.
class ServerService : Service() {

    private val servers = mutableListOf<ServerSocket>()

    override fun onCreate() {
        super.onCreate()
        createChannel()
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIF_ID, notification)
        }
        for (port in PORTS) {
            startServer(port)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startServer(port: Int) {
        try {
            val s = ServerSocket(port, 50, InetAddress.getByName("127.0.0.1"))
            servers.add(s)
            thread(isDaemon = true) { serve(s) }
        } catch (e: Exception) {
            // Порт занят или ошибка сокета: пропускаем этот порт
        }
    }

    // На любой GET-запрос отвечает редиректом на Google
    private fun serve(s: ServerSocket) {
        while (!s.isClosed) {
            try {
                s.accept().use { socket ->
                    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                    var line = reader.readLine()
                    while (!line.isNullOrEmpty()) {
                        line = reader.readLine()
                    }
                    val response = "HTTP/1.1 302 Found\r\n" +
                            "Location: https://www.google.com\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n\r\n"
                    socket.getOutputStream().apply {
                        write(response.toByteArray())
                        flush()
                    }
                }
            } catch (e: Exception) {
                // Игнорируем ошибки отдельных соединений
            }
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(CHANNEL_ID, "Серверы", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return builder
            .setContentTitle("Серверы работают")
            .setContentText("Порты: 8300, 8400, 8500")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        servers.forEach { it.close() }
        servers.clear()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "servers"
        const val NOTIF_ID = 1
        val PORTS = listOf(8300, 8400, 8500)
    }
}
