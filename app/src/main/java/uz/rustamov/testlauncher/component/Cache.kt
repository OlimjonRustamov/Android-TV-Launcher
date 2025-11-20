package uz.rustamov.testlauncher.component

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class Cache(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_cache", Context.MODE_PRIVATE)

    fun saveLastOpenedApp(packageName: String) {
        prefs.edit { putString("last_opened_app", packageName) }
    }

    fun getLastOpenedApp(): String? {
        return prefs.getString("last_opened_app", null)
    }



    // Clear all cache
    fun clear() {
        prefs.edit { clear() }
    }
}
