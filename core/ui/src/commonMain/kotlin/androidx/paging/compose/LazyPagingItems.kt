package androidx.paging.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.paging.CombinedLoadStates
import androidx.paging.ItemSnapshotList
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

class LazyPagingItems<T : Any> internal constructor(
    private val flow: Flow<PagingData<T>>,
) {
    private val pagingDataPresenter = object : PagingDataPresenter<T>() {
        override suspend fun presentPagingDataEvent(event: PagingDataEvent<T>) {
            updateItemSnapshotList()
        }
    }

    var itemSnapshotList by mutableStateOf(pagingDataPresenter.snapshot())
        private set

    val itemCount: Int
        get() = itemSnapshotList.size

    private fun updateItemSnapshotList() {
        itemSnapshotList = pagingDataPresenter.snapshot()
    }

    operator fun get(index: Int): T? {
        pagingDataPresenter[index]
        return itemSnapshotList[index]
    }

    fun peek(index: Int): T? = itemSnapshotList[index]

    fun retry() {
        pagingDataPresenter.retry()
    }

    fun refresh() {
        pagingDataPresenter.refresh()
    }

    var loadState: CombinedLoadStates by mutableStateOf(
        pagingDataPresenter.loadStateFlow.value
            ?: CombinedLoadStates(
                refresh = InitialLoadStates.refresh,
                prepend = InitialLoadStates.prepend,
                append = InitialLoadStates.append,
                source = InitialLoadStates,
            )
    )
        private set

    internal suspend fun collectPagingData() {
        flow.collectLatest {
            pagingDataPresenter.collectFrom(it)
        }
    }

    internal suspend fun collectLoadState() {
        pagingDataPresenter.loadStateFlow.collect {
            if (it != null) {
                loadState = it
            }
        }
    }
}

private val IncompleteLoadState = LoadState.NotLoading(false)
private val InitialLoadStates = LoadStates(
    LoadState.Loading,
    IncompleteLoadState,
    IncompleteLoadState,
)

@Composable
fun <T : Any> Flow<PagingData<T>>.collectAsLazyPagingItems(): LazyPagingItems<T> {
    val lazyPagingItems = remember(this) { LazyPagingItems(this) }
    LaunchedEffect(lazyPagingItems) {
        lazyPagingItems.collectPagingData()
    }
    LaunchedEffect(lazyPagingItems) {
        lazyPagingItems.collectLoadState()
    }
    return lazyPagingItems
}

fun <T : Any> LazyPagingItems<T>.itemKey(
    key: ((item: T) -> Any)? = null,
): (index: Int) -> Any {
    return { index ->
        if (key == null) {
            index
        } else {
            val item = peek(index)
            if (item == null) index else key(item)
        }
    }
}
