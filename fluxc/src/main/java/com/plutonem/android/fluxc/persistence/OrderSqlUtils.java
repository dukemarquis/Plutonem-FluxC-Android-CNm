package com.plutonem.android.fluxc.persistence;

import com.plutonem.android.fluxc.model.BuyerModel;
import com.plutonem.android.fluxc.model.LocalOrRemoteId.LocalId;
import com.plutonem.android.fluxc.model.OrderModel;
import com.wellsql.generated.OrderModelTable;
import com.yarolegovich.wellsql.ConditionClauseBuilder;
import com.yarolegovich.wellsql.SelectQuery;
import com.yarolegovich.wellsql.SelectQuery.Order;
import com.yarolegovich.wellsql.WellSql;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Reusable;

@Reusable
public class OrderSqlUtils {
    @Inject
    public OrderSqlUtils() {
    }

    public List<LocalId> getLocalPostIdsForFilter(BuyerModel buyer, String orderBy, @Order int order) {
        ConditionClauseBuilder<SelectQuery<OrderModel>> clauseBuilder =
                WellSql.select(OrderModel.class)
                        // We only need the local ids
                        .columns(OrderModelTable.ID)
                        .where().beginGroup()
                        .equals(OrderModelTable.LOCAL_BUYER_ID, buyer.getId())
                        .endGroup();
        /*
         * Remember that, since we are only querying the `OrderModelTable.ID` column, the rest of the fields for the
         * post won't be there which is exactly what we want.
         */
        List<OrderModel> localPosts = clauseBuilder.endWhere().orderBy(orderBy, order).getAsModel();
        List<LocalId> localPostIds = new ArrayList<>();
        for (OrderModel post : localPosts) {
            localPostIds.add(new LocalId(post.getId()));
        }
        return localPostIds;
    }
}
