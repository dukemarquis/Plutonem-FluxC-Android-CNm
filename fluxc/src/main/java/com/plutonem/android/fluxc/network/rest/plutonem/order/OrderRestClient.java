package com.plutonem.android.fluxc.network.rest.plutonem.order;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.plutonem.android.fluxc.Dispatcher;
import com.plutonem.android.fluxc.action.OrderAction;
import com.plutonem.android.fluxc.generated.OrderActionBuilder;
import com.plutonem.android.fluxc.generated.SubmitActionBuilder;
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
import com.plutonem.android.fluxc.store.OrderStore.FetchOrderListResponsePayload;
import com.plutonem.android.fluxc.store.OrderStore.FetchOrderResponsePayload;
import com.plutonem.android.fluxc.store.OrderStore.OrderError;
import com.plutonem.android.fluxc.store.OrderStore.OrderListItem;
import com.plutonem.android.fluxc.store.OrderStore.RemoteOrderPayload;
import com.plutonem.android.fluxc.store.OrderStore.RemoteInfoPayload;

import org.wordpress.android.util.StringUtils;

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

    public void pushOrder(final OrderModel order, final BuyerModel buyer) {
        String url;

        if (order.isLocalDraft()) {
            url = PLUTONEMREST.buyers.buyer(buyer.getBuyerId()).orders.new_.getUrlV1_2();
        } else {
            url = PLUTONEMREST.buyers.buyer(buyer.getBuyerId()).orders.new_.getUrlV1_2();
        }

        Map<String, Object> body = orderModelToParams(order);

        final PlutonemGsonRequest<OrderPNComRestResponse> request = PlutonemGsonRequest.buildPostRequest(url, body,
                OrderPNComRestResponse.class,
                new Listener<OrderPNComRestResponse>() {
                    @Override
                    public void onResponse(OrderPNComRestResponse response) {
                        OrderModel submittedOrder = orderResponseToOrderModel(response);

                        submittedOrder.setIsLocalDraft(false);
                        submittedOrder.setIsLocallyChanged(false);
                        submittedOrder.setId(order.getId());
                        submittedOrder.setLocalBuyerId(buyer.getId());

                        RemoteOrderPayload payload = new RemoteOrderPayload(submittedOrder, buyer);
                        mDispatcher.dispatch(SubmitActionBuilder.newPushedOrderAction(payload));
                    }
                },
                new PlutonemErrorListener() {
                    @Override
                    public void onErrorResponse(@NonNull PlutonemGsonNetworkError error) {
                        // Possible non-generic errors: 404 unknown_order (invalid order ID)
                        RemoteOrderPayload payload = new RemoteOrderPayload(order, buyer);
                        payload.error = new OrderError(error.apiError, error.message);
                        mDispatcher.dispatch(SubmitActionBuilder.newPushedOrderAction(payload));
                    }
                }
        );

        request.addQueryParameter("context", "edit");

        request.disableRetries();
        add(request);
    }

    public void signInfo(final OrderModel order, final BuyerModel buyer) {
        String url;

        url = PLUTONEMREST.buyers.buyer(buyer.getBuyerId()).orders.sign_.getUrlV1_2();

        Map<String, Object> body = orderModelToParams(order);

        final PlutonemGsonRequest<InfoPNComRestResponse> request = PlutonemGsonRequest.buildPostRequest(url, body,
                InfoPNComRestResponse.class,
                new Listener<InfoPNComRestResponse>() {
                    @Override
                    public void onResponse(InfoPNComRestResponse response) {
                        String encryptedInfo = infoResponseToInfoString(response);

                        RemoteInfoPayload payload = new RemoteInfoPayload(encryptedInfo, order, buyer);
                        mDispatcher.dispatch(SubmitActionBuilder.newSignedInfoAction(payload));
                    }
                },
                new PlutonemErrorListener() {
                    @Override
                    public void onErrorResponse(@NonNull PlutonemGsonNetworkError error) {
                        RemoteInfoPayload payload = new RemoteInfoPayload(null, order, buyer);
                        payload.error = new OrderError(error.apiError, error.message);
                        mDispatcher.dispatch(SubmitActionBuilder.newSignedInfoAction(payload));
                    }
                }
        );

        request.addQueryParameter("context", "edit");

        request.disableRetries();
        add(request);
    }

    public void decryptResult(final String resultInfo, final String resultStatus, final String requestInfo, final OrderModel order,final BuyerModel buyer) {
        String url;

        url = PLUTONEMREST.buyers.buyer(buyer.getBuyerId()).orders.decrypt_.getUrlV1_2();

        Map<String, Object> body = makeParams(resultInfo, resultStatus, requestInfo);

        final PlutonemGsonRequest<String> request = PlutonemGsonRequest.buildPostRequest(url, body,
                String.class,
                new Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                new PlutonemErrorListener() {
                    @Override
                    public void onErrorResponse(@NonNull PlutonemGsonNetworkError error) {

                    }
                }
        );

        request.addQueryParameter("context", "edit");

        request.disableRetries();
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

    private String infoResponseToInfoString(InfoPNComRestResponse from) {
        String info;
        info = from.getInfo();

        return info;
    }

    private Map<String, Object> orderModelToParams(OrderModel order) {
        Map<String, Object> params = new HashMap<>();

        params.put("status", StringUtils.notNullStr(order.getStatus()));
        params.put("order_name", StringUtils.notNullStr(order.getOrderName()));
        params.put("order_phone_number", StringUtils.notNullStr(order.getOrderPhoneNumber()));
        params.put("order_address", StringUtils.notNullStr(order.getOrderAddress()));


        if (!TextUtils.isEmpty(order.getDateCreated())) {
            params.put("date", order.getDateCreated());
        }

        params.put("shop_name", StringUtils.notNullStr(order.getShopTitle()));
        params.put("product_name", StringUtils.notNullStr(order.getProductDetail()));
        params.put("item_sales_price", StringUtils.notNullStr(order.getItemSalesPrice()));
        params.put("order_number", StringUtils.notNullStr(String.valueOf(order.getOrderNumber())));
        params.put("item_distribution_mode", StringUtils.notNullStr(order.getItemDistributionMode()));


        // We are not adding `lastModified` date to the params because that should be updated by the server when there
        // is a change in the order. This is tested for both Calypso and PNAndroid on 01/10/2020 and verified that it's
        // working as expected. I am only adding this note here to avoid a possible confusion about it in the future.
        if (!TextUtils.isEmpty(order.getOrderFormat())) {
            params.put("format", order.getOrderFormat());
        }

        return params;
    }

    private Map<String, Object> makeParams(String resultInfo, String resultStatus, String requestInfo) {
        Map<String, Object> params = new HashMap<>();

        params.put("result_info", StringUtils.notNullStr(resultInfo));
        params.put("result_status", StringUtils.notNullStr(resultStatus));
        params.put("request_info", StringUtils.notNullStr(requestInfo));

        return params;
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
