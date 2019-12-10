package com.plutonem.android.fluxc.generated;

import com.plutonem.android.fluxc.action.OrderAction;
import com.plutonem.android.fluxc.annotations.action.Action;
import com.plutonem.android.fluxc.store.OrderStore.FetchOrderResponsePayload;
import com.plutonem.android.fluxc.store.OrderStore.FetchOrderListPayload;
import com.plutonem.android.fluxc.store.OrderStore.FetchOrderListResponsePayload;
import com.plutonem.android.fluxc.store.OrderStore.RemoteOrderPayload;

public class OrderActionBuilder {
    public OrderActionBuilder() {
    }

    public static Action<FetchOrderListPayload> newFetchOrderListAction(FetchOrderListPayload payload) {
        return new Action(OrderAction.FETCH_ORDER_LIST, payload);
    }

    public static Action<RemoteOrderPayload> newFetchOrderAction(RemoteOrderPayload payload) {
        return new Action(OrderAction.FETCH_ORDER, payload);
    }

    public static Action<FetchOrderListResponsePayload> newFetchedOrderListAction(FetchOrderListResponsePayload payload) {
        return new Action(OrderAction.FETCHED_ORDER_LIST, payload);
    }

    public static Action<FetchOrderResponsePayload> newFetchedOrderAction(FetchOrderResponsePayload payload) {
        return new Action(OrderAction.FETCHED_ORDER, payload);
    }
}
