package uz.rustamov.testlauncher

import android.app.Activity
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import uz.rustamov.testlauncher.component.Cache
import uz.rustamov.testlauncher.component.InstalledApp
import uz.rustamov.testlauncher.component.ItemInstalledApp
import uz.rustamov.testlauncher.component.loadApps
import uz.rustamov.testlauncher.ui.theme.testlauncherTheme


@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalTvMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val cache: Cache by lazy { Cache(this) }
    private var installedApps: List<InstalledApp> = emptyList()
    private val defaultLauncherHelper by lazy { DefaultLauncherHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var lastOpenedApp by remember { mutableStateOf(cache.getLastOpenedApp()) }
            var apps by remember { mutableStateOf(installedApps) }
            LifecycleResumeEffect(Unit) {
                apps = loadApps()
                onPauseOrDispose {}
            }
            testlauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {

                        lastOpenedApp?.let { lastOpenedApp ->
                            item(span = { GridItemSpan(6) }) {
                                Text(
                                    "Last opened App",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            item {
                                installedApps.find { it.packageName == lastOpenedApp }?.let { ItemInstalledApp(it) {
                                    openApp(it.packageName)
                                } }
                            }
                            item(span = { GridItemSpan(5) }) {}
                        }
                        item(span = { GridItemSpan(6) }) {
                            Text(
                                "All installed apps, choose one to launch",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        items(apps.size) { index ->
                            ItemInstalledApp(apps[index]) {
                                openApp(apps[index].packageName)
                                cache.saveLastOpenedApp(apps[index].packageName)
                                lastOpenedApp = apps[index].packageName
                            }
                        }
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback {}
    }

    private fun openApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        installedApps = loadApps()

        validateDefaultLauncher()
    }
    private fun validateDefaultLauncher() {
        if (!defaultLauncherHelper.isDefaultLauncher() && defaultLauncherHelper.canRequestDefaultLauncher()) {
            val intent = defaultLauncherHelper.requestDefaultLauncherIntent()
            @Suppress("DEPRECATION")
            if (intent != null) startActivityForResult(intent, 123)
            return
        }
        if (isAccessibilityServiceEnabled(TvAccessibilityService::class.java)) {
            Log.d("TvAccessibilityService", "Accessibility service is enabled")
        } else {
            AlertDialog.Builder(this)
                .setTitle("Please allow accessibility service to you the app with all functionalities")
                .setPositiveButton("Go to settings") { dialog, which ->
                    try {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(this, "Failed to open settings", Toast.LENGTH_SHORT).show()
                    }
                }
                .show()
        }
    }

}