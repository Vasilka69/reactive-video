package ru.vasili4.reactivevideo.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.vasili4.reactivevideo.R
import ru.vasili4.reactivevideo.data.local.AppPreferences
import ru.vasili4.reactivevideo.data.model.UserCredentials
import ru.vasili4.reactivevideo.data.repository.ReactiveVideoRepository
import ru.vasili4.reactivevideo.data.network.ApiErrors
import ru.vasili4.reactivevideo.databinding.ActivityLoginBinding
import ru.vasili4.reactivevideo.ui.common.showBackendUrlDialog
import ru.vasili4.reactivevideo.ui.common.showToast
import ru.vasili4.reactivevideo.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferences: AppPreferences
    private lateinit var repository: ReactiveVideoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = AppPreferences(this)
        repository = ReactiveVideoRepository(this)

        renderBackendUrl()

        binding.buttonLogin.setOnClickListener { submit(isRegistration = false) }
        binding.buttonRegister.setOnClickListener { submit(isRegistration = true) }
        binding.buttonBackendUrl.setOnClickListener {
            showBackendUrlDialog(preferences.getBaseUrl()) { newUrl ->
                preferences.setBaseUrl(newUrl)
                renderBackendUrl()
                showToast("Backend URL сохранён")
            }
        }
    }

    private fun submit(isRegistration: Boolean) {
        val credentials = readCredentials() ?: return

        lifecycleScope.launch {
            setLoading(true)
            try {
                if (isRegistration) {
                    repository.register(credentials)
                    showToast("Регистрация успешна, выполняю вход")
                }

                val token = repository.login(credentials)
                preferences.setToken(token)
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finishAffinity()
            } catch (throwable: Throwable) {
                showToast(ApiErrors.fromThrowable(throwable))
            } finally {
                setLoading(false)
            }
        }
    }

    private fun readCredentials(): UserCredentials? {
        val login = binding.editLogin.text?.toString()?.trim().orEmpty()
        val password = binding.editPassword.text?.toString().orEmpty()

        if (login.isBlank() || password.isBlank()) {
            showToast("Введите логин и пароль")
            return null
        }

        return UserCredentials(login = login, password = password)
    }

    private fun renderBackendUrl() {
        binding.textBackendUrl.text = getString(R.string.backend_prefix, preferences.getBaseUrl())
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
        binding.buttonLogin.isEnabled = !loading
        binding.buttonRegister.isEnabled = !loading
        binding.buttonBackendUrl.isEnabled = !loading
    }
}
