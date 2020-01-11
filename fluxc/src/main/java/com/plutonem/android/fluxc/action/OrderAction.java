package com.plutonem.android.fluxc.action;

import com.plutonem.android.fluxc.annotations.Action;
import com.plutonem.android.fluxc.annotations.ActionEnum;
import com.plutonem.android.fluxc.annotations.action.IAction;
import com.plutonem.android.fluxc.model.OrderModel;
import com.plutonem.android.fluxc.store.OrderStore.FetchOrderResponsePayload;
import com.plutonem.android.fluxc.store.OrderStore.FetchOrderListPayload;
import com.plutonem.android.fluxc.store.OrderStore.FetchOrderListResponsePayload;
import com.plutonem.android.fluxc.store.OrderStore.RemoteOrderPayload;

@ActionEnum
public enum OrderAction implements IAction {
    // Remote actions
    @Action(payloadType = FetchOrderListPayload.class)
    FETCH_ORDER_LIST,
    @Action(payloadType = RemoteOrderPayload.class)
    FETCH_ORDER,
    @Action(payloadType = RemoteOrderPayload.class)
    PUSH_ORDER,

    // Remote responses
    @Action(payloadType = FetchOrderListResponsePayload.class)
    FETCHED_ORDER_LIST,
    @Action(payloadType = FetchOrderResponsePayload.class)
    FETCHED_ORDER,
    @Action(payloadType = RemoteOrderPayload.class)
    PUSHED_ORDER,

    // Local actions
    @Action(payloadType = OrderModel.class)
    UPDATE_ORDER,
    @Action(payloadType = OrderModel.class)
    REMOVE_ORDER
}
