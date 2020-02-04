package com.plutonem.android.fluxc.store;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.plutonem.android.fluxc.Dispatcher;
import com.plutonem.android.fluxc.Payload;
import com.plutonem.android.fluxc.action.OrderAction;
import com.plutonem.android.fluxc.annotations.action.Action;
import com.plutonem.android.fluxc.annotations.action.IAction;
import com.plutonem.android.fluxc.generated.ListActionBuilder;
import com.plutonem.android.fluxc.generated.OrderActionBuilder;
import com.plutonem.android.fluxc.model.BuyerModel;
import com.plutonem.android.fluxc.model.CauseOfOnOrderChanged;
import com.plutonem.android.fluxc.model.LocalOrRemoteId;
import com.plutonem.android.fluxc.model.LocalOrRemoteId.LocalId;
import com.plutonem.android.fluxc.model.OrderModel;
import com.plutonem.android.fluxc.model.list.OrderListDescriptor;
import com.plutonem.android.fluxc.model.list.OrderListDescriptor.OrderListDescriptorForRestBuyer;
import com.plutonem.android.fluxc.model.order.OrderStatus;
import com.plutonem.android.fluxc.network.BaseRequest.BaseNetworkError;
import com.plutonem.android.fluxc.network.rest.plutonem.order.OrderRestClient;
import com.plutonem.android.fluxc.persistence.OrderSqlUtils;
import com.plutonem.android.fluxc.store.ListStore.FetchedListItemsPayload;
import com.plutonem.android.fluxc.store.ListStore.ListError;
import com.plutonem.android.fluxc.store.ListStore.ListErrorType;
import com.plutonem.android.fluxc.store.ListStore.ListItemsChangedPayload;
import com.wellsql.generated.OrderModelTable;
import com.yarolegovich.wellsql.SelectQuery;
import com.yarolegovich.wellsql.WellSql;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.AppLog.T;
import org.wordpress.android.util.DateTimeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

    public static class OrderListItem {
        public Long remoteOrderId;
        public String lastModified;
        public String status;

        public OrderListItem(Long remoteOrderId, String lastModified, String status) {
            this.remoteOrderId = remoteOrderId;
            this.lastModified = lastModified;
            this.status = status;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class FetchOrderListResponsePayload extends Payload<OrderError> {
        @NotNull
        public OrderListDescriptor listDescriptor;
        @NotNull
        public List<OrderListItem> orderListItems;
        public boolean loadedMore;
        public boolean canLoadMore;

        public FetchOrderListResponsePayload(@NonNull OrderListDescriptor listDescriptor,
                                             @NonNull List<OrderListItem> orderListItems,
                                             boolean loadedMore,
                                             boolean canLoadMore,
                                             @Nullable OrderError error) {
            this.listDescriptor = listDescriptor;
            this.orderListItems = orderListItems;
            this.loadedMore = loadedMore;
            this.canLoadMore = canLoadMore;
            this.error = error;
        }
    }

    public static class RemoteOrderPayload extends Payload<OrderError> {
        public OrderModel order;
        public BuyerModel buyer;

        public RemoteOrderPayload(OrderModel order, BuyerModel buyer) {
            this.order = order;
            this.buyer = buyer;
        }
    }

    public static class RemoteInfoPayload extends Payload<OrderError> {
        public String info;
        public OrderModel order;
        public BuyerModel buyer;

        public RemoteInfoPayload(String info, OrderModel order, BuyerModel buyer) {
            this.info = info;
            this.order = order;
            this.buyer = buyer;
        }
    }

    public static class RemoteResultPayload extends Payload<OrderError> {
        public String resultInfo;
        public String resultStatus;
        public String requestInfo;
        public OrderModel order;
        public BuyerModel buyer;

        public RemoteResultPayload(String resultInfo, String resultStatus, String requestInfo, OrderModel order, BuyerModel buyer) {
            this.resultInfo = resultInfo;
            this.resultStatus = resultStatus;
            this.requestInfo = requestInfo;
            this.order = order;
            this.buyer = buyer;
        }
    }

    public static class FetchOrderResponsePayload extends RemoteOrderPayload {
        public OrderAction origin = OrderAction.FETCH_ORDER; // Only used to track fetching newly uploaded XML-RPC orders

        public FetchOrderResponsePayload(OrderModel order, BuyerModel buyer) {
            super(order, buyer);
        }
    }

    public static class OrderError implements OnChangedError {
        public OrderErrorType type;
        public String message;

        public OrderError(OrderErrorType type, @NonNull String message) {
            this.type = type;
            this.message = message;
        }

        public OrderError(@NonNull String type, @NonNull String message) {
            this.type = OrderErrorType.fromString(type);
            this.message = message;
        }

        public OrderError(OrderErrorType type) {
            this(type, "");
        }
    }

    // OnChanged events
    public static class OnOrderChanged extends OnChanged<OrderError> {
        public final int rowsAffected;
        public final boolean canLoadMore;
        public final CauseOfOnOrderChanged causeOfChange;

        public OnOrderChanged(CauseOfOnOrderChanged causeOfChange, int rowsAffected) {
            this.causeOfChange = causeOfChange;
            this.rowsAffected = rowsAffected;
            this.canLoadMore = false;
        }
    }

    public static class OnOrderSubmitted extends OnChanged<OrderError> {
        public OrderModel order;

        public OnOrderSubmitted(OrderModel order) {
            this.order = order;
        }
    }

    public static class OnInfoEncrypted extends OnChanged<OrderError> {
        public String info;
        public OrderModel order;
        public BuyerModel buyer;

        public OnInfoEncrypted(String info, OrderModel order, BuyerModel buyer) {
            this.info = info;
            this.order = order;
            this.buyer = buyer;
        }
    }

    public enum OrderErrorType {
//        UNKNOWN_ORDER,
//        INVALID_RESPONSE,
        GENERIC_ERROR;

        public static OrderErrorType fromString(String string) {
            if (string != null) {
                for (OrderErrorType v : OrderErrorType.values()) {
                    if (string.equalsIgnoreCase(v.name())) {
                        return v;
                    }
                }
            }
            return GENERIC_ERROR;
        }
    }

    private final OrderRestClient mOrderRestClient;
    private final OrderSqlUtils mOrderSqlUtils;
    // Ensures that the UploadStore is initialized whenever the OrderStore is,
    // to ensure actions are shadowed and repeated by the UploadStore
    @SuppressWarnings("unused")
    @Inject
    UploadStore mUploadStore;

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

    public OrderModel instantiateOrderModel(BuyerModel buyer, String orderFormat) {
        OrderModel order = new OrderModel();
        order.setLocalBuyerId(buyer.getId());
        order.setIsLocalDraft(true);
        order.setDateLocallyChanged((DateTimeUtils.iso8601UTCFromDate(new Date())));
        order.setOrderFormat(orderFormat);

        // Insert the order into the db, updating the object to include the local ID
        order = mOrderSqlUtils.insertOrderForResult(order);

        // id is set to -1 if insertion fails
        if (order.getId() == -1) {
            return null;
        }
        return order;
    }

    /**
     * Given a local ID for a order, returns that order as a {@link OrderModel}.
     */
    public OrderModel getOrderByLocalOrderId(int localId) {
        List<OrderModel> result = WellSql.select(OrderModel.class)
                                         .where().equals(OrderModelTable.ID, localId).endWhere()
                                         .getAsModel();
        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    public List<OrderModel> getOrdersByLocalOrRemoteOrderIds(List<? extends LocalOrRemoteId> localOrRemoteIds,
                                                             BuyerModel buyer) {
        if (localOrRemoteIds == null || buyer == null) {
            return Collections.emptyList();
        }
        return mOrderSqlUtils.getOrdersByLocalOrRemoteOrderIds(localOrRemoteIds, buyer.getId());
    }

    /**
     * Given a list of remote IDs for a order and the buyer to which it belongs, returns the orders as map where the
     * key is the remote order ID and the value is the {@link OrderModel}.
     */
    private Map<Long, OrderModel> getOrdersByRemoteOrderIds(List<Long> remoteIds, BuyerModel buyer) {
        if (buyer == null) {
            return Collections.emptyMap();
        }
        List<OrderModel> orderList = mOrderSqlUtils.getOrdersByRemoteIds(remoteIds, buyer.getId());
        Map<Long, OrderModel> orderMap = new HashMap<>(orderList.size());
        for (OrderModel order : orderList) {
            orderMap.put(order.getRemoteOrderId(), order);
        }
        return orderMap;
    }

    /**
     * Returns the local orders for the given order list descriptor.
     */
    public @NonNull
    List<LocalId> getLocalOrderIdsForDescriptor(OrderListDescriptor orderListDescriptor) {
        return Collections.emptyList();
//        String orderBy = null;
//        switch (orderListDescriptor.getOrderBy()) {
//            case DATE:
//                orderBy = OrderModelTable.DATE_CREATED;
//                break;
//            case ID:
//                orderBy = OrderModelTable.ID;
//                break;
//        }
//        int order;
//        if (orderListDescriptor.getOrder() == ListOrder.ASC) {
//            order = SelectQuery.ORDER_ASCENDING;
//        } else {
//            order = SelectQuery.ORDER_DESCENDING;
//        }
//        return mOrderSqlUtils.getLocalOrderIdsForFilter(orderListDescriptor.getBuyer(), orderBy, order);
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
            case FETCHED_ORDER_LIST:
                handleFetchedOrderList((FetchOrderListResponsePayload) action.getPayload());
                break;
            case FETCH_ORDER:
                fetchOrder((RemoteOrderPayload) action.getPayload());
                break;
            case FETCHED_ORDER:
                handleFetchSingleOrderCompleted((FetchOrderResponsePayload) action.getPayload());
                break;
            case PUSH_ORDER:
                pushOrder((RemoteOrderPayload) action.getPayload());
                break;
            case PUSHED_ORDER:
                handlePushOrderCompleted((RemoteOrderPayload) action.getPayload());
                break;
            case SIGN_INFO:
                signInfo((RemoteOrderPayload) action.getPayload());
            case SIGNED_INFO:
                handleSignInfoCompleted((RemoteInfoPayload) action.getPayload());
            case DECRYPT_RESULT:
                decryptResult((RemoteResultPayload) action.getPayload());
            case UPDATE_ORDER:
                updateOrder((OrderModel) action.getPayload(), true);
                break;
            case REMOVE_ORDER:
                removeOrder((OrderModel) action.getPayload());
                break;
        }
    }

    private void fetchOrder(RemoteOrderPayload payload) {
        if (payload.buyer.isUsingPnRestApi()) {
            mOrderRestClient.fetchOrder(payload.order, payload.buyer);
        }
    }

    private void handleFetchOrderList(FetchOrderListPayload payload) {
        if (payload.listDescriptor instanceof OrderListDescriptorForRestBuyer) {
            OrderListDescriptorForRestBuyer descriptor = (OrderListDescriptorForRestBuyer) payload.listDescriptor;
            mOrderRestClient.fetchOrderList(descriptor, payload.offset);
        }
    }

    private void handleFetchedOrderList(FetchOrderListResponsePayload payload) {
        ListError fetchedListItemsError = null;
        List<Long> orderIds;
        if (payload.isError()) {
            ListErrorType errorType = ListErrorType.GENERIC_ERROR;
            fetchedListItemsError = new ListError(errorType, payload.error.message);
            orderIds = Collections.emptyList();
        } else {
            orderIds = new ArrayList<>(payload.orderListItems.size());
            BuyerModel buyer = payload.listDescriptor.getBuyer();
            for (OrderListItem item : payload.orderListItems) {
                orderIds.add(item.remoteOrderId);
            }
            Map<Long, OrderModel> orders = getOrdersByRemoteOrderIds(orderIds, buyer);
            for (OrderListItem item : payload.orderListItems) {
                OrderModel order = orders.get(item.remoteOrderId);
                if (order == null) {
                    // Order doesn't exist in the DB, nothing to do.
                    continue;
                }

                // Check if the order's last modified date or status have changed.
                // We need to check status separately.
                boolean isOrderChanged =
                        !order.getLastModified().equals(item.lastModified)
                                || !order.getStatus().equals(item.status);

                if (isOrderChanged) {
                    // Dispatch a fetch action for the orders that are changed.
                    mDispatcher.dispatch(OrderActionBuilder.newFetchOrderAction(new RemoteOrderPayload(order, buyer)));
                }
            }
        }

        FetchedListItemsPayload fetchedListItemsPayload =
                new FetchedListItemsPayload(payload.listDescriptor, orderIds,
                        payload.loadedMore, payload.canLoadMore, fetchedListItemsError);
        mDispatcher.dispatch(ListActionBuilder.newFetchedListItemsAction(fetchedListItemsPayload));
    }

    private void handleFetchSingleOrderCompleted(FetchOrderResponsePayload payload) {
        if (payload.isError()) {
            OnOrderChanged event = new OnOrderChanged(
                    new CauseOfOnOrderChanged.UpdateOrder(payload.order.getId(), payload.order.getRemoteOrderId()), 0);
            event.error = payload.error;
            emitChange(event);
        } else {
            updateOrder(payload.order, false);
        }
    }

    private void handlePushOrderCompleted(RemoteOrderPayload payload) {
        if (payload.isError()) {
            OnOrderSubmitted onOrderSubmitted = new OnOrderSubmitted(payload.order);
            onOrderSubmitted.error = payload.error;
            emitChange(onOrderSubmitted);
        } else {
            if (payload.buyer.isUsingPnRestApi()) {
                // The PN.COM REST API response contains the modified order, so we're already in sync with the server
                // All we need to do is store it and emit OnOrderChanged
                updateOrder(payload.order, false);
                emitChange(new OnOrderSubmitted(payload.order));
            }
        }
    }

    private void handleSignInfoCompleted(RemoteInfoPayload payload) {
        if (payload.isError()) {
            OnOrderSubmitted onOrderSubmitted = new OnOrderSubmitted(payload.order);
            onOrderSubmitted.error = payload.error;
            emitChange(onOrderSubmitted);
        } else {
            if (payload.buyer.isUsingPnRestApi()) {
                emitChange(new OnInfoEncrypted(payload.info, payload.order, payload.buyer));
            }
        }
    }

    private void pushOrder(RemoteOrderPayload payload) {
        if (payload.buyer.isUsingPnRestApi()) {
            mOrderRestClient.pushOrder(payload.order, payload.buyer);
        }
    }

    private void signInfo(RemoteOrderPayload payload) {
        if (payload.buyer.isUsingPnRestApi()) {
            mOrderRestClient.signInfo(payload.order, payload.buyer);
        }
    }

    private void decryptResult(RemoteResultPayload payload) {
        if (payload.buyer.isUsingPnRestApi()) {
            mOrderRestClient.decryptResult(payload.resultInfo, payload.resultStatus, payload.requestInfo, payload.order, payload.buyer);
        }
    }

    private void updateOrder(OrderModel order, boolean changeLocalDate) {
        if (changeLocalDate) {
            order.setDateLocallyChanged((DateTimeUtils.iso8601UTCFromDate(new Date())));
        }
        int rowsAffected = mOrderSqlUtils.insertOrUpdateOrderOverwritingLocalChanges(order);
        CauseOfOnOrderChanged causeOfChange = new CauseOfOnOrderChanged.UpdateOrder(order.getId(), order.getRemoteOrderId());
        OnOrderChanged onOrderChanged = new OnOrderChanged(causeOfChange, rowsAffected);
        emitChange(onOrderChanged);

        mDispatcher.dispatch(ListActionBuilder.newListItemsChangedAction(
                new ListItemsChangedPayload(OrderListDescriptor.calculateTypeIdentifier(order.getLocalBuyerId()))));
    }

    private void removeOrder(OrderModel order) {
        if (order == null) {
            return;
        }
        int rowsAffected = mOrderSqlUtils.deleteOrder(order);

        CauseOfOnOrderChanged causeOfChange = new CauseOfOnOrderChanged.RemoveOrder(order.getId(), order.getRemoteOrderId());
        OnOrderChanged onOrderChanged = new OnOrderChanged(causeOfChange, rowsAffected);
        emitChange(onOrderChanged);
    }
}
