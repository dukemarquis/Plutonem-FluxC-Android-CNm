package com.plutonem.android.fluxc.action;

import com.plutonem.android.fluxc.annotations.Action;
import com.plutonem.android.fluxc.annotations.ActionEnum;
import com.plutonem.android.fluxc.annotations.action.IAction;
import com.plutonem.android.fluxc.model.OrderModel;
import com.plutonem.android.fluxc.store.OrderStore.FetchOrderListPayload;
import com.plutonem.android.fluxc.store.OrderStore.FetchOrderListResponsePayload;
import com.plutonem.android.fluxc.store.OrderStore.FetchOrderResponsePayload;
import com.plutonem.android.fluxc.store.OrderStore.RemoteDecryptionPayload;
import com.plutonem.android.fluxc.store.OrderStore.RemoteInfoPayload;
import com.plutonem.android.fluxc.store.OrderStore.RemoteOrderPayload;
import com.plutonem.android.fluxc.store.OrderStore.RemoteResultPayload;

@ActionEnum
public enum OrderAction implements IAction {
    // Remote actions
    @Action(payloadType = FetchOrderListPayload.class)
    FETCH_ORDER_LIST,
    @Action(payloadType = RemoteOrderPayload.class)
    FETCH_ORDER,
    @Action(payloadType = RemoteOrderPayload.class)
    PUSH_ORDER,
    @Action(payloadType = RemoteOrderPayload.class)
    SIGN_INFO,
    @Action(payloadType = RemoteResultPayload.class)
    DECRYPT_RESULT,

    // Remote responses
    @Action(payloadType = FetchOrderListResponsePayload.class)
    FETCHED_ORDER_LIST,
    @Action(payloadType = FetchOrderResponsePayload.class)
    FETCHED_ORDER,
    @Action(payloadType = RemoteOrderPayload.class)
    PUSHED_ORDER,
    @Action(payloadType = RemoteInfoPayload.class)
    SIGNED_INFO,
    @Action(payloadType = RemoteDecryptionPayload.class)
    DECRYPTED_RESULT,

    // Local actions
    @Action(payloadType = OrderModel.class)
    UPDATE_ORDER,
    @Action(payloadType = OrderModel.class)
    REMOVE_ORDER,
    @Action
    REMOVE_ALL_ORDERS
}
