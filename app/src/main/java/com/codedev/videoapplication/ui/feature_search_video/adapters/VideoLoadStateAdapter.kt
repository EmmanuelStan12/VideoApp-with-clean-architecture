package com.codedev.videoapplication.ui.feature_search_video.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codedev.videoapplication.databinding.LayoutLoadStateBinding

class VideoLoadStateAdapter constructor(
    val retry: () -> Unit
) : LoadStateAdapter<VideoLoadStateAdapter.VideoLoadStateViewHolder>() {

    override fun onBindViewHolder(holder: VideoLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): VideoLoadStateViewHolder {
        return VideoLoadStateViewHolder(
            LayoutLoadStateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    inner class VideoLoadStateViewHolder(private val binding: LayoutLoadStateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.retryButton.setOnClickListener {
                retry.invoke()
            }
        }

        fun bind(loadState: LoadState) {
            binding.apply {
                progressBar.isVisible = loadState is LoadState.Loading
                retryButton.isVisible = loadState !is LoadState.Loading
                progressErrorText.isVisible = loadState !is LoadState.Loading
                if(loadState is LoadState.Error) {
                    progressErrorText.text = loadState.error.message ?: "An Unknown Error Occurred"
                }
            }
        }
    }
}