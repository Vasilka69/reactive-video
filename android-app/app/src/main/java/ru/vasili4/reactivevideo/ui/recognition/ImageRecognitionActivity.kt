package ru.vasili4.reactivevideo.ui.recognition

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.vasili4.reactivevideo.R
import ru.vasili4.reactivevideo.data.network.ApiErrors
import ru.vasili4.reactivevideo.data.repository.ReactiveVideoRepository
import ru.vasili4.reactivevideo.databinding.ActivityImageRecognitionBinding
import ru.vasili4.reactivevideo.ui.common.showToast

class ImageRecognitionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageRecognitionBinding
    private lateinit var repository: ReactiveVideoRepository

    private var fileId: String = ""
    private var initialUseCache: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageRecognitionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ReactiveVideoRepository(this)
        fileId = intent.getStringExtra(EXTRA_FILE_ID).orEmpty()
        initialUseCache = intent.getBooleanExtra(EXTRA_USE_CACHE, false)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.title = intent.getStringExtra(EXTRA_FILE_PATH)?.substringAfterLast('/')
            ?: getString(R.string.recognition_title)

        binding.buttonRecognize.setOnClickListener { loadRecognition(useCache = false) }
        binding.buttonRecognizeCached.setOnClickListener { loadRecognition(useCache = true) }
        binding.buttonToggleAnnotations.setOnClickListener {
            val visible = binding.annotationOverlay.toggleVisibility()
            showToast(getString(if (visible) R.string.annotations_visible else R.string.annotations_hidden))
        }

        loadImage()
        loadRecognition(initialUseCache)
    }

    private fun loadImage() {
        lifecycleScope.launch {
            try {
                val bytes = repository.getFileBytes(fileId)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap == null) {
                    showToast("Не удалось декодировать изображение")
                    return@launch
                }
                val maxWidth = resources.displayMetrics.widthPixels - resources.getDimensionPixelSize(R.dimen.image_screen_padding)
                val scale = minOf(1f, maxWidth / bitmap.width.toFloat())
                val width = (bitmap.width * scale).toInt()
                val height = (bitmap.height * scale).toInt()

                val params = FrameLayout.LayoutParams(width, height)
                binding.imageRecognition.layoutParams = params
                binding.annotationOverlay.layoutParams = FrameLayout.LayoutParams(width, height)
                binding.imageRecognition.setImageBitmap(bitmap)
                binding.annotationOverlay.setSourceSize(bitmap.width, bitmap.height)
            } catch (throwable: Throwable) {
                showToast(ApiErrors.fromThrowable(throwable))
            }
        }
    }

    private fun loadRecognition(useCache: Boolean) {
        lifecycleScope.launch {
            binding.progressBar.visibility = android.view.View.VISIBLE
            try {
                val response = repository.getRecognition(fileId, useCache)
                binding.annotationOverlay.setAnnotations(response.labels)
                showToast(if (useCache) "Кэшированная разметка получена" else "Разметка построена")
            } catch (throwable: Throwable) {
                showToast(ApiErrors.fromThrowable(throwable))
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }

    companion object {
        private const val EXTRA_FILE_ID = "extra_file_id"
        private const val EXTRA_FILE_PATH = "extra_file_path"
        private const val EXTRA_USE_CACHE = "extra_use_cache"

        fun newIntent(context: Context, fileId: String, filePath: String, useCache: Boolean): Intent {
            return Intent(context, ImageRecognitionActivity::class.java)
                .putExtra(EXTRA_FILE_ID, fileId)
                .putExtra(EXTRA_FILE_PATH, filePath)
                .putExtra(EXTRA_USE_CACHE, useCache)
        }
    }
}
