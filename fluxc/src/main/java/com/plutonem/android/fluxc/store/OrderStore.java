package com.plutonem.android.fluxc.store;

import com.plutonem.android.fluxc.Dispatcher;
import com.plutonem.android.fluxc.annotations.action.Action;
import com.plutonem.android.fluxc.model.order.OrderStatus;
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

    }
}
