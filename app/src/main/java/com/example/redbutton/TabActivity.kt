package com.example.redbutton

import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient

// Окно с отдельным хранилищем WebView. Серверы работают в ServerService,
// окно только открывает localhost нужного порта.
// Каждый подкласс запускается в своём процессе (см. AndroidManifest),
// поэтому cookie и вход у окон полностью разделены.
abstract class TabActivity(private val port: Int, private val dataSuffix: String) : Activity() {

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

        val view = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
        }
        webView = view
        setContentView(view)
        view.loadUrl("http://localhost:$port")
    }

    override fun onBackPressed() {
        val view = webView
        if (view != null && view.canGoBack()) {
            view.goBack()
        } else {
            super.onBackPressed()
        }
    }
}

class Tab1Activity : TabActivity(8300, "tab1")
class Tab2Activity : TabActivity(8400, "tab2")
class Tab3Activity : TabActivity(8500, "tab3")
