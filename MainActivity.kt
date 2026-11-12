package ma.hichammesbah.labovirtuel

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import java.io.File

/**
 * TP de physique en 3D — Pr Hicham MESBAH.
 * La simulation (HTML + three.js) est chargée depuis les assets de l'application
 * via WebViewAssetLoader, ce qui garde localStorage (relevés et compte rendu).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private val home = "https://appassets.androidplatform.net/assets/index.html"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.parseColor("#151A21")
        window.navigationBarColor = Color.parseColor("#0F1318")

        webView = WebView(this)
        webView.setBackgroundColor(Color.parseColor("#0F1318"))
        setContentView(webView)

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            mediaPlaybackRequiresUserGesture = true
            cacheMode = WebSettings.LOAD_DEFAULT
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = false
            textZoom = 100
        }

        webView.webViewClient = object : WebViewClientCompat() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
                assetLoader.shouldInterceptRequest(request.url)

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url
                if (url.host == "appassets.androidplatform.net") return false
                // Liens externes : ouverts dans le navigateur du téléphone
                startActivity(Intent(Intent.ACTION_VIEW, url))
                return true
            }
        }
        webView.addJavascriptInterface(AndroidBridge(), "AndroidApp")

        if (savedInstanceState != null) webView.restoreState(savedInstanceState)
        else webView.loadUrl(home)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onPause() { webView.onPause(); super.onPause() }
    override fun onResume() { super.onResume(); webView.onResume() }
    override fun onDestroy() { webView.destroy(); super.onDestroy() }

    /** Pont appelé par la page : enregistre le compte rendu dans Téléchargements/Pr_Hicham_MESBAH. */
    inner class AndroidBridge {
        @JavascriptInterface
        fun saveFile(filename: String, content: String): Boolean {
            val name = filename.replace(Regex("[\\\\/:*?\"<>|]"), "_")
            return try {
                val uri = writeDownload(name, content.toByteArray(Charsets.UTF_8))
                runOnUiThread {
                    Toast.makeText(this@MainActivity, getString(R.string.saved, name), Toast.LENGTH_LONG).show()
                    shareFile(uri, name)
                }
                true
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, getString(R.string.save_error, e.message ?: ""), Toast.LENGTH_LONG).show()
                }
                false
            }
        }
    }

    private fun writeDownload(name: String, bytes: ByteArray): Uri {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, name)
                put(MediaStore.Downloads.MIME_TYPE, "text/html")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Pr_Hicham_MESBAH")
            }
            val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("MediaStore indisponible")
            contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            return uri
        }
        // Android 8–9 : dossier de l'application, partagé via FileProvider
        val dir = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Pr_Hicham_MESBAH").apply { mkdirs() }
        val file = File(dir, name)
        file.writeBytes(bytes)
        return FileProvider.getUriForFile(this, "$packageName.files", file)
    }

    private fun shareFile(uri: Uri, name: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(send, getString(R.string.share_title)))
    }
}
