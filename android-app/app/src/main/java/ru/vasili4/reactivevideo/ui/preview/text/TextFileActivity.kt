package ru.vasili4.reactivevideo.ui.preview.text

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File
import ru.vasili4.reactivevideo.R
import ru.vasili4.reactivevideo.data.network.ApiErrors
import ru.vasili4.reactivevideo.data.repository.ReactiveVideoRepository
import ru.vasili4.reactivevideo.databinding.ActivityTextFileBinding
import ru.vasili4.reactivevideo.ui.common.showToast

class TextFileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTextFileBinding
    private lateinit var repository: ReactiveVideoRepository

    private var fileId: String = ""
    private var mediaPlayer: MediaPlayer? = null
    private var audioCacheFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextFileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ReactiveVideoRepository(this)
        fileId = intent.getStringExtra(EXTRA_FILE_ID).orEmpty()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.title = intent.getStringExtra(EXTRA_FILE_PATH)?.substringAfterLast('/')
            ?: getString(R.string.text_preview_title)

        binding.buttonSpeak.setOnClickListener { loadSpeech(useCache = false) }
        binding.buttonSpeakCached.setOnClickListener { loadSpeech(useCache = true) }

        loadText()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun loadText() {
        lifecycleScope.launch {
            binding.progressBar.visibility = android.view.View.VISIBLE
            try {
                val text = repository.getTextContent(fileId)
                binding.textContent.text = text.ifBlank { getString(R.string.empty_text_content) }
            } catch (throwable: Throwable) {
                binding.textContent.text = ApiErrors.fromThrowable(throwable)
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private fun loadSpeech(useCache: Boolean) {
        lifecycleScope.launch {
            binding.progressBar.visibility = android.view.View.VISIBLE
            try {
                val audioBytes = repository.getTextToSpeech(fileId, useCache)
                val file = File(cacheDir, "tts_${System.currentTimeMillis()}.mp3")
                file.writeBytes(audioBytes)
                audioCacheFile?.delete()
                audioCacheFile = file
                playAudio(file)
                showToast(if (useCache) "Кэшированная озвучка готова" else "Озвучка готова")
            } catch (throwable: Throwable) {
                showToast(ApiErrors.fromThrowable(throwable))
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private fun playAudio(file: File) {
        releasePlayer()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            start()
        }
    }

    private fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        private const val EXTRA_FILE_ID = "extra_file_id"
        private const val EXTRA_FILE_PATH = "extra_file_path"

        fun newIntent(context: Context, fileId: String, filePath: String): Intent {
            return Intent(context, TextFileActivity::class.java)
                .putExtra(EXTRA_FILE_ID, fileId)
                .putExtra(EXTRA_FILE_PATH, filePath)
        }
    }
}
