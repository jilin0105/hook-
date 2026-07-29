package com.reversing.nativecryptohook

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.util.Base64

/**
 * Hook net.ad.util.NativeSecurity 的所有 native 方法
 * 捕获密钥、IV、加解密入参出参
 */
object NativeSecurityHook {

    private const val TAG = "NativeSecurity"
    private const val CLASS = "net.ad.util.NativeSecurity"

    fun hook(classLoader: ClassLoader) {
        try {
            val clazz = XposedHelpers.findClass(CLASS, classLoader)
            XposedBridge.log("[$TAG] 找到目标类: $clazz")

            //───── 1. getSecurityKey() ─────
            XposedHelpers.findAndHookMethod(clazz, "getSecurityKey",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val key = param.result?.toString() ?: "null"
                        FileLogger.log(TAG, "📌 getSecurityKey() = $key")
                        XposedBridge.log("[$TAG] 📌 getSecurityKey() = $key")
                    }
                }
            )

            //───── 2. getSecurityIv() ─────
            XposedHelpers.findAndHookMethod(clazz, "getSecurityIv",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val iv = param.result?.toString() ?: "null"
                        FileLogger.log(TAG, "📌 getSecurityIv() = $iv")
                        XposedBridge.log("[$TAG] 📌 getSecurityIv() = $iv")
                    }
                }
            )

            //───── 3. getBaseUrl() ─────
            XposedHelpers.findAndHookMethod(clazz, "getBaseUrl",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val url = param.result?.toString() ?: "null"
                        FileLogger.log(TAG, "📌 getBaseUrl() = $url")
                        XposedBridge.log("[$TAG] 📌 getBaseUrl() = $url")
                    }
                }
            )

            //───── 4. encrypt(String data) → String ─────
            XposedHelpers.findAndHookMethod(clazz, "encrypt",
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val input = param.args[0]?.toString() ?: "null"
                        FileLogger.log(TAG, "🔐 encrypt() 入参: $input")
                        XposedBridge.log("[$TAG] 🔐 encrypt() 入参: $input")
                    }
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val output = param.result?.toString() ?: "null"
                        FileLogger.log(TAG, "🔐 encrypt() 返回: $output")
                        XposedBridge.log("[$TAG] 🔐 encrypt() 返回: $output")
                    }
                }
            )

            //───── 5. decrypt(String encrypted) → String ─────
            XposedHelpers.findAndHookMethod(clazz, "decrypt",
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val input = param.args[0]?.toString() ?: "null"
                        FileLogger.log(TAG, "🔓 decrypt() 入参: $input")
                        XposedBridge.log("[$TAG] 🔓 decrypt() 入参: $input")
                    }
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val output = param.result?.toString() ?: "null"
                        FileLogger.log(TAG, "🔓 decrypt() 返回: $output")
                        XposedBridge.log("[$TAG] 🔓 decrypt() 返回: $output")
                    }
                }
            )

            //───── 6. initBoxConfig(Context) ─────
            XposedHelpers.findAndHookMethod(clazz, "initBoxConfig",
                android.content.Context::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        FileLogger.log(TAG, "⚙️ initBoxConfig() 被调用")
                        // 打印调用栈
                        val sw = java.io.StringWriter()
                        val pw = java.io.PrintWriter(sw)
                        Throwable().printStackTrace(pw)
                        FileLogger.log(TAG, "调用栈:\n${sw}")
                    }
                    override fun afterHookedMethod(param: MethodHookParam) {
                        FileLogger.log(TAG, "⚙️ initBoxConfig() 返回")
                    }
                }
            )

            XposedBridge.log("[$TAG] ✅ NativeSecurity 全部 hook 成功！")

        } catch (e: Exception) {
            XposedBridge.log("[$TAG] ❌ hook 失败: $e")
            FileLogger.log(TAG, "❌ hook 失败: $e")
        }
    }
}