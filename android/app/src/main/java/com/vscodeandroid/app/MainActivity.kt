package com.vscodeandroid.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.*

class MainActivity : Activity() {
    private lateinit var web: WebView
    private val PICK = 1001

    @SuppressLint(["SetJavaScriptEnabled", "AddJavascriptInterface"])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = true
        web.settings.allowContentAccess = true
        web.settings.loadWithOverviewMode = true
        web.settings.useWideViewPort = true
        web.settings.javaScriptCanOpenWindowsAutomatically = true
        web.settings.mediaPlaybackRequiresUserGesture = false
        web.webViewClient = WebViewClient()
        web.webChromeClient = WebChromeClient()
        web.addJavascriptInterface(Bridge(), "AndroidBridge")
        web.loadUrl("file:///android_asset/www/index.html")
    }

    inner class Bridge {
        @JavascriptInterface fun saveFiles(json: String) {
            getSharedPreferences("vscode", MODE_PRIVATE).edit().putString("files", json).apply()
        }
        @JavascriptInterface fun loadFiles(): String {
            return getSharedPreferences("vscode", MODE_PRIVATE).getString("files", "") ?: ""
        }
        @JavascriptInterface fun openFile() {
            runOnUiThread {
                val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE); type = "*/*" }
                startActivityForResult(i, PICK)
            }
        }
    }

    override fun onActivityResult(rc: Int, res: Int, data: Intent?) {
        super.onActivityResult(rc, res, data)
        if (rc == PICK && res == Activity.RESULT_OK) {
            val uri: Uri = data?.data ?: return
            val text = contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: ""
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "imported.txt"
            val safe = text.replace("\\", "\\\\").replace("`", "\\`").replace("$", "\\$")
            web.evaluateJavascript("onAndroidFile(`$name`, `$safe`)", null)
        }
    }

    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
