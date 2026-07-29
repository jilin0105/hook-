package com.reversing.nativecryptohook

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * LSPosed/Xposed 模块入口
 * 目标包名: com.kaiyu.guangying
 */
class HookEntry : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // 只 hook 目标 App
        if (lpparam.packageName != "com.kaiyu.guangying") return

        XposedBridge.log("[NativeCryptoHook] 🚀 模块已加载，目标: com.kaiyu.guangying")

        // 确保模块只在目标进程初始化后执行（延迟 hook）
        try {
            // hook NativeSecurity Java 层方法
            NativeSecurityHook.hook(lpparam.classLoader)
        } catch (e: Exception) {
            XposedBridge.log("[NativeCryptoHook] ❌ NativeSecurityHook 失败: $e")
        }

        try {
            // hook OkHttp 拦截器
            InterceptorHook.hook(lpparam.classLoader)
        } catch (e: Exception) {
            XposedBridge.log("[NativeCryptoHook] ❌ InterceptorHook 失败: $e")
        }

        XposedBridge.log("[NativeCryptoHook] ✅ 所有 hook 注册完成！日志输出到 /sdcard/NativeCryptoHook/")
        FileLogger.log("HookEntry", "✅ 模块加载完成，开始记录...")
    }
}