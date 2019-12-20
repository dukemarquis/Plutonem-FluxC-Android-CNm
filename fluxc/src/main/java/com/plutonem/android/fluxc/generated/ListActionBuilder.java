package com.plutonem.android.fluxc.generated;

import com.plutonem.android.fluxc.action.ListAction;
import com.plutonem.android.fluxc.annotations.action.Action;
import com.plutonem.android.fluxc.annotations.action.ActionBuilder;
import com.plutonem.android.fluxc.store.ListStore.RemoveExpiredListsPayload;
import com.plutonem.android.fluxc.store.ListStore.FetchedListItemsPayload;
import com.plutonem.android.fluxc.store.ListStore.ListItemsChangedPayload;

public final class ListActionBuilder extends ActionBuilder {
    public ListActionBuilder() {
    }

    public static Action<FetchedListItemsPayload> newFetchedListItemsAction(FetchedListItemsPayload payload) {
        return new Action(ListAction.FETCHED_LIST_ITEMS, payload);
    }

    public static Action<ListItemsChangedPayload> newListItemsChangedAction(ListItemsChangedPayload payload) {
        return new Action(ListAction.LIST_ITEMS_CHANGED, payload);
    }

    public static Action<RemoveExpiredListsPayload> newRemoveExpiredListsAction(RemoveExpiredListsPayload payload) {
        return new Action(ListAction.REMOVE_EXPIRED_LISTS, payload);
    }
}
