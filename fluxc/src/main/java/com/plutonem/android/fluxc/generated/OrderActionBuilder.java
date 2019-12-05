package com.plutonem.android.fluxc.generated;

import com.plutonem.android.fluxc.action.OrderAction;
import com.plutonem.android.fluxc.annotations.action.Action;
import com.plutonem.android.fluxc.store.OrderStore.FetchOrderListPayload;

public class OrderActionBuilder {
    public OrderActionBuilder() {
    }

    public static Action<FetchOrderListPayload> newFetchOrderListAction(FetchOrderListPayload payload) {
        return new Action(OrderAction.FETCH_ORDER_LIST, payload);
    }
}
