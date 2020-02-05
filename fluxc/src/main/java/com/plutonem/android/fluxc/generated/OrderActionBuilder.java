package com.plutonem.android.fluxc.generated;

import com.plutonem.android.fluxc.action.OrderAction;
import com.plutonem.android.fluxc.annotations.action.Action;
import com.plutonem.android.fluxc.model.OrderModel;
import com.plutonem.android.fluxc.store.OrderStore.FetchOrderListPayload;
import com.plutonem.android.fluxc.store.OrderStore.FetchOrderListResponsePayload;
import com.plutonem.android.fluxc.store.OrderStore.FetchOrderResponsePayload;
import com.plutonem.android.fluxc.store.OrderStore.RemoteDecryptionPayload;
import com.plutonem.android.fluxc.store.OrderStore.RemoteInfoPayload;
import com.plutonem.android.fluxc.store.OrderStore.RemoteOrderPayload;
import com.plutonem.android.fluxc.store.OrderStore.RemoteResultPayload;

public class OrderActionBuilder {
    public OrderActionBuilder() {
    }

    public static Action<FetchOrderListPayload> newFetchOrderListAction(FetchOrderListPayload payload) {
        return new Action(OrderAction.FETCH_ORDER_LIST, payload);
    }

    public static Action<RemoteOrderPayload> newFetchOrderAction(RemoteOrderPayload payload) {
        return new Action(OrderAction.FETCH_ORDER, payload);
    }

    public static Action<RemoteOrderPayload> newPushOrderAction(RemoteOrderPayload payload) {
        return new Action(OrderAction.PUSH_ORDER, payload);
    }

    public static Action<RemoteOrderPayload> newSignInfoAction(RemoteOrderPayload payload) {
        return new Action(OrderAction.SIGN_INFO, payload);
    }

    public static Action<RemoteResultPayload> newDecryptResultAction(RemoteResultPayload payload) {
        return new Action(OrderAction.DECRYPT_RESULT, payload);
    }

    public static Action<FetchOrderListResponsePayload> newFetchedOrderListAction(FetchOrderListResponsePayload payload) {
        return new Action(OrderAction.FETCHED_ORDER_LIST, payload);
    }

    public static Action<FetchOrderResponsePayload> newFetchedOrderAction(FetchOrderResponsePayload payload) {
        return new Action(OrderAction.FETCHED_ORDER, payload);
    }

    public static Action<RemoteOrderPayload> newPushedOrderAction(RemoteOrderPayload payload) {
        return new Action(OrderAction.PUSHED_ORDER, payload);
    }

    public static Action<RemoteInfoPayload> newSignedInfoAction(RemoteInfoPayload payload) {
        return new Action(OrderAction.SIGNED_INFO, payload);
    }

    public static Action<RemoteDecryptionPayload> newDecryptedResultAction(RemoteDecryptionPayload payload) {
        return new Action(OrderAction.DECRYPTED_RESULT, payload);
    }

    public static Action<OrderModel> newUpdateOrderAction(OrderModel payload) {
        return new Action(OrderAction.UPDATE_ORDER, payload);
    }

    public static Action<OrderModel> newRemoveOrderAction(OrderModel payload) {
        return new Action(OrderAction.REMOVE_ORDER, payload);
    }
}
