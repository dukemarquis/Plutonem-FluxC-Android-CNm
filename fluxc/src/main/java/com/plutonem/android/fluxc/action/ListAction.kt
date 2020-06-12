package com.plutonem.android.fluxc.action

import com.plutonem.android.fluxc.annotations.Action
import com.plutonem.android.fluxc.annotations.ActionEnum
import com.plutonem.android.fluxc.annotations.action.IAction
import com.plutonem.android.fluxc.model.list.ListDescriptorTypeIdentifier
import com.plutonem.android.fluxc.store.ListStore.*

@ActionEnum
enum class ListAction : IAction {
    @Action(payloadType = FetchedListItemsPayload::class)
    FETCHED_LIST_ITEMS,
    @Action(payloadType = ListItemsChangedPayload::class)
    LIST_ITEMS_CHANGED,
    @Action(payloadType = ListDescriptorTypeIdentifier::class)
    LIST_DATA_INVALIDATED,
    @Action(payloadType = RemoveExpiredListsPayload::class)
    REMOVE_EXPIRED_LISTS,
    @Action
    REMOVE_ALL_LISTS
}