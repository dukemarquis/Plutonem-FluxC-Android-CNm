package com.plutonem.android.fluxc.persistence;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.plutonem.android.fluxc.model.BuyerModel;
import com.plutonem.android.fluxc.model.LocalOrRemoteId;
import com.plutonem.android.fluxc.model.LocalOrRemoteId.LocalId;
import com.plutonem.android.fluxc.model.LocalOrRemoteId.RemoteId;
import com.plutonem.android.fluxc.model.OrderModel;
import com.wellsql.generated.OrderModelTable;
import com.yarolegovich.wellsql.ConditionClauseBuilder;
import com.yarolegovich.wellsql.SelectQuery;
import com.yarolegovich.wellsql.SelectQuery.Order;
import com.yarolegovich.wellsql.WellSql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import javax.inject.Inject;

import dagger.Reusable;

@Reusable
public class OrderSqlUtils {
    @Inject
    public OrderSqlUtils() {
    }

    public int insertOrUpdateOrder(OrderModel order, boolean overwriteLocalChanges) {
        if (order == null) {
            return 0;
        }

        List<OrderModel> orderResult;
        if (order.isLocalDraft()) {
            orderResult = WellSql.select(OrderModel.class)
                    .where()
                    .equals(OrderModelTable.ID, order.getId())
                    .endWhere().getAsModel();
        } else {
            orderResult = WellSql.select(OrderModel.class)
                    .where().beginGroup()
                    .equals(OrderModelTable.ID, order.getId())
                    .or()
                    .beginGroup()
                    .equals(OrderModelTable.REMOTE_ORDER_ID, order.getRemoteOrderId())
                    .equals(OrderModelTable.LOCAL_BUYER_ID, order.getLocalBuyerId())
                    .endGroup()
                    .endGroup().endWhere().getAsModel();
        }
        int numberOfDeletedRows = 0;
        if (orderResult.isEmpty()) {
            // insert
            WellSql.insert(order).asSingleTransaction(true).execute();
            return 1;
        } else {
            if (orderResult.size() > 1) {
                // We've ended up with a duplicate entry, probably due to a push/fetch race
                // condition. One matches based on local ID (this is the one we're trying to
                // update with a remote order ID). The other matches based on local buyer ID +
                // remote order ID, and we got it from a fetch. Just remove the duplicated
                // entry we got from the fetch as the chance the client app is already using it is
                // lower (it was most probably fetched a few ms ago).
                ListIterator<OrderModel> postModelListIterator = orderResult.listIterator();
                while (postModelListIterator.hasNext()) {
                    OrderModel item = postModelListIterator.next();
                    if (item.getId() != order.getId()) {
                        WellSql.delete(OrderModel.class).whereId(item.getId());
                        postModelListIterator.remove();
                        numberOfDeletedRows++;
                    }
                }
            }
            int oldId = orderResult.get(0).getId();
            // Update only if local changes for this order don't exist
            if (overwriteLocalChanges || !orderResult.get(0).isLocallyChanged()) {
                return WellSql.update(OrderModel.class).whereId(oldId)
                        .put(order, new UpdateAllExceptId<>(OrderModel.class)).execute()
                        + numberOfDeletedRows;
            }
        }
        return numberOfDeletedRows;
    }

    public int insertOrUpdateOrderOverwritingLocalChanges(OrderModel post) {
        return insertOrUpdateOrder(post, true);
    }

    public List<OrderModel> getOrdersByRemoteIds(@Nullable List<Long> remoteIds, int localBuyerId) {
        if (remoteIds != null && remoteIds.size() > 0) {
            return WellSql.select(OrderModel.class)
                    .where().isIn(OrderModelTable.REMOTE_ORDER_ID, remoteIds)
                    .equals(OrderModelTable.LOCAL_BUYER_ID, localBuyerId).endWhere()
                    .getAsModel();
        }
        return Collections.emptyList();
    }

    public List<OrderModel> getOrdersByLocalOrRemoteOrderIds(
            @NonNull List<? extends LocalOrRemoteId> localOrRemoteIds, int localBuyerId) {
        if (localOrRemoteIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> localIds = new ArrayList<>();
        List<Long> remoteIds = new ArrayList<>();
        for (LocalOrRemoteId localOrRemoteId : localOrRemoteIds) {
            if (localOrRemoteId instanceof LocalId) {
                localIds.add(((LocalId) localOrRemoteId).getValue());
            } else if (localOrRemoteId instanceof RemoteId) {
                remoteIds.add(((RemoteId) localOrRemoteId).getValue());
            }
        }
        ConditionClauseBuilder<SelectQuery<OrderModel>> whereQuery =
                WellSql.select(OrderModel.class).where().equals(OrderModelTable.LOCAL_BUYER_ID, localBuyerId).beginGroup();
        boolean addIsInLocalIdsCondition = !localIds.isEmpty();
        if (addIsInLocalIdsCondition) {
            whereQuery = whereQuery.isIn(OrderModelTable.ID, localIds);
        }
        if (!remoteIds.isEmpty()) {
            if (addIsInLocalIdsCondition) {
                // Add `or` only if we are checking for both local and remote ids
                whereQuery = whereQuery.or();
            }
            whereQuery = whereQuery.isIn(OrderModelTable.REMOTE_ORDER_ID, remoteIds);
        }
        return whereQuery.endGroup().endWhere().getAsModel();
    }

    public OrderModel insertOrderForResult(OrderModel order) {
        WellSql.insert(order).asSingleTransaction(true).execute();

        return order;
    }

    public int deleteOrder(OrderModel order) {
        if (order == null) {
            return 0;
        }

        return WellSql.delete(OrderModel.class)
                .where().beginGroup()
                .equals(OrderModelTable.ID, order.getId())
                .equals(OrderModelTable.LOCAL_BUYER_ID, order.getLocalBuyerId())
                .endGroup()
                .endWhere()
                .execute();
    }

    public List<LocalId> getLocalOrderIdsForFilter(BuyerModel buyer, String orderBy, @Order int order) {
        return Collections.emptyList();
//        ConditionClauseBuilder<SelectQuery<OrderModel>> clauseBuilder =
//                WellSql.select(OrderModel.class)
//                        // We only need the local ids
//                        .columns(OrderModelTable.ID)
//                        .where().beginGroup()
//                        .equals(OrderModelTable.LOCAL_BUYER_ID, buyer.getId())
//                        .endGroup();
//        /*
//         * Remember that, since we are only querying the `OrderModelTable.ID` column, the rest of the fields for the
//         * post won't be there which is exactly what we want.
//         */
//        List<OrderModel> localPosts = clauseBuilder.endWhere().orderBy(orderBy, order).getAsModel();
//        List<LocalId> localPostIds = new ArrayList<>();
//        for (OrderModel post : localPosts) {
//            localPostIds.add(new LocalId(post.getId()));
//        }
//        return localPostIds;
    }
}
