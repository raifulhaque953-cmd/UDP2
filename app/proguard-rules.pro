# Add project specific ProGuard rules here.
# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep data classes used by the capture engine
-keep class com.pocketshark.app.data.model.** { *; }
-keep class com.pocketshark.app.capture.** { *; }

# Keep VpnService
-keep class com.pocketshark.app.capture.VpnCaptureService { *; }
