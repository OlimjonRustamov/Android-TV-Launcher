package uz.rustamov.testlauncher

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import java.util.logging.Handler

class TvAccessibilityService : AccessibilityService() {

    fun getDefaultLauncherPackage(context: Context): String? {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }

        val res = context.packageManager.resolveActivity(intent, 0)
        return res?.activityInfo?.packageName
    }
    fun isMyAppInForeground(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val usm = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
            val time = System.currentTimeMillis()

            val stats = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                time - 1000 * 10,
                time
            )

            if (stats.isNullOrEmpty()) return false

            val recent = stats.maxByOrNull { it.lastTimeUsed } ?: return false
            return recent.packageName == packageName
        } else {
            //could not detect app in foreground
            return true
        }
    }


    private val handler by lazy { android.os.Handler(Looper.getMainLooper()) }
    private val openAppRunnable by lazy { Runnable {
        if (isMyAppInForeground()) return@Runnable
        startActivity(Intent(this, MainActivity::class.java).apply {
            addFlags(FLAG_ACTIVITY_NEW_TASK)
        })
    } }

    override fun onServiceConnected() {
        super.onServiceConnected()

        Log.d("TvAccessibilityService", "Service connected")

        val info = AccessibilityServiceInfo().apply {
            eventTypes =
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_VIEW_FOCUSED

            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC

            // We want events from all apps
            notificationTimeout = 100

            // Important: request permission to perform global actions
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }

        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        Log.d("TvAccessibilityService", "event: ${event.packageName}")

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return

            val myPkg = "uz.rustamov.testlauncher"

            val defaultLauncher = getDefaultLauncherPackage(this)

            if (pkg == defaultLauncher && pkg != myPkg) {
                Log.d("TvAccessibilityService", "User returned to default launcher: ")
                handler.removeCallbacks(openAppRunnable)
                handler.postDelayed(openAppRunnable, 200)
            }
        }
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return false

        // Example: intercept long-press BACK and go HOME
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.isLongPress) {
            performGlobalAction(GLOBAL_ACTION_HOME)
            return true // consumed
        }

//        Log.d("TvAccessibilityService", "onKeyEvent $event")

        // Example: map MENU button to open your launcher, etc.
        return false // let others handle normally
    }

    override fun onInterrupt() {
        // Called when the system wants to interrupt your service

        Log.d("TvAccessibilityService", "Interrupted")
    }
}

fun Context.isAccessibilityServiceEnabled(
    service: Class<out AccessibilityService>
): Boolean {
    val expectedComponent = ComponentName(this, service)

    val enabledServices =
        Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServices)

    for (component in colonSplitter) {
        if (ComponentName.unflattenFromString(component) == expectedComponent) {
            return true
        }
    }
    return false
}