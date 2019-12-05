package com.plutonem.android.fluxc.store;

import com.plutonem.android.fluxc.Dispatcher;
import com.plutonem.android.fluxc.Payload;
import com.plutonem.android.fluxc.action.OrderAction;
import com.plutonem.android.fluxc.annotations.action.Action;
import com.plutonem.android.fluxc.annotations.action.IAction;
import com.plutonem.android.fluxc.model.list.OrderListDescriptor;
import com.plutonem.android.fluxc.model.list.OrderListDescriptor.OrderListDescriptorForRestBuyer;
import com.plutonem.android.fluxc.model.order.OrderStatus;
import com.plutonem.android.fluxc.network.BaseRequest.BaseNetworkError;
import com.plutonem.android.fluxc.network.rest.plutonem.order.OrderRestClient;
import com.plutonem.android.fluxc.persistence.OrderSqlUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.AppLog.T;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OrderStore extends Store {
    public static final List<OrderStatus> DEFAULT_ORDER_STATUS_LIST = Collections.unmodifiableList(Arrays.asList(
            OrderStatus.DELIVERING,
            OrderStatus.RECEIVING,
            OrderStatus.FINISHED));

    public static class FetchOrderListPayload extends Payload<BaseNetworkError> {
        public OrderListDescriptor listDescriptor;
        public long offset;

        public FetchOrderListPayload(OrderListDescriptor listDescriptor, long offset) {
            this.listDescriptor = listDescriptor;
            this.offset = offset;
        }
    }

    private final OrderRestClient mOrderRestClient;
    private final OrderSqlUtils mOrderSqlUtils;
    // Ensures that the UploadStore is initialized whenever the OrderStore is,
    // to ensure actions are shadowed and repeated by the UploadStore
    @SuppressWarnings("unused")
    @Inject UploadStore mUploadStore;

    @Inject
    public OrderStore(Dispatcher dispatcher, OrderRestClient orderRestClient, OrderSqlUtils orderSqlUtils) {
        super(dispatcher);
        mOrderRestClient = orderRestClient;
        mOrderSqlUtils = orderSqlUtils;
    }

    @Override
    public void onRegister() {
        AppLog.d(T.API, "OrderStore onRegister");
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onAction(Action action) {
        IAction actionType = action.getType();
        if (!(actionType instanceof OrderAction)) {
            return;
        }

        switch ((OrderAction) actionType) {
            case FETCH_ORDER_LIST:
                handleFetchOrderList((FetchOrderListPayload) action.getPayload());
                break;
        }
    }

    private void handleFetchOrderList(FetchOrderListPayload payload) {
        if (payload.listDescriptor instanceof OrderListDescriptorForRestBuyer) {
            OrderListDescriptorForRestBuyer descriptor = (OrderListDescriptorForRestBuyer) payload.listDescriptor;
            mOrderRestClient.fetchOrderList(descriptor, payload.offset);
        }
    }
}
