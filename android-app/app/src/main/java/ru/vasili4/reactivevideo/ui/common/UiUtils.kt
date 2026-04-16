package ru.vasili4.reactivevideo.ui.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.vasili4.reactivevideo.R
import ru.vasili4.reactivevideo.data.network.ApiFactory
import ru.vasili4.reactivevideo.databinding.DialogBackendUrlBinding
import ru.vasili4.reactivevideo.ui.auth.LoginActivity

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Activity.openLoginAndFinish() {
    startActivity(Intent(this, LoginActivity::class.java))
    finishAffinity()
}

fun Context.showBackendUrlDialog(
    currentUrl: String,
    onSaved: (String) -> Unit,
) {
    val binding = DialogBackendUrlBinding.inflate(LayoutInflater.from(this))
    binding.editBackendUrl.setText(currentUrl)

    MaterialAlertDialogBuilder(this)
        .setTitle(R.string.backend_url_title)
        .setView(binding.root)
        .setPositiveButton(R.string.save_action, null)
        .setNegativeButton(R.string.cancel_action, null)
        .create()
        .also { dialog ->
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val value = binding.editBackendUrl.text?.toString().orEmpty()
                    val normalized = ApiFactory.normalizeBaseUrl(value)
                    onSaved(normalized)
                    dialog.dismiss()
                }
            }
        }
        .show()
}
