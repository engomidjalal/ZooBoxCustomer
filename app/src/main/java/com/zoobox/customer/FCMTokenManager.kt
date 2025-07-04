package com.zoobox.customer

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.webkit.CookieManager

object FCMTokenManager {
    private const val TAG = "FCMTokenManager"
    private const val PREF_NAME = "fcm_prefs"
    private const val KEY_FCM_TOKEN = "fcm_token"

    /**
     * Get the stored FCM token or fetch a new one if not available
     */
    suspend fun getFCMToken(context: Context): String? {
        return try {
            // Try to get from preferences first
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            var token = prefs.getString(KEY_FCM_TOKEN, null)

            // If not found, fetch new token
            if (token == null) {
                token = withContext(Dispatchers.IO) {
                    try {
                        FirebaseMessaging.getInstance().token.await()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error getting FCM token", e)
                        null
                    }
                }

                // Save the new token
                token?.let { saveToken(context, it) }
            }

            token
        } catch (e: Exception) {
            Log.e(TAG, "Error in getFCMToken", e)
            null
        }
    }

    /**
     * Save FCM token to preferences and as a cookie
     */
    fun saveToken(context: Context, token: String) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
            Log.d(TAG, "FCM token saved successfully: $token")
            saveFCMTokenAsCookie(token)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving FCM token", e)
        }
    }

    /**
     * Save the FCM token as a cookie for the WebView domain
     */
    private fun saveFCMTokenAsCookie(token: String) {
        try {
            val cookieManager = CookieManager.getInstance()
            val cookieString = "FCM_token=$token; path=/; domain=mikmik.site; max-age=31536000" // 1 year
            cookieManager.setCookie("https://mikmik.site", cookieString)
            cookieManager.flush()
            Log.d(TAG, "FCM token set as cookie: $cookieString")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting FCM token as cookie", e)
        }
    }

    /**
     * Clear stored FCM token (useful when logging out)
     */
    fun clearFCMToken(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove(KEY_FCM_TOKEN).apply()
            Log.d(TAG, "FCM token cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing FCM token", e)
        }
    }
}