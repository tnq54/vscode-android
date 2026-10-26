package com.vscodeandroid.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.*
import android.widget.EditText
import android.widget.LinearLayout

class MainActivity : Activity() {
    private lateinit var web: WebView
    private val PICK = 1001
    private val PREF = "vscode"
    private val LOCAL_URL = "file:///android_asset/www/index.html"

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)
        val cm = CookieManager.getInstance()
        cm.setAcceptCookie(true)
        cm.setAcceptThirdPartyCookies(web, true)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.databaseEnabled = true
        web.settings.allowFileAccess = true
        web.settings.allowContentAccess = true
        web.settings.loadWithOverviewMode = true
        web.settings.useWideViewPort = true
        web.settings.javaScriptCanOpenWindowsAutomatically = true
        web.settings.mediaPlaybackRequiresUserGesture = false
        web.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        web.settings.userAgentString = web.settings.userAgentString + " VSCodeAndroid"
        web.webViewClient = WebViewClient()
        web.webChromeClient = WebChromeClient()
        web.addJavascriptInterface(Bridge(), "AndroidBridge")
        loadSavedOrAsk()
    }

    private fun prefs() = getSharedPreferences(PREF, MODE_PRIVATE)

    private fun serverUrl(): String = prefs().getString("server_url", "") ?: ""
    private fun serverPass(): String = prefs().getString("server_pass", "") ?: ""

    private fun targetUrl(): String {
        val base = serverUrl().trim().trimEnd('/')
        if (base.isEmpty()) return LOCAL_URL
        val pass = serverPass()
        return if (pass.isNotEmpty()) "$base/?password=$pass" else base
    }

    private fun loadSavedOrAsk() {
        if (serverUrl().isEmpty()) {
            web.loadUrl(LOCAL_URL)
            showServerDialog()
        } else {
            web.loadUrl(targetUrl())
        }
    }

    private fun showServerDialog() {
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 20, 40, 0) }
        val urlInput = EditText(this).apply { hint = "https://server-cua-ban:8080"; setText(serverUrl()) }
        val passInput = EditText(this).apply { hint = "Password code-server (neu co)"; setText(serverPass()) }
        layout.addView(urlInput)
        layout.addView(passInput)
        AlertDialog.Builder(this)
            .setTitle("VS Code full-stack")
            .setMessage("Nhap URL code-server tren VPS de dung terminal that, Node/Python, extensions. Bo trong = dung offline.")
            .setView(layout)
            .setPositiveButton("Ket noi") { _, _ ->
                prefs().edit()
                    .putString("server_url", urlInput.text.toString().trim())
                    .putString("server_pass", passInput.text.toString())
                    .apply()
                web.loadUrl(targetUrl())
            }
            .setNegativeButton("Offline") { _, _ -> web.loadUrl(LOCAL_URL) }
            .setNeutralButton("Xem HD") { _, _ ->
                web.loadUrl(LOCAL_URL)
                AlertDialog.Builder(this)
                    .setTitle("Chay code-server tren VPS")
                    .setMessage("SSH vao VPS, chay:\n\ncurl -fsSL https://code-server.dev/install.sh | sh\ncode-server --host 0.0.0.0 --port 8080\n\nRoi mo port + dat PASSWORD, nhap URL vao app.")
                    .setPositiveButton("OK", null)
                    .show()
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 1, 0, "Server...")
        menu.add(0, 2, 0, "Offline mode")
        menu.add(0, 3, 0, "Reload")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> showServerDialog()
            2 -> web.loadUrl(LOCAL_URL)
            3 -> web.reload()
        }
        return true
    }

    inner class Bridge {
        @JavascriptInterface fun saveFiles(json: String) {
            prefs().edit().putString("files", json).apply()
        }
        @JavascriptInterface fun loadFiles(): String {
            return prefs().getString("files", "") ?: ""
        }
        @JavascriptInterface fun getServerUrl(): String = serverUrl()
        @JavascriptInterface fun setServerUrl(url: String, pass: String) {
            prefs().edit().putString("server_url", url).putString("server_pass", pass).apply()
            runOnUiThread { web.loadUrl(targetUrl()) }
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
