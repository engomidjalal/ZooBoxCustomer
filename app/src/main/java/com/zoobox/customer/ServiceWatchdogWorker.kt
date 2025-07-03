package com.zoobox.customer

import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class ServiceWatchdogWorker(context: android.content.Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        Log.d("ServiceWatchdogWorker", "ServiceWatchdogWorker is now a stub. CookieSenderService removed.")
        return Result.success()
    }
}