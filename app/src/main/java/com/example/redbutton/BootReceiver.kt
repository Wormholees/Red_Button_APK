package com.example.redbutton

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

// Запускает сервис с серверами после перезагрузки телефона
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val service = Intent(context, ServerService::class.java)
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(service)
            } else {
                context.startService(service)
            }
        }
    }
}
