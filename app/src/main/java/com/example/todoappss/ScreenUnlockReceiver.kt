package com.example.todoappss

    import android.content.BroadcastReceiver
    import android.content.Context
    import android.content.Intent
    import android.util.Log

    class ScreenUnlockReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent?.action == Intent.ACTION_USER_PRESENT) {
                Log.d("ScreenUnlockReceiver", "🔓 User unlocked the screen")

                val launchIntent = Intent(context, MainActivity::class.java)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            }
        }
    }
