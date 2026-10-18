package com.example.redbutton

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.webkit.WebView
import android.webkit.WebViewClient
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
    private var webView: WebView? = null

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
        // Запускаем локальный сервер на 127.0.0.1:8300, если он ещё не запущен
        if (server == null) {
            try {
                val s = ServerSocket(PORT, 50, InetAddress.getByName("127.0.0.1"))
                server = s
                thread(isDaemon = true) { serve(s) }
            } catch (e: Exception) {
                Toast.makeText(this, "Не удалось запустить сервер на порту $PORT", Toast.LENGTH_LONG).show()
                return
            }
        }

        // Открываем localhost:8300 внутри приложения; сервер отвечает редиректом на Google
        val view = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
        }
        webView = view
        setContentView(view)
        view.loadUrl("http://localhost:$PORT")
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

    // Кнопка «Назад» сначала возвращает на предыдущую страницу в WebView
    override fun onBackPressed() {
        val view = webView
        if (view != null && view.canGoBack()) {
            view.goBack()
        } else {
            super.onBackPressed()
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
