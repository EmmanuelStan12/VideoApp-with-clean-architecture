package com.codedev.videoapplication.ui.feature_search_video

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codedev.videoapplication.R
import com.codedev.videoapplication.databinding.FragmentSearchVideoBinding
import com.codedev.videoapplication.ui.feature_search_video.adapters.SearchVideoAdapter
import com.codedev.videoapplication.ui.feature_search_video.adapters.VideoLoadStateAdapter
import com.codedev.videoapplication.ui.utils.onChange
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SearchVideoFragment : Fragment(R.layout.fragment_search_video) {

    private var _binding: FragmentSearchVideoBinding? = null
    private val binding get() = _binding!!

    private lateinit var linearLayoutManager: LinearLayoutManager
    var job: Job? = null

    @ExperimentalCoroutinesApi
    private val viewModel by viewModels<SearchVideoViewModel>()

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentSearchVideoBinding.bind(view)

        val adapter = SearchVideoAdapter {
            val action = SearchVideoFragmentDirections.actionSearchVideoFragmentToVideoActivity(it)
            findNavController().navigate(action)
        }

        linearLayoutManager = LinearLayoutManager(requireContext())

        binding.searchInputField.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when(actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    job?.cancel()
                    job = CoroutineScope(Dispatchers.IO).launch {
                        viewModel.accept(SearchVideoEvents.Search(binding.searchInputField.text.toString()))
                    }
                    true
                }
                else -> false
            }
        }

        val state = viewModel.state.value
        if(!state.hasNotScrolledForCurrentSearch) {
            binding.searchRecyclerView.scrollToPosition(state.currentPosition)
        }
        binding.searchInputField.setText(state.query)

        binding.searchRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            this.adapter = adapter.withLoadStateHeaderAndFooter(
                footer = VideoLoadStateAdapter { adapter.retry() },
                header = VideoLoadStateAdapter { adapter.retry() }
            )
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val currentPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition()
                    val visiblePosition: Int = linearLayoutManager.findFirstCompletelyVisibleItemPosition() ?: -1
                    if (dy != 0 && visiblePosition > -1) {
                        viewModel.accept(SearchVideoEvents.Scroll(viewModel.state.value.query, visiblePosition))
                    }
                }
            })
        }

        binding.retryButton.setOnClickListener {
            adapter.retry()
        }

        adapter.addLoadStateListener { loadStates ->
            binding.apply {
                progressBar.isVisible = loadStates.source.refresh is LoadState.Loading
                searchRecyclerView.isVisible = loadStates.source.refresh is LoadState.NotLoading
                searchRecyclerView.isVisible = loadStates.source.refresh is LoadState.NotLoading
                if (loadStates.source.refresh is LoadState.Error) {
                    retryButton.isVisible = true
                    progressErrorText.isVisible = true
                    progressErrorText.text =
                        (loadStates.source.refresh as LoadState.Error).error.message
                            ?: "Unknown Error Occurred"
                } else {
                    retryButton.isVisible = false
                    progressErrorText.isVisible = false
                }
                if (loadStates.source.refresh is LoadState.NotLoading && loadStates.append.endOfPaginationReached && adapter.itemCount < 1) {
                    searchRecyclerView.isVisible = false
                    emptyListText.isVisible = true
                } else {
                    searchRecyclerView.isVisible = true
                    emptyListText.isVisible = false
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.state.collectLatest {
                adapter.submitData(viewLifecycleOwner.lifecycle, it.pagingData)

                if(it.hasNotScrolledForCurrentSearch) {
                    //binding.searchRecyclerView.scrollToPosition(0)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.connection.collectLatest {
                if(it) {
                    binding.apply {
                        internetDetails.text = resources.getString(R.string.internet_connection)
                        internetDetails.setTextColor(ResourcesCompat.getColor(resources, R.color.text_white, null))
                        imageWifi.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_internet, null))
                        internetSection.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.dark_grey, null))
                    }
                } else {
                    binding.apply {
                        internetDetails.text = resources.getString(R.string.no_internet_connection)
                        internetDetails.setTextColor(ResourcesCompat.getColor(resources, R.color.dark_grey, null))
                        imageWifi.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_no_internet, null))
                        internetSection.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.text_white, null))
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
        _binding = null
    }
}