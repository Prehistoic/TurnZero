package com.turnzero.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.turnzero.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class UpdateManager(private val context: Context) {

    sealed class UpdateStatus {
        data object Idle : UpdateStatus()
        data object Checking : UpdateStatus()
        data class Downloading(val progress: Int) : UpdateStatus()
        data object UpToDate : UpdateStatus()
        data class UpdateAvailable(val version: String, val downloadUrl: String) : UpdateStatus()
        data class ReadyToInstall(val apkFile: File) : UpdateStatus()
        data class Error(val message: String) : UpdateStatus()
    }

    companion object {
        private const val TAG = "TZ/UpdateManager"
        private const val GITHUB_API_URL = "https://api.github.com/repos/Prehistoic/TurnZero/releases/latest"
    }

    suspend fun checkForUpdate(): UpdateStatus = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                connection.disconnect()
                if (responseCode == 404) {
                    return@withContext UpdateStatus.UpToDate
                }
                return@withContext UpdateStatus.Error("Server error: $responseCode")
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val tagName = json.getString("tag_name")
            val latestVersion = tagName.removePrefix("v")

            val assets = json.getJSONArray("assets")
            var downloadUrl = ""
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.getString("name")
                if (name.endsWith(".apk") && name.contains("debug", ignoreCase = true).not()) {
                    downloadUrl = asset.getString("browser_download_url")
                    break
                }
            }

            if (downloadUrl.isEmpty() && assets.length() > 0) {
                downloadUrl = assets.getJSONObject(0).getString("browser_download_url")
            }

            connection.disconnect()

            val currentVersion = BuildConfig.VERSION_NAME
            if (compareVersions(latestVersion, currentVersion) > 0) {
                UpdateStatus.UpdateAvailable(latestVersion, downloadUrl)
            } else {
                UpdateStatus.UpToDate
            }
        } catch (e: Exception) {
            Log.e(TAG, "Update check failed", e)
            UpdateStatus.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun downloadUpdate(
        downloadUrl: String,
        onProgress: suspend (Int) -> Unit = {},
    ): UpdateStatus = withContext(Dispatchers.IO) {
        try {
            val url = URL(downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 30000

            val fileSize = connection.contentLength

            val updatesDir = File(context.cacheDir, "updates")
            updatesDir.mkdirs()
            val apkFile = File(updatesDir, "update.apk")

            connection.inputStream.use { inputStream ->
                FileOutputStream(apkFile).use { outputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytesRead: Long = 0
                    var lastPercent = -1

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        if (fileSize > 0) {
                            val percent = downloadPercent(totalBytesRead, fileSize.toLong())
                            if (percent != lastPercent) {
                                lastPercent = percent
                                onProgress(percent)
                            }
                        }
                    }
                }
            }
            connection.disconnect()

            UpdateStatus.ReadyToInstall(apkFile)
        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            UpdateStatus.Error(e.message ?: "Download failed")
        }
    }

    fun installApk(apkFile: File) {
        val apkUri = FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            apkFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLen = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLen) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1.compareTo(p2)
        }
        return 0
    }
}

/** Percentage of a download that is complete, or `0` while the total size is unknown. */
internal fun downloadPercent(bytesRead: Long, totalBytes: Long): Int {
    if (totalBytes <= 0) return 0
    return ((bytesRead * 100) / totalBytes).toInt().coerceIn(0, 100)
}
