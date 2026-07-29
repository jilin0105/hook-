package com.reversing.nativecryptohook

import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 文件日志器：把 hook 到的数据写到 /sdcard/NativeCryptoHook/ 下
 */
object FileLogger {

    private val TAG = "NativeCryptoHook"
    private val DATE_FMT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val logDir = File("/sdcard/NativeCryptoHook/")

    init {
        try {
            if (!logDir.exists()) logDir.mkdirs()
            // 写入 .nomedia 防止相册显示
            File(logDir, ".nomedia").createNewFile()
        } catch (e: Exception) {
            Log.e(TAG, "无法创建日志目录", e)
        }
    }

    @Synchronized
    fun log(tag: String, message: String) {
        val line = "[${DATE_FMT.format(Date())}] [$tag] $message"
        Log.d(TAG, line)
        try {
            val file = File(logDir, "${tag}.log")
            file.appendText(line + "\n")
        } catch (e: Exception) {
            Log.e(TAG, "写日志失败", e)
        }
    }

    /** 记录二进制数据的 hex */
    fun logHex(tag: String, label: String, data: ByteArray?) {
        if (data == null) {
            log(tag, "$label = null")
            return
        }
        val hex = data.joinToString("") { "%02x".format(it) }
        log(tag, "$label (${data.size}B) = $hex")
        // 也尝试转 UTF-8 显示
        try {
            val utf8 = String(data, Charsets.UTF_8)
            if (utf8.all { it.isLetterOrDigit() || it in "+/=?&.%:-_" }) {
                log(tag, "$label (UTF8) = $utf8")
            }
        } catch (_: Exception) {}
    }
}