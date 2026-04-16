package ru.vasili4.reactivevideo.ui.entry

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.vasili4.reactivevideo.data.local.AppPreferences
import ru.vasili4.reactivevideo.ui.auth.LoginActivity
import ru.vasili4.reactivevideo.ui.main.MainActivity

class EntryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = AppPreferences(this)
        val target = if (preferences.getToken().isNullOrBlank()) {
            LoginActivity::class.java
        } else {
            MainActivity::class.java
        }

        startActivity(Intent(this, target))
        finish()
    }
}
