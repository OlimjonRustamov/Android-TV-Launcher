package uz.rustamov.testlauncher.component

import android.content.Intent
import uz.rustamov.testlauncher.MainActivity

fun MainActivity.loadApps() : List<InstalledApp>{
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }

    return packageManager.queryIntentActivities(intent, 0)
        .map {
            InstalledApp(
                name = it.loadLabel(packageManager).toString(),
                packageName = it.activityInfo.packageName,
                icon = it.loadIcon(packageManager)
            )
        }
        .sortedBy { it.name.lowercase() }
        //do not show app itself
        .filter { it.packageName != packageName }
}