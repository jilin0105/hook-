package com.reversing.nativecryptohook

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

/**
 * Hook OkHttp 请求拦截器，捕获加密头信息
 * 拦截器类: i.a.c.e（第二个拦截器，负责加签）
 * 拦截器类: i.a.c.d（第一个拦截器，检测代理/VPN）
 */
object InterceptorHook {

    private const val TAG = "Interceptor"

    fun hook(classLoader: ClassLoader) {
        hookEncryptInterceptor(classLoader)
        hookDetectInterceptor(classLoader)
    }

    /** 拦截第二个拦截器 i.a.c.e — 添加加密签名头 */
    private fun hookEncryptInterceptor(classLoader: ClassLoader) {
        try {
            val clazz = XposedHelpers.findClass("i.a.c.e", classLoader)
            XposedBridge.log("[$TAG] 找到加密拦截器: $clazz")

            // intercept(Chain) → Response
            XposedHelpers.findAndHookMethod(clazz, "intercept",
                okhttp3.Interceptor.Chain::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
                        try {
                            val chain = param.args[0]
                            // 通过反射获取原始 Request
                            val request = chain?.let {
                                XposedHelpers.callMethod(it, "request")
                            }
                            val url = request?.let {
                                XposedHelpers.callMethod(it, "url")
                            }?.toString() ?: "unknown"
                            val method = request?.let {
                                XposedHelpers.callMethod(it, "method")
                            }?.toString() ?: "?"

                            // 获取请求头（在拦截器修改之前）
                            val headers = request?.let {
                                XposedHelpers.callMethod(it, "headers")
                            }
                            val headerStr = headers?.toString() ?: ""

                            FileLogger.log(TAG, "🌐 $method $url")
                            FileLogger.log(TAG, "📋 原始请求头:\n$headerStr")

                        } catch (e: Exception) {
                            FileLogger.log(TAG, "before hook error: $e")
                        }
                    }

                    override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                        try {
                            val response = param.result
                            // 获取修改后的请求（从 Response 反查 Request）
                            val request = response?.let {
                                XposedHelpers.callMethod(it, "request")
                            }
                            val headers = request?.let {
                                XposedHelpers.callMethod(it, "headers")
                            }

                            val headersMap = mutableMapOf<String, String>()
                            // 遍历 header
                            for (i in 0 until (headers?.let {
                                XposedHelpers.callMethod(it, "size")
                            } as? Int ?: 0)) {
                                val name = headers?.let {
                                    XposedHelpers.callMethod(it, "name", i)
                                }?.toString() ?: ""
                                val value = headers?.let {
                                    XposedHelpers.callMethod(it, "value", i)
                                }?.toString() ?: ""
                                headersMap[name] = value
                            }

                            // 打印关键加密头
                            val signHeaders = listOf(
                                "app-api-verify-time",
                                "app-api-verify-sign",
                                "app-user-token",
                                "app-version-code",
                                "app-user-device-id"
                            )
                            for (h in signHeaders) {
                                if (headersMap.containsKey(h)) {
                                    FileLogger.log(TAG, "📍 $h = ${headersMap[h]}")
                                }
                            }
                            FileLogger.log(TAG, "📋 完整请求头 (${headersMap.size}项):\n$headersMap")

                        } catch (e: Exception) {
                            FileLogger.log(TAG, "after hook error: $e")
                        }
                    }
                }
            )

            XposedBridge.log("[$TAG] ✅ 加密拦截器 hook 成功！")

        } catch (e: Exception) {
            XposedBridge.log("[$TAG] ❌ 加密拦截器 hook 失败: $e")
            FileLogger.log(TAG, "❌ 加密拦截器 hook 失败: $e")
        }
    }

    /** 拦截第一个拦截器 i.a.c.d — 代理/VPN检测（记录被拦截的请求） */
    private fun hookDetectInterceptor(classLoader: ClassLoader) {
        try {
            val clazz = XposedHelpers.findClass("i.a.c.d", classLoader)

            // b() - VPN 检测
            XposedHelpers.findAndHookMethod(clazz, "b",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                        val result = param.result
                        FileLogger.log(TAG, "👁️ i.a.c.d.b() VPN检测 = $result")
                    }
                }
            )

            // c() - 代理检测
            XposedHelpers.findAndHookMethod(clazz, "c",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                        val result = param.result
                        FileLogger.log(TAG, "👁️ i.a.c.d.c() 代理检测 = $result")
                    }
                }
            )

            // intercept() 
            XposedHelpers.findAndHookMethod(clazz, "intercept",
                okhttp3.Interceptor.Chain::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
                        FileLogger.log(TAG, "👁️ i.a.c.d.intercept() 被调用")
                    }
                }
            )

            XposedBridge.log("[$TAG] ✅ 检测拦截器 hook 成功！")

        } catch (e: Exception) {
            XposedBridge.log("[$TAG] ❌ 检测拦截器 hook 失败: $e")
        }
    }
}