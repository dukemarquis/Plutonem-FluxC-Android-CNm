package com.plutonem.android.fluxc.persistence;

import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;

import com.plutonem.android.fluxc.model.AccountModel;
import com.plutonem.android.fluxc.model.BuyerModel;
import com.wellsql.generated.AccountModelTable;
import com.wellsql.generated.BuyerModelTable;
import com.yarolegovich.wellsql.SelectQuery;
import com.yarolegovich.wellsql.WellSql;

import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.AppLog.T;

import java.util.Iterator;
import java.util.List;

public class BuyerSqlUtils {
    public static class DuplicateBuyerException extends Exception {
    }

    public static SelectQuery<BuyerModel> getBuyersWith(String field, Object value) {
        return WellSql.select(BuyerModel.class)
                .where().equals(field, value).endWhere();
    }

    /**
     * Inserts the given BuyerModel into the DB, or updates an existing entry where buyers match.
     *
     * Possible cases:
     * 1. Exists in the DB already and matches by local id (simple update) -> UPDATE
     * 2. Exists in the DB, is a Plutonem buyer and matches by remote id (BUYER_ID) -> UPDATE
     * 3. Not matching any previous cases -> INSERT
     */
    public static int insertOrUpdateBuyer(BuyerModel buyer) throws DuplicateBuyerException {
        if (buyer == null) {
            return 0;
        }

        // If we're inserting or updating a PN REST API buyer, validate that we actually have a Plutonem
        // AccountModel present
        // This prevents a late UPDATE_BUYERS action from re-populating the database after sign out from Plutonem
        if (buyer.isUsingPnRestApi()) {
            List<AccountModel> accountModel = WellSql.select(AccountModel.class)
                    .where()
                    .not().equals(AccountModelTable.USER_ID, 0)
                    .endWhere()
                    .getAsModel();
            if (accountModel.isEmpty()) {
                AppLog.w(T.DB, "Can't insert PN buyer " + buyer.getBuyerId() + ", missing user account");
                return 0;
            }
        }

        // If the buyer already exist and has an id, we want to update it.
        List<BuyerModel> buyerResult = WellSql.select(BuyerModel.class)
                .where().beginGroup()
                .equals(BuyerModelTable.ID, buyer.getId())
                .endGroup().endWhere().getAsModel();
        if (!buyerResult.isEmpty()) {
            AppLog.d(T.DB, "Buyer found by (local) ID: " + buyer.getId());
        }

        // Looks like a new buyer, make sure we don't already have it.
        if (buyerResult.isEmpty()) {
            if (buyer.getBuyerId() > 0) {
                // For Plutonem buyers, the PN ID is a unique enough identifier
                buyerResult = WellSql.select(BuyerModel.class)
                        .where().beginGroup()
                        .equals(BuyerModelTable.BUYER_ID, buyer.getBuyerId())
                        .endGroup().endWhere().getAsModel();
                if (!buyerResult.isEmpty()) {
                    AppLog.d(T.DB, "Buyer found by BUYER_ID: " + buyer.getBuyerId());
                }
            }
        }

        if (buyerResult.isEmpty()) {
            // No buyer with this local ID or REMOTE_ID, then insert it
            AppLog.d(T.DB, "Inserting buyer: " + buyer.getBuyerId());
            WellSql.insert(buyer).asSingleTransaction(true).execute();
            return 1;
        }else {
            // Update old buyer
            AppLog.d(T.DB, "Updating buyer: " + buyer.getBuyerId());
            int oldId = buyerResult.get(0).getId();
            try {
                return WellSql.update(BuyerModel.class).whereId(oldId)
                        .put(buyer, new UpdateAllExceptId<>(BuyerModel.class)).execute();
            } catch (SQLiteConstraintException e) {
                AppLog.e(T.DB, "Error while updating buyer: buyerId=" + buyer.getBuyerId(), e);
                throw new DuplicateBuyerException();
            }
        }
    }

    public static int deleteBuyer(BuyerModel buyer) {
        if (buyer == null) {
            return 0;
        }
        return WellSql.delete(BuyerModel.class)
                .where().equals(BuyerModelTable.ID, buyer.getId()).endWhere()
                .execute();
    }

    public static SelectQuery<BuyerModel> getBuyersAccessedViaPNRest() {
        return WellSql.select(BuyerModel.class)
                .where().beginGroup()
                .equals(BuyerModelTable.ORIGIN, BuyerModel.ORIGIN_PN_REST)
                .endGroup().endWhere();
    }

    /**
     * Removes all buyers from local database with the following criteria:
     * 1. Buyer is a PN connected buyer
     * 2. Remote buyer ID does not match a buyer ID found in given buyers list
     *
     * @param buyers
     *  list of buyers to keep in local database
     */
    public static int removePNRestBuyersAbsentFromList(@NonNull List<BuyerModel> buyers) {
        // get all local PN buyers
        List<BuyerModel> localBuyers = WellSql.select(BuyerModel.class)
                .where()
                .equals(BuyerModelTable.ORIGIN, BuyerModel.ORIGIN_PN_REST)
                .endWhere().getAsModel();

        if (localBuyers.size() > 0) {
            // iterate through all local PN buyers
            Iterator<BuyerModel> localIterator = localBuyers.iterator();
            while (localIterator.hasNext()) {
                BuyerModel localBuyer = localIterator.next();

                // don't remove local buyer if the remote ID matches a given buyer's ID
                for (BuyerModel buyer : buyers) {
                    if (buyer.getBuyerId() == localBuyer.getBuyerId()) {
                        localIterator.remove();
                        break;
                    }
                }
            }

            // delete applicable buyers
            for (BuyerModel buyer : localBuyers) {
                deleteBuyer(buyer);
            }
        }

        return localBuyers.size();
    }
}
