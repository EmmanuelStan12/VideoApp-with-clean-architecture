package com.codedev.videoapplication.ui.feature_search_video

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.codedev.videoapplication.data.models.Hit
import com.codedev.videoapplication.domain.usecases.VideoUseCases
import com.codedev.videoapplication.ui.utils.ConnectionFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class SearchVideoViewModel @Inject constructor(
    private val useCases: VideoUseCases,
    private val savedStateHandle: SavedStateHandle,
    application: Application
): AndroidViewModel(application) {

    companion object {
        const val LAST_SEARCH_QUERY = "last_search_query"
        const val LAST_QUERY_SCROLLED = "last_query_scrolled"
        const val DEFAULT_QUERY = "science"
    }

    val state: StateFlow<SearchVideoState>
    val accept: (SearchVideoEvents) -> Unit

    init {
        val initialQuery: String = savedStateHandle.get(LAST_SEARCH_QUERY) ?: DEFAULT_QUERY
        val lastQueryScrolled: String = savedStateHandle.get(LAST_QUERY_SCROLLED) ?: DEFAULT_QUERY
        val actionStateFlow = MutableSharedFlow<SearchVideoEvents>()
        val searches = actionStateFlow
            .filterIsInstance<SearchVideoEvents.Search>()
            .distinctUntilChanged()
            .onStart { emit(SearchVideoEvents.Search(query = initialQuery)) }
        val queriesScrolled = actionStateFlow
            .filterIsInstance<SearchVideoEvents.Scroll>()
            .distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
            .onStart { emit(SearchVideoEvents.Scroll(currentQuery = lastQueryScrolled)) }
        state = searches
            .flatMapLatest { search ->
                combine(
                    queriesScrolled,
                    searchVideo(search.query),
                    ::Pair
                ).distinctUntilChangedBy {
                        it.second
                    }
                    .map { (scroll, pagingData) ->
                        SearchVideoState(
                            query = search.query,
                            pagingData = pagingData,
                            lastQueryScrolled = scroll.currentQuery,
                            hasNotScrolledForCurrentSearch = search.query != scroll.currentQuery,
                            currentPosition = scroll.currentPosition
                        )
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = SearchVideoState()
            )
        accept = { event ->
            viewModelScope.launch { actionStateFlow.emit(event) }
        }


    }
    val connection = ConnectionFlow(
        getApplication(),
        viewModelScope
    ).connectionFlow


    private suspend fun searchVideo(query: String = DEFAULT_QUERY): Flow<PagingData<Hit>> {
        return useCases.searchVideo(query)
            .map { pagingData ->
                Log.d("TAG", "searchVideo: ${pagingData.toString()}")
                pagingData.map {
                    it.copy(views = it.views)
                }
            }
            .cachedIn(viewModelScope)
    }

    override fun onCleared() {
        savedStateHandle[LAST_SEARCH_QUERY] = state.value.query
        savedStateHandle[LAST_QUERY_SCROLLED] = state.value.lastQueryScrolled
        super.onCleared()
    }
}

sealed class UiEvents {
    object Header: UiEvents()
    object Footer: UiEvents()
}