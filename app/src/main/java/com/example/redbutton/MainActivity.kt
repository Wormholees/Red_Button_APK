package com.example.redbutton

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private var server: ServerSocket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val button = Button(this).apply {
            text = "Открыть Google"
            setBackgroundColor(Color.RED)
            setTextColor(Color.WHITE)
            textSize = 24f
            setPadding(96, 48, 96, 48)
            setOnClickListener { startServerAndOpenGoogle() }
        }

        val layout = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#F4F4F4"))
            addView(button)
        }

        setContentView(layout)
    }

    private fun startServerAndOpenGoogle() {
        if (server == null) {
            try {
                // Слушаем только на этом устройстве (127.0.0.1), порт 8300
                val s = ServerSocket(PORT, 50, InetAddress.getByName("127.0.0.1"))
                server = s
                thread(isDaemon = true) { serve(s) }
            } catch (e: Exception) {
                Toast.makeText(this, "Не удалось запустить сервер на порту $PORT", Toast.LENGTH_LONG).show()
                return
            }
        }

        // Открываем адрес сервера в браузере; сервер отвечает редиректом на Google
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://localhost:$PORT"))
        startActivity(intent)
    }

    // Простой HTTP-сервер: на любой GET-запрос отвечает редиректом на Google
    private fun serve(s: ServerSocket) {
        while (!s.isClosed) {
            try {
                s.accept().use { socket ->
                    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                    // Читаем заголовки запроса до пустой строки
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

    override fun onDestroy() {
        server?.close()
        server = null
        super.onDestroy()
    }

    companion object {
        const val PORT = 8300
    }
}
