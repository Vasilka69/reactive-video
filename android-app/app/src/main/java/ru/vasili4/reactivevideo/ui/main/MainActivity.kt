package ru.vasili4.reactivevideo.ui.main

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import ru.vasili4.reactivevideo.R
import ru.vasili4.reactivevideo.data.local.AppPreferences
import ru.vasili4.reactivevideo.data.model.FileItem
import ru.vasili4.reactivevideo.data.model.FileType
import ru.vasili4.reactivevideo.data.model.UserResponse
import ru.vasili4.reactivevideo.data.network.ApiErrors
import ru.vasili4.reactivevideo.data.repository.ReactiveVideoRepository
import ru.vasili4.reactivevideo.databinding.ActivityMainBinding
import ru.vasili4.reactivevideo.ui.common.openLoginAndFinish
import ru.vasili4.reactivevideo.ui.common.showBackendUrlDialog
import ru.vasili4.reactivevideo.ui.common.showToast
import ru.vasili4.reactivevideo.ui.preview.image.ImagePreviewActivity
import ru.vasili4.reactivevideo.ui.preview.text.TextFileActivity
import ru.vasili4.reactivevideo.ui.preview.video.VideoPreviewActivity
import ru.vasili4.reactivevideo.ui.recognition.ImageRecognitionActivity

class MainActivity : AppCompatActivity(), FileActionListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferences: AppPreferences
    private lateinit var repository: ReactiveVideoRepository
    private lateinit var fileAdapter: FileAdapter

    private var selectedBucket: String? = null
    private var lastLoadedBuckets: List<String> = emptyList()

    private val openDocument = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) {
            return@registerForActivityResult
        }
        val bucket = selectedBucket
        if (bucket.isNullOrBlank()) {
            showToast(getString(R.string.bucket_required))
            return@registerForActivityResult
        }
        uploadFile(uri, bucket)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = AppPreferences(this)
        repository = ReactiveVideoRepository(this)
        fileAdapter = FileAdapter(this)

        setupToolbar()
        setupRecycler()
        setupBucketDropdown()
        setupActions()
        loadDashboard(showBlockingLoader = true)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setOnMenuItemClickListener(::onToolbarMenuClick)
    }

    private fun setupRecycler() {
        binding.recyclerFiles.adapter = fileAdapter
        binding.recyclerFiles.isNestedScrollingEnabled = false
    }

    private fun setupBucketDropdown() {
        binding.autoSelectedBucket.setOnItemClickListener { _, _, position, _ ->
            selectedBucket = lastLoadedBuckets.getOrNull(position)
        }
    }

    private fun setupActions() {
        binding.swipeRefresh.setOnRefreshListener { loadDashboard(showBlockingLoader = false) }
        binding.buttonCreateBucket.setOnClickListener { createBucket() }
        binding.buttonUploadFile.setOnClickListener {
            if (selectedBucket.isNullOrBlank()) {
                showToast(getString(R.string.bucket_required))
                return@setOnClickListener
            }
            runCatching {
                openDocument.launch(arrayOf("*/*"))
            }.onFailure {
                showToast(getString(R.string.pick_file_error))
            }
        }
    }

    private fun onToolbarMenuClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> loadDashboard(showBlockingLoader = false)
            R.id.action_settings -> {
                showBackendUrlDialog(preferences.getBaseUrl()) { newUrl ->
                    preferences.setBaseUrl(newUrl)
                    renderBackendLabel()
                    loadDashboard(showBlockingLoader = true)
                    showToast("Backend URL обновлён")
                }
            }
            R.id.action_logout -> {
                preferences.clearToken()
                openLoginAndFinish()
            }
        }
        return true
    }

    private fun loadDashboard(showBlockingLoader: Boolean) {
        lifecycleScope.launch {
            if (showBlockingLoader) {
                binding.progressBar.visibility = View.VISIBLE
            }
            binding.emptyState.visibility = View.GONE
            binding.contentGroup.visibility = if (showBlockingLoader) View.INVISIBLE else View.VISIBLE
            binding.swipeRefresh.isRefreshing = !showBlockingLoader

            try {
                val dashboard = coroutineScope {
                    val userDeferred = async { repository.getUser() }
                    val bucketsDeferred = async { repository.getBuckets() }
                    val filesDeferred = async { repository.getFiles() }
                    Triple(userDeferred.await(), bucketsDeferred.await(), filesDeferred.await())
                }
                renderUser(dashboard.first)
                renderBuckets(dashboard.second)
                renderFiles(dashboard.third)
            } catch (throwable: Throwable) {
                if (ApiErrors.isUnauthorized(throwable)) {
                    preferences.clearToken()
                    showToast("Сессия истекла, войдите снова")
                    openLoginAndFinish()
                    return@launch
                }
                showToast(ApiErrors.fromThrowable(throwable))
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                binding.contentGroup.visibility = View.VISIBLE
            }
        }
    }

    private fun renderUser(user: UserResponse) {
        binding.textUser.text = getString(R.string.user_prefix, user.login)
        renderBackendLabel()
    }

    private fun renderBackendLabel() {
        binding.textBackend.text = getString(R.string.backend_prefix, preferences.getBaseUrl())
    }

    private fun renderBuckets(buckets: List<String>) {
        lastLoadedBuckets = buckets
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, buckets)
        binding.autoSelectedBucket.setAdapter(adapter)

        selectedBucket = when {
            selectedBucket in buckets -> selectedBucket
            buckets.isNotEmpty() -> buckets.first()
            else -> null
        }
        binding.autoSelectedBucket.setText(selectedBucket.orEmpty(), false)
        binding.textNoBuckets.visibility = if (buckets.isEmpty()) View.VISIBLE else View.GONE
        renderBucketDeleteButtons(buckets)
    }

    private fun renderBucketDeleteButtons(buckets: List<String>) {
        binding.bucketDeleteContainer.removeAllViews()
        buckets.forEach { bucketName ->
            val button = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
                text = bucketName
                setOnClickListener { confirmDeleteBucket(bucketName) }
            }
            val params = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                rightMargin = resources.getDimensionPixelSize(R.dimen.spacing_small)
                bottomMargin = resources.getDimensionPixelSize(R.dimen.spacing_small)
            }
            binding.bucketDeleteContainer.addView(button, params)
        }
    }

    private fun renderFiles(files: List<FileItem>) {
        fileAdapter.updateItems(files)
        binding.emptyState.visibility = if (files.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun createBucket() {
        val name = binding.editBucketName.text?.toString()?.trim().orEmpty()
        if (name.isBlank()) {
            showToast("Введите имя бакета")
            return
        }

        lifecycleScope.launch {
            try {
                repository.createBucket(name)
                binding.editBucketName.setText("")
                showToast("Бакет создан")
                loadDashboard(showBlockingLoader = false)
            } catch (throwable: Throwable) {
                handleActionError(throwable)
            }
        }
    }

    private fun confirmDeleteBucket(bucketName: String) {
        MaterialAlertDialogBuilder(this)
            .setMessage(getString(R.string.delete_bucket_confirm, bucketName))
            .setPositiveButton(R.string.delete_action) { _, _ ->
                lifecycleScope.launch {
                    try {
                        repository.deleteBucket(bucketName)
                        if (selectedBucket == bucketName) {
                            selectedBucket = null
                        }
                        showToast("Бакет удалён")
                        loadDashboard(showBlockingLoader = false)
                    } catch (throwable: Throwable) {
                        handleActionError(throwable)
                    }
                }
            }
            .setNegativeButton(R.string.cancel_action, null)
            .show()
    }

    private fun uploadFile(uri: Uri, bucket: String) {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            try {
                repository.uploadFile(uri, bucket)
                showToast("Файл загружен")
                loadDashboard(showBlockingLoader = false)
            } catch (throwable: Throwable) {
                handleActionError(throwable)
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onPreview(item: FileItem) {
        when (item.type) {
            FileType.IMAGE, FileType.GIF -> startActivity(ImagePreviewActivity.newIntent(this, item.fileId, item.filePath))
            FileType.VIDEO -> startActivity(VideoPreviewActivity.newIntent(this, item.fileId, item.filePath))
            FileType.TEXT -> onOpenText(item)
            FileType.OTHER -> onDownload(item)
        }
    }

    override fun onRecognize(item: FileItem, useCache: Boolean) {
        startActivity(ImageRecognitionActivity.newIntent(this, item.fileId, item.filePath, useCache))
    }

    override fun onOpenText(item: FileItem) {
        startActivity(TextFileActivity.newIntent(this, item.fileId, item.filePath))
    }

    override fun onDownload(item: FileItem) {
        lifecycleScope.launch {
            try {
                val path = repository.saveFileToDownloads(item)
                showToast(getString(R.string.download_success, path))
            } catch (throwable: Throwable) {
                handleActionError(throwable)
            }
        }
    }

    override fun onDelete(item: FileItem) {
        MaterialAlertDialogBuilder(this)
            .setMessage(getString(R.string.delete_file_confirm, item.fileName))
            .setPositiveButton(R.string.delete_action) { _, _ ->
                lifecycleScope.launch {
                    try {
                        repository.deleteFile(item.fileId)
                        showToast("Файл удалён")
                        loadDashboard(showBlockingLoader = false)
                    } catch (throwable: Throwable) {
                        handleActionError(throwable)
                    }
                }
            }
            .setNegativeButton(R.string.cancel_action, null)
            .show()
    }

    private fun handleActionError(throwable: Throwable) {
        if (ApiErrors.isUnauthorized(throwable)) {
            preferences.clearToken()
            showToast("Сессия истекла, войдите снова")
            openLoginAndFinish()
            return
        }
        showToast(ApiErrors.fromThrowable(throwable))
    }
}
