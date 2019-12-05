package com.plutonem.android.fluxc.store

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.plutonem.android.fluxc.Dispatcher
import com.plutonem.android.fluxc.annotations.action.Action
import com.plutonem.android.fluxc.model.LocalOrRemoteId.RemoteId
import com.plutonem.android.fluxc.model.list.*
import com.plutonem.android.fluxc.model.list.ListState.FETCHED
import com.plutonem.android.fluxc.model.list.datasource.InternalPagedListDataSource
import com.plutonem.android.fluxc.model.list.datasource.ListItemDataSourceInterface
import com.plutonem.android.fluxc.persistence.ListItemSqlUtils
import com.plutonem.android.fluxc.persistence.ListSqlUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.wordpress.android.util.AppLog
import org.wordpress.android.util.DateTimeUtils
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

/**
 * This Store is responsible for managing lists and their metadata. One of the designs goals for this Store is expose
 * as little as possible to the consumers and make sure the exposed parts are immutable. This not only moves the
 * responsibility of mutation to the Store but also makes it much easier to use the exposed data.
 */
@Singleton
class ListStore @Inject constructor(
    private val listSqlUtils: ListSqlUtils,
    private val listItemSqlUtils: ListItemSqlUtils,
    private val coroutineContext: CoroutineContext,
    dispatcher: Dispatcher
) : Store(dispatcher) {
    @Subscribe(threadMode = ThreadMode.ASYNC)
    override fun onAction(action: Action<*>) {

    }

    override fun onRegister() {
        AppLog.d(AppLog.T.API, ListStore::class.java.simpleName + " onRegister")
    }

    /**
     * This is the function that'll be used to consume lists.
     *
     * @param listDescriptor Describes which list will be consumed
     * @param dataSource Describes how to take certain actions such as fetching a list for the item type [LIST_ITEM].
     * @param lifecycle The lifecycle of the client that'll be consuming this list. It's used to make sure everything
     * is cleaned up properly once the client is destroyed.
     *
     * @return A [PagedListWrapper] that provides all the necessary information to consume a list such as its data,
     * whether the first page is being fetched, whether there are any errors etc. in `LiveData` format.
     */
    fun <LIST_DESCRIPTOR : ListDescriptor, ITEM_IDENTIFIER, LIST_ITEM> getList(
        listDescriptor: LIST_DESCRIPTOR,
        dataSource: ListItemDataSourceInterface<LIST_DESCRIPTOR, ITEM_IDENTIFIER, LIST_ITEM>,
        lifecycle: Lifecycle
    ): PagedListWrapper<LIST_ITEM> {
        val factory = createPagedListFactory(listDescriptor, dataSource)
        val pagedListData = createPagedListLiveData(
            listDescriptor = listDescriptor,
            dataSource = dataSource,
            pagedListFactory = factory
        )
        return PagedListWrapper(
            data = pagedListData,
            dispatcher = mDispatcher,
            listDescriptor = listDescriptor,
            lifecycle = lifecycle,
            refresh = {
                handleFetchList(listDescriptor, loadMore = false) { offset ->
                    dataSource.fetchList(listDescriptor, offset)
                }
            },
            invalidate = factory::invalidate,
            parentCoroutineContext = coroutineContext
        )
    }

    /**
     * A helper function that creates a [PagedList] [LiveData] for the given [LIST_DESCRIPTOR], [dataSource] and the
     * [PagedListFactory].
     */
    private fun <LIST_DESCRIPTOR : ListDescriptor, ITEM_IDENTIFIER, LIST_ITEM> createPagedListLiveData(
        listDescriptor: LIST_DESCRIPTOR,
        dataSource: ListItemDataSourceInterface<LIST_DESCRIPTOR, ITEM_IDENTIFIER, LIST_ITEM>,
        pagedListFactory: PagedListFactory<LIST_DESCRIPTOR, ITEM_IDENTIFIER, LIST_ITEM>
    ): LiveData<PagedList<LIST_ITEM>> {
        val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setInitialLoadSizeHint(listDescriptor.config.initialLoadSize)
            .setPageSize(listDescriptor.config.dbPageSize)
            .build()
        val boundaryCallback = object : PagedList.BoundaryCallback<LIST_ITEM>() {
            override fun onItemAtEndLoaded(itemAtEnd: LIST_ITEM) {
                // Load more items if we are near the end of list
                GlobalScope.launch(coroutineContext) {
                    handleFetchList(listDescriptor, loadMore = true) { offset ->
                        dataSource.fetchList(listDescriptor, offset)
                    }
                }
                super.onItemAtEndLoaded(itemAtEnd)
            }
        }
        return LivePagedListBuilder<Int, LIST_ITEM>(pagedListFactory, pagedListConfig)
            .setBoundaryCallback(boundaryCallback).build()
    }

    /**
     * A helper function that creates a [PagedListFactory] for the given [LIST_DESCRIPTOR] and [dataSource].
     */
    private fun <LIST_DESCRIPTOR : ListDescriptor, ITEM_IDENTIFIER, LIST_ITEM> createPagedListFactory(
        listDescriptor: LIST_DESCRIPTOR,
        dataSource: ListItemDataSourceInterface<LIST_DESCRIPTOR, ITEM_IDENTIFIER, LIST_ITEM>
    ): PagedListFactory<LIST_DESCRIPTOR, ITEM_IDENTIFIER, LIST_ITEM> {
        val getRemoteItemIds = { getListItems(listDescriptor).map { RemoteId(value = it) } }
        val getIsListFullyFetched = { getListState(listDescriptor) == FETCHED }
        return PagedListFactory(
            createDataSource = {
                InternalPagedListDataSource(
                    listDescriptor = listDescriptor,
                    remoteItemIds = getRemoteItemIds(),
                    isListFullyFetched = getIsListFullyFetched(),
                    itemDataSource = dataSource
                )
            })
    }

    /**
     * A helper function that returns the list items for the given [ListDescriptor].
     */
    private fun getListItems(listDescriptor: ListDescriptor): List<Long> {
        val listModel = listSqlUtils.getList(listDescriptor)
        return if (listModel != null) {
            listItemSqlUtils.getListItems(listModel.id).map { it.remoteItemId }
        } else emptyList()
    }

    /**
     * A helper function that initiates the fetch from remote for the given [ListDescriptor].
     *
     * Before fetching the list, it'll first check if this is a valid fetch depending on the list's state. Then, it'll
     * update the list's state and emit that change. Finally, it'll calculate the offset and initiate the fetch with
     * the given [fetchList] function.
     */
    private fun handleFetchList(
        listDescriptor: ListDescriptor,
        loadMore: Boolean,
        fetchList: (Long) -> Unit
    ) {
        val currentState = getListState(listDescriptor)
        if (!loadMore && currentState.isFetchingFirstPage()) {
            // already fetching the first page
            return
        } else if (loadMore && !currentState.canLoadMore()) {
            // we can only load more if there is more data to be loaded
            return
        }

        val newState = if (loadMore) ListState.LOADING_MORE else ListState.FETCHING_FIRST_PAGE
        listSqlUtils.insertOrUpdateList(listDescriptor, newState)
        handleListStateChange(listDescriptor, newState)

        val listModel = requireNotNull(listSqlUtils.getList(listDescriptor)) {
            "The `ListModel` can never be `null` here since either a new list is inserted or existing one updated"
        }
        val offset = if (loadMore) listItemSqlUtils.getListItemsCount(listModel.id) else 0L
        fetchList(offset)
    }

    /**
     * A helper function that emits the latest [ListState] for the given [ListDescriptor].
     */
    private fun handleListStateChange(listDescriptor: ListDescriptor, newState: ListState, error: ListError? = null) {
        emitChange(OnListStateChanged(listDescriptor, newState, error))
    }

    /**
     * A helper function that returns the [ListState] for the given [ListDescriptor].
     */
    private fun getListState(listDescriptor: ListDescriptor): ListState {
        val listModel = listSqlUtils.getList(listDescriptor)
        return if (listModel != null && !isListStateOutdated(listModel)) {
            requireNotNull(ListState.values().firstOrNull { it.value == listModel.stateDbValue }) {
                "The stateDbValue of the ListModel didn't match any of the `ListState`s. This likely happened " +
                        "because the ListState values were altered without a DB migration."
            }
        } else ListState.defaultState
    }

    /**
     * A helper function that returns whether it has been more than a certain time has passed since it's `lastModified`.
     *
     * Since we keep the state in the DB, in the case of application being closed during a fetch, it'll carry
     * over to the next session. To prevent such cases, we use a timeout approach. If it has been more than a
     * certain time since the list is last updated, we should ignore the state.
     */
    private fun isListStateOutdated(listModel: ListModel): Boolean {
        listModel.lastModified?.let {
            val lastModified = DateTimeUtils.dateUTCFromIso8601(it)
            val timePassed = (Date().time - lastModified.time)
            return timePassed > LIST_STATE_TIMEOUT
        }
        // If a list is null, it means we have never fetched it before, so it can't be outdated
        return false
    }

    /**
     * The event to be emitted whenever there is a change to the [ListState]
     */
    class OnListStateChanged(
        val listDescriptor: ListDescriptor,
        val newState: ListState,
        error: ListError?
    ) : Store.OnChanged<ListError>() {
        init {
            this.error = error
        }
    }

    class ListError(
        val type: ListErrorType,
        val message: String? = null
    ) : OnChangedError

    enum class ListErrorType {
        GENERIC_ERROR
    }
}