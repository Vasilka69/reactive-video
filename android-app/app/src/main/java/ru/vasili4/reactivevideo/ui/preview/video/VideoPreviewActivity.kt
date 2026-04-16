package ru.vasili4.reactivevideo.ui.preview.video

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import ru.vasili4.reactivevideo.R
import ru.vasili4.reactivevideo.data.local.AppPreferences
import ru.vasili4.reactivevideo.data.network.ApiFactory
import ru.vasili4.reactivevideo.databinding.ActivityVideoPreviewBinding

class VideoPreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPreviewBinding
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.title = intent.getStringExtra(EXTRA_FILE_PATH)?.substringAfterLast('/')
            ?: getString(R.string.video_preview_title)
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }

    private fun initializePlayer() {
        val fileId = intent.getStringExtra(EXTRA_FILE_ID) ?: return
        val token = AppPreferences(this).getToken().orEmpty()
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setDefaultRequestProperties(mapOf("Authorization" to token))

        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(ApiFactory.fileUrl(this, fileId)))

        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer
            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    companion object {
        private const val EXTRA_FILE_ID = "extra_file_id"
        private const val EXTRA_FILE_PATH = "extra_file_path"

        fun newIntent(context: Context, fileId: String, filePath: String): Intent {
            return Intent(context, VideoPreviewActivity::class.java)
                .putExtra(EXTRA_FILE_ID, fileId)
                .putExtra(EXTRA_FILE_PATH, filePath)
        }
    }
}
