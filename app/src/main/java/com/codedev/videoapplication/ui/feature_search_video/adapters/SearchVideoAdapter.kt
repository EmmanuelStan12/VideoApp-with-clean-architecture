package com.codedev.videoapplication.ui.feature_search_video.adapters

import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.codedev.videoapplication.R
import com.codedev.videoapplication.data.models.Hit
import com.codedev.videoapplication.databinding.LayoutSearchVideoBinding

private val diffCallback = object : DiffUtil.ItemCallback<Hit>() {
    override fun areItemsTheSame(oldItem: Hit, newItem: Hit): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Hit, newItem: Hit): Boolean {
        return oldItem == newItem
    }
}

class SearchVideoAdapter(
    val navigate: (Hit) -> Unit
) : PagingDataAdapter<Hit, SearchVideoAdapter.SearchVideoViewHolder>(
    diffCallback
) {

    override fun onBindViewHolder(holder: SearchVideoViewHolder, position: Int) {
        val hit = getItem(position)
        hit?.let {
            holder.bind(it)
            holder.itemView.setOnClickListener { v ->
                navigate(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchVideoViewHolder {
        return SearchVideoViewHolder(
            LayoutSearchVideoBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    inner class SearchVideoViewHolder(private val binding: LayoutSearchVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(hit: Hit) {
            binding.apply {
                username.text = hit.user
                videoDownloads.text = formatString(hit.downloads, "Downloads")
                videoViews.text = formatString(hit.views, "Views")
                videoComments.text = formatString(hit.comments, "Comments")
                videoLikes.text = formatString(hit.likes, "Likes")
                videoDuration.text = formatString(hit.duration, "Seconds")

                /*val dataRetriever = MediaMetadataRetriever()
                dataRetriever.setDataSource(hit.videos.medium.url)
                val bitmap = dataRetriever.getFrameAtTime(1000L)*/

                val url = "https://i.vimeocdn.com/video/${hit.picture_id}_960x540.jpg"

                val circularProgressDrawable = CircularProgressDrawable(itemView.context)
                circularProgressDrawable.strokeWidth = 5f
                circularProgressDrawable.centerRadius = 30f
                circularProgressDrawable.backgroundColor = 255
                circularProgressDrawable.start()

                Glide.with(itemView.context)
                    .load(hit.userImageURL)
                    .placeholder(circularProgressDrawable)
                    .centerCrop()
                    .error(R.drawable.ic_error)
                    .into(userImage)

                Glide.with(itemView.context)
                    .load(url)
                    .placeholder(circularProgressDrawable)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.ic_error)
                    .into(videoImage)
            }
        }

        private fun formatString(value: Int, text: String) : String {
            return itemView.resources.getString(R.string.video_info, value, text)
        }
    }
}