package com.example.project_s

import android.os.Build
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NewsWebView : AppCompatActivity() {
    lateinit var webview: WebView
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stock_web)
        //로딩
        showLoadingDialog()
        webview = findViewById(R.id.webview1)
        webview.settings.apply {
            this.setSupportZoom(false) // 화면 확대 허용
            this.javaScriptEnabled = true // 자바스크립트 허용
            this.domStorageEnabled = true // 로컬 저장 허용
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
        }
        webview.apply{
            webViewClient = WebViewClient()
        }

        webview.loadUrl("https://m.stock.naver.com/news/mainnews")
    }
    override fun onBackPressed() {
        if (webview.canGoBack()) { webview.goBack() }
        else { finish() }
    }
    private fun showLoadingDialog() {
        val dialog = LoadingDialog(this@NewsWebView)
        CoroutineScope(Main).launch {
            dialog.show()
            delay(2000)
            dialog.dismiss()
        }
    }
}