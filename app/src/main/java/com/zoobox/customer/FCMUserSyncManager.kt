package com.zoobox.customer

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

object FCMUserSyncManager {
    private const val TAG = "FCMUserSyncManager"
    private const val PREF_NAME = "fcm_user_sync_prefs"
    private const val KEY_LAST_POSTED_TOKEN = "last_posted_fcm_token"
    private const val KEY_LAST_POSTED_USER_ID = "last_posted_user_id"

    fun onFCMTokenOrUserIdChanged(context: Context, fcmToken: String?, userId: String?) {
        if (fcmToken.isNullOrEmpty() || userId.isNullOrEmpty()) return

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val lastToken = prefs.getString(KEY_LAST_POSTED_TOKEN, null)
        val lastUserId = prefs.getString(KEY_LAST_POSTED_USER_ID, null)

        if (fcmToken == lastToken && userId == lastUserId) {
            Log.d(TAG, "No change in FCM token or user_id, skipping POST")
            return
        }

        // POST to server
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val formBody = FormBody.Builder()
                    .add("user_id", userId)
                    .add("FCM_token", fcmToken)
                    .add("device_type", "android")
                    .build()
                val request = Request.Builder()
                    .url("https://mikmik.site/FCM_token_updater.php")
                    .post(formBody)
                    .build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                Log.d(TAG, "POST response: $responseBody")

                if (response.isSuccessful) {
                    // Save last posted values
                    prefs.edit()
                        .putString(KEY_LAST_POSTED_TOKEN, fcmToken)
                        .putString(KEY_LAST_POSTED_USER_ID, userId)
                        .apply()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error posting FCM token and user_id", e)
            }
        }
    }
} 