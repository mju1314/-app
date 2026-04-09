package com.example.expensetracker.common

import android.app.Activity
import android.content.Intent

object AppRestarter {
    fun restart(activity: Activity) {
        val packageManager = activity.packageManager
        val intent = packageManager.getLaunchIntentForPackage(activity.packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            ?: return

        activity.startActivity(intent)
        activity.finish()
        Runtime.getRuntime().exit(0)
    }
}
