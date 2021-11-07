package com.codedev.videoapplication.ui.feature_get_video

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.VideoView
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.navigation.navArgs
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.codedev.videoapplication.R
import com.codedev.videoapplication.data.models.transformTags
import com.codedev.videoapplication.databinding.ActivityVideoBinding
import com.codedev.videoapplication.databinding.LayoutVideoDetailsBinding
import com.codedev.videoapplication.ui.feature_get_video.utils.addListener
import com.codedev.videoapplication.ui.feature_get_video.utils.openBrowser
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.util.Util
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "VideoActivity"

@AndroidEntryPoint
class VideoActivity : AppCompatActivity() {

    private val args: VideoActivityArgs by navArgs()
    private var _binding: ActivityVideoBinding? = null
    private val binding get() = _binding!!

    private var player: SimpleExoPlayer? = null
    private var listener: Player.Listener? = null

    private var cancelButton: AppCompatImageView? = null
    private var fullScreenButton: AppCompatImageView? = null
    private var titleTextView: AppCompatTextView? = null

    private var height: Int? = null

    private val viewModel by viewModels<VideoViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViews()

    }

    private fun setupViews() {
        val hit = args.hit
        val videoImage = "https://i.vimeocdn.com/video/${hit.picture_id}_960x540.jpg"
        val userPage = "https://pixabay.com/users/${hit.user}-${hit.user_id}"

        val circularProgressDrawable = CircularProgressDrawable(this)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        binding.videoImage?.let {
            Glide.with(this)
                .load(videoImage)
                .placeholder(circularProgressDrawable)
                .centerCrop()
                .error(R.drawable.ic_error)
                .into(it)
        }

        binding.userImage?.let {
            Glide.with(this)
                .load(hit.userImageURL)
                .placeholder(circularProgressDrawable)
                .centerCrop()
                .error(R.drawable.ic_error)
                .into(it)
        }

        binding.apply {
            videoTitle?.text = hit.type
            username?.text = hit.user
            textError?.isVisible = false
            videoDetailsSection?.apply {
                videoComments.text = hit.comments.toString()
                videoLikes.text = hit.likes.toString()
                videoViews.text = hit.views.toString()
                pageLink?.text = hit.pageURL
                userLinkDetails?.text = StringBuilder().append("Learn more About ").append(hit.user)
                userLink?.text = userPage
                tags?.text = hit.transformTags()

                pageLink?.openBrowser(userPage, this@VideoActivity)
                userLink?.openBrowser(hit.pageURL, this@VideoActivity)
            }
        }

    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun initializePlayer() {
        val trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        player = SimpleExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()
            .also { exoPlayer ->
                binding.playerView.player = exoPlayer
            }

        cancelButton = binding.playerView.findViewById(R.id.cross_im)
        fullScreenButton = binding.playerView.findViewById(R.id.full_screen)
        titleTextView = binding.playerView.findViewById(R.id.header_tv)

        fullScreenButton?.setOnClickListener {
            val orientation = viewModel.state.value.orientation
            if (orientation == UiOrientation.PORTRAIT) {
                this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                viewModel.execute(VideoEvents.UpdateOrientation(UiOrientation.LANDSCAPE))
            } else {
                this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                viewModel.execute(VideoEvents.UpdateOrientation(UiOrientation.PORTRAIT))
            }
        }
        cancelButton?.setOnClickListener {
            finish()
        }

        player?.addListener(
            idle = {
                binding.progressBar.isVisible = false
                binding.videoImage?.isVisible = false
                binding.textError?.isVisible = true
                Log.d(TAG, "initializePlayer: ${binding.textError?.text} hello")
            },
            buffering = {
                binding.progressBar.isVisible = true
                binding.textError?.isVisible = false
            },
            ready = {
                binding.progressBar.isVisible = false
                binding.videoImage?.isVisible = false
                binding.textError?.isVisible = false
            },
            onListen = {
                listener = it
            },
            error = { error ->
                binding.progressBar.isVisible = false
                binding.videoImage?.isVisible = false
                binding.textError?.let {
                    it.isVisible = true
                    it.text = StringBuilder().append(error).append(" Click to Try again")
                }
            }
        )
        playVideo()
    }

    private fun playVideo() {
        val hit = args.hit
        val media = hit.videos.tiny.url
        val uri = Uri.parse(media)
        val mediaItem = MediaItem.fromUri(uri)
        val state = viewModel.state.value
        player?.let { exoPlayer ->
            exoPlayer.playWhenReady = state.playWhenReady
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.seekTo(
                state.currentWindow,
                state.playbackPosition
            )
            exoPlayer.prepare()
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onBackPressed() {
        val orientation = viewModel.state.value.orientation
        if (orientation == UiOrientation.LANDSCAPE) {
            this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            viewModel.execute(VideoEvents.UpdateOrientation(UiOrientation.PORTRAIT))
        } else {
            super.onBackPressed()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUi()
            viewModel.execute(VideoEvents.UpdateOrientation(UiOrientation.LANDSCAPE))
            height = binding.playerView.layoutParams.height
            val layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            binding.playerView.layoutParams = layoutParams
            binding.videoImage?.layoutParams = layoutParams
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            viewModel.execute(VideoEvents.UpdateOrientation(UiOrientation.PORTRAIT))
            val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height!!)
            binding.playerView.layoutParams = layoutParams
            binding.videoImage?.layoutParams = layoutParams
            binding.descriptionLayout?.isVisible = true
            showSystemUi()
        }
    }

    private fun hideSystemUi() {
        if (Util.SDK_INT < 16) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        binding.playerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun showSystemUi() {
        if (Util.SDK_INT < 16) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN.inv(),
                WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
            )
        }

        binding.playerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.state.value.orientation == UiOrientation.LANDSCAPE) {
            hideSystemUi()
        }
        if ((Util.SDK_INT < 24 || player == null)) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }


    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        player?.run {
            viewModel.execute(
                VideoEvents.SaveVideoState(
                    viewModel.state.value.copy(
                        playWhenReady = this.playWhenReady,
                        currentWindow = this.currentWindowIndex,
                        playbackPosition = this.currentPosition
                    )
                )
            )
            listener?.let { removeListener(it) }
            release()
        }
        player = null
    }
}

