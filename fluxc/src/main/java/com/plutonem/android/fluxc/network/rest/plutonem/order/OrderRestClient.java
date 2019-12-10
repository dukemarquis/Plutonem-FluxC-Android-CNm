package com.plutonem.android.fluxc.network.rest.plutonem.order;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.plutonem.android.fluxc.Dispatcher;
import com.plutonem.android.fluxc.generated.OrderActionBuilder;
import com.plutonem.android.fluxc.generated.endpoint.PLUTONEMREST;
import com.plutonem.android.fluxc.model.BuyerModel;
import com.plutonem.android.fluxc.model.OrderModel;
import com.plutonem.android.fluxc.model.list.AccountFilter;
import com.plutonem.android.fluxc.model.list.OrderListDescriptor.OrderListDescriptorForRestBuyer;
import com.plutonem.android.fluxc.model.order.OrderStatus;
import com.plutonem.android.fluxc.network.UserAgent;
import com.plutonem.android.fluxc.network.rest.plutonem.BasePlutonemRestClient;
import com.plutonem.android.fluxc.network.rest.plutonem.PlutonemGsonRequest;
import com.plutonem.android.fluxc.network.rest.plutonem.PlutonemGsonRequest.PlutonemErrorListener;
import com.plutonem.android.fluxc.network.rest.plutonem.PlutonemGsonRequest.PlutonemGsonNetworkError;
import com.plutonem.android.fluxc.network.rest.plutonem.auth.AccessToken;
import com.plutonem.android.fluxc.network.rest.plutonem.order.OrderPNComRestResponse.OrdersResponse;
import com.plutonem.android.fluxc.store.OrderStore.OrderError;
import com.plutonem.android.fluxc.store.OrderStore.FetchOrderResponsePayload;
import com.plutonem.android.fluxc.store.OrderStore.FetchOrderListResponsePayload;
import com.plutonem.android.fluxc.store.OrderStore.OrderListItem;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

@Singleton
public class OrderRestClient extends BasePlutonemRestClient {
    public OrderRestClient(Context appContext, Dispatcher dispatcher, RequestQueue requestQueue, AccessToken accessToken,
                           UserAgent userAgent) {
        super(appContext, dispatcher, requestQueue, accessToken, userAgent);
    }

    public void fetchOrder(final OrderModel order, final BuyerModel buyer) {
        String url = PLUTONEMREST.buyers.buyer(buyer.getBuyerId()).orders.order(order.getRemoteOrderId()).getUrlV1_1();

        Map<String, String> params = new HashMap<>();

        params.put("context", "display");

        final PlutonemGsonRequest<OrderPNComRestResponse> request = PlutonemGsonRequest.buildGetRequest(url, params,
                OrderPNComRestResponse.class,
                new Listener<OrderPNComRestResponse>() {
                    @Override
                    public void onResponse(OrderPNComRestResponse response) {
                        OrderModel fetchedOrder = orderResponseToOrderModel(response);
                        fetchedOrder.setId(order.getId());
                        fetchedOrder.setLocalBuyerId(buyer.getId());

                        FetchOrderResponsePayload payload = new FetchOrderResponsePayload(fetchedOrder, buyer);
                        payload.order = fetchedOrder;

                        mDispatcher.dispatch(OrderActionBuilder.newFetchedOrderAction(payload));
                    }
                },
                new PlutonemErrorListener() {
                    @Override
                    public void onErrorResponse(@NonNull PlutonemGsonNetworkError error) {
                        // Possible non-generic errors: 404 unknown_post (invalid post ID)
                        FetchOrderResponsePayload payload = new FetchOrderResponsePayload(order, buyer);
                        payload.error = new OrderError(error.apiError, error.message);
                        mDispatcher.dispatch(OrderActionBuilder.newFetchedOrderAction(payload));
                    }
                }
        );
        add(request);
    }

    public void fetchOrderList(final OrderListDescriptorForRestBuyer listDescriptor, final long offset) {
        String url = PLUTONEMREST.buyers.buyer(listDescriptor.getBuyer().getBuyerId()).orders.getUrlV1_1();

        final int pageSize = listDescriptor.getConfig().getNetworkPageSize();
        String fields = TextUtils.join(",", Arrays.asList("ID", "modified", "status"));
        Map<String, String> params =
                createFetchOrderListParameters(false, offset, pageSize, listDescriptor.getStatusList(),
                        listDescriptor.getAccount(), fields, listDescriptor.getOrder().getValue(),
                        listDescriptor.getOrderBy().getValue());

        final boolean loadedMore = offset > 0;

        final PlutonemGsonRequest<OrdersResponse> request = PlutonemGsonRequest.buildGetRequest(url, params,
                OrdersResponse.class,
                new Listener<OrdersResponse>() {
                    @Override
                    public void onResponse(OrdersResponse response) {
                        List<OrderListItem> orderListItems = new ArrayList<>(response.getOrders().size());
                        for (OrderPNComRestResponse orderResponse : response.getOrders()) {
                            orderListItems
                                    .add(new OrderListItem(orderResponse.getRemoteOrderId(), orderResponse.getModified(),
                                            orderResponse.getStatus()));
                        }
                        boolean canLoadMore = orderListItems.size() == pageSize;
                        FetchOrderListResponsePayload responsePayload =
                                new FetchOrderListResponsePayload(listDescriptor, orderListItems, loadedMore,
                                        canLoadMore, null);
                        mDispatcher.dispatch(OrderActionBuilder.newFetchedOrderListAction(responsePayload));
                    }
                },
                new PlutonemErrorListener() {
                    @Override
                    public void onErrorResponse(@NonNull PlutonemGsonNetworkError error) {
                        OrderError orderError = new OrderError(error.apiError, error.message);
                        FetchOrderListResponsePayload responsePayload =
                                new FetchOrderListResponsePayload(listDescriptor, Collections.<OrderListItem>emptyList(),
                                        loadedMore, false, orderError);
                        mDispatcher.dispatch(OrderActionBuilder.newFetchedOrderListAction(responsePayload));
                    }
                });
        add(request);
    }

    private OrderModel orderResponseToOrderModel(OrderPNComRestResponse from) {
        OrderModel order = new OrderModel();
        order.setRemoteOrderId(from.getRemoteOrderId());
        order.setRemoteBuyerId(from.getRemoteBuyerId());
        order.setDateCreated(from.getDate());
        order.setLastModified(from.getModified());
        order.setRemoteLastModified(from.getModified());
        order.setShopTitle(from.getShopTitle());
        order.setProductDetail(from.getProductName());
        order.setOrderDetail(from.getOrderDetail());
        order.setStatus(from.getStatus());

        if (from.getAccount() != null) {
            order.setAccountId(from.getAccount().getId());
            order.setAccountDisplayName(from.getAccount().getName());
        }

        return order;
    }

    private Map<String, String> createFetchOrderListParameters(final boolean getPages,
                                                               final long offset,
                                                               final int number,
                                                               @Nullable final List<OrderStatus> statusList,
                                                               @Nullable AccountFilter accountFilter,
                                                               @Nullable final String fields,
                                                               @Nullable final String order,
                                                               @Nullable final String orderBy) {
        Map<String, String> params = new HashMap<>();

        params.put("number", String.valueOf(number));

        if (getPages) {
            params.put("type", "page");
        }

        if (!TextUtils.isEmpty(order)) {
            params.put("order", order);
        }
        if (!TextUtils.isEmpty(orderBy)) {
            params.put("order_by", orderBy);
        }
        if (statusList != null && statusList.size() > 0) {
            params.put("status", OrderStatus.orderStatusListToString(statusList));
        }
        if (offset > 0) {
            params.put("offset", String.valueOf(offset));
        }

        if (!TextUtils.isEmpty(fields)) {
            params.put("fields", fields);
        }

        return params;
    }
}
