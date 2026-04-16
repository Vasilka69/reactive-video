package ru.vasili4.reactivevideo.ui.preview.image

import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.vasili4.reactivevideo.R
import ru.vasili4.reactivevideo.data.model.FileType
import ru.vasili4.reactivevideo.data.network.ApiErrors
import ru.vasili4.reactivevideo.data.repository.ReactiveVideoRepository
import ru.vasili4.reactivevideo.databinding.ActivityImagePreviewBinding
import ru.vasili4.reactivevideo.ui.common.showToast

class ImagePreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImagePreviewBinding
    private lateinit var repository: ReactiveVideoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImagePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ReactiveVideoRepository(this)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.title = intent.getStringExtra(EXTRA_FILE_PATH)?.substringAfterLast('/')
            ?: getString(R.string.image_preview_title)

        loadImage()
    }

    private fun loadImage() {
        val fileId = intent.getStringExtra(EXTRA_FILE_ID) ?: return
        val fileType = FileType.from(intent.getStringExtra(EXTRA_FILE_PATH).orEmpty())

        lifecycleScope.launch {
            binding.progressBar.visibility = android.view.View.VISIBLE
            try {
                val bytes = repository.getFileBytes(fileId)
                if (fileType == FileType.GIF && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val drawable = decodeAnimatedDrawable(bytes)
                    binding.imageView.setImageDrawable(drawable)
                    (drawable as? AnimatedImageDrawable)?.start()
                } else {
                    binding.imageView.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                }
            } catch (throwable: Throwable) {
                showToast(ApiErrors.fromThrowable(throwable))
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private fun decodeAnimatedDrawable(bytes: ByteArray): Drawable {
        val source = ImageDecoder.createSource(java.nio.ByteBuffer.wrap(bytes))
        return ImageDecoder.decodeDrawable(source)
    }

    companion object {
        private const val EXTRA_FILE_ID = "extra_file_id"
        private const val EXTRA_FILE_PATH = "extra_file_path"

        fun newIntent(context: Context, fileId: String, filePath: String): Intent {
            return Intent(context, ImagePreviewActivity::class.java)
                .putExtra(EXTRA_FILE_ID, fileId)
                .putExtra(EXTRA_FILE_PATH, filePath)
        }
    }
}
