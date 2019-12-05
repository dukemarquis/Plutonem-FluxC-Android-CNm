package com.plutonem.android.fluxc.model.list

import androidx.lifecycle.*
import androidx.paging.PagedList
import com.plutonem.android.fluxc.Dispatcher
import com.plutonem.android.fluxc.store.ListStore.ListError
import com.plutonem.android.fluxc.store.ListStore.OnListStateChanged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.coroutines.CoroutineContext

class PagedListWrapper<T>(
    val data: LiveData<PagedList<T>>,
    private val dispatcher: Dispatcher,
    private val listDescriptor: ListDescriptor,
    private val lifecycle: Lifecycle,
    private val refresh: () -> Unit,
    private val invalidate: () -> Unit,
    private val parentCoroutineContext: CoroutineContext
) : LifecycleObserver, CoroutineScope {
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = parentCoroutineContext + job

    private val _isFetchingFirstPage = MutableLiveData<Boolean>()
    val isFetchingFirstPage: LiveData<Boolean> = _isFetchingFirstPage

    private val _isLoadingMore = MutableLiveData<Boolean>()
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _listError = MutableLiveData<ListError?>()
    val listError: LiveData<ListError?> = _listError

    private val _isEmpty = MediatorLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty

    /**
     * Register the dispatcher so we can handle `ListStore` events and add an observer for the lifecycle so we can
     * cleanup properly in `onDestroy`.
     */
    init {
        _isEmpty.addSource(data) {
            _isEmpty.value = it?.isEmpty()
        }
        dispatcher.register(this)
        lifecycle.addObserver(this)
    }

    /**
     * Handles the [Lifecycle.Event.ON_DESTROY] event to cleanup the registration for dispatcher and removing the
     * observer for lifecycle.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        lifecycle.removeObserver(this)
        dispatcher.unregister(this)
        job.cancel()
    }

    /**
     * A method to be used by clients to refresh the first page of a list from network.
     */
    fun fetchFirstPage() {
        launch {
            refresh()
        }
    }

    /**
     * Handles the [OnListStateChanged] `ListStore` event. It'll update the state information related [LiveData]
     * instances.
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    @Suppress("unused")
    fun onListStateChanged(event: OnListStateChanged) {
        if (event.listDescriptor != listDescriptor) {
            return
        }
        _isFetchingFirstPage.postValue(event.newState.isFetchingFirstPage())
        _isLoadingMore.postValue(event.newState.isLoadingMore())
        _listError.postValue(event.error)
    }
}