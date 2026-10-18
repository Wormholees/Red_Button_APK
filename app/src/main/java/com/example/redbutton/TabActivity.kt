package com.example.redbutton

import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.concurrent.thread

// Одно окно с отдельным сервером и отдельным хранилищем WebView.
// Каждый подкласс запускается в своём процессе (см. AndroidManifest),
// поэтому cookie и вход у окон полностью разделены.
abstract class TabActivity(private val port: Int, private val dataSuffix: String) : Activity() {

    private var server: ServerSocket? = null
    private var webView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Отдельный каталог данных WebView для этого процесса.
        // Должно вызываться до создания первого WebView в процессе.
        try {
            WebView.setDataDirectorySuffix(dataSuffix)
        } catch (e: Exception) {
            // Уже инициализировано: продолжаем с общим хранилищем
        }

        startServer()

        val view = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
        }
        webView = view
        setContentView(view)
        view.loadUrl("http://localhost:$port")
    }

    private fun startServer() {
        try {
            val s = ServerSocket(port, 50, InetAddress.getByName("127.0.0.1"))
            server = s
            thread(isDaemon = true) { serve(s) }
        } catch (e: Exception) {
            // Порт занят: страница всё равно откроется, если сервер уже работает
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
}

class Tab1Activity : TabActivity(8300, "tab1")
class Tab2Activity : TabActivity(8400, "tab2")
class Tab3Activity : TabActivity(8500, "tab3")
