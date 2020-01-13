package com.plutonem.android.fluxc.store;

import android.database.Cursor;

import com.plutonem.android.fluxc.Dispatcher;
import com.plutonem.android.fluxc.action.BuyerAction;
import com.plutonem.android.fluxc.annotations.action.Action;
import com.plutonem.android.fluxc.annotations.action.IAction;
import com.plutonem.android.fluxc.model.BuyerModel;
import com.plutonem.android.fluxc.model.BuyersModel;
import com.plutonem.android.fluxc.network.rest.plutonem.buyer.BuyerRestClient;
import com.plutonem.android.fluxc.persistence.BuyerSqlUtils;
import com.plutonem.android.fluxc.persistence.BuyerSqlUtils.DuplicateBuyerException;
import com.plutonem.android.fluxc.utils.BuyerErrorUtils;
import com.wellsql.generated.BuyerModelTable;
import com.yarolegovich.wellsql.WellSql;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.AppLog.T;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * SQLite based only. There is no in memory copy of mapped data, everything is queried from the DB.
 */
@Singleton
public class BuyerStore extends Store {
    private BuyerRestClient mBuyerRestClient;

    public static class BuyerError implements OnChangedError {
        public BuyerErrorType type;
        public String message;

        public BuyerError(BuyerErrorType type) {
            this(type, "");
        }

        public BuyerError(BuyerErrorType type, String message) {
            this.type = type;
            this.message = message;
        }
    }

    // OnChanged Events
    public static class OnBuyerChanged extends OnChanged<BuyerError> {
        public int rowsAffected;

        public OnBuyerChanged(int rowsAffected) {
            this.rowsAffected = rowsAffected;
        }
    }

    public static class UpdateBuyersResult {
        public int rowsAffected = 0;
        public boolean duplicateBuyerFound = false;
    }

    public enum BuyerErrorType {
        INVALID_BUYER,
        UNKNOWN_BUYER,
        DUPLICATE_BUYER,
        INVALID_RESPONSE,
        UNAUTHORIZED,
        GENERIC_ERROR
    }

    @Inject
    public BuyerStore(Dispatcher dispatcher, BuyerRestClient buyerRestClient) {
        super(dispatcher);
        mBuyerRestClient = buyerRestClient;
    }

    @Override
    public void onRegister() {
        AppLog.d(T.API, "BuyerStore onRegister");
    }

    /**
     * Returns all sites in the store as a {@link BuyerModel} list.
     */
    public List<BuyerModel> getBuyers() {
        return WellSql.select(BuyerModel.class).getAsModel();
    }

    /**
     * Returns all buyers in the store as a {@link Cursor}.
     */
    public Cursor getBuyersCursor() {
        return WellSql.select(BuyerModel.class).getAsCursor();
    }

    /**
     * Returns the number of buyers of any kind in the store.
     */
    public int getBuyersCount() {
        return getBuyersCursor().getCount();
    }

    /**
     * Checks whether the store contains any buyers of any kind.
     */
    public boolean hasBuyer() {
        return getBuyersCount() != 0;
    }

    /**
     * Obtains the buyer with the given (local) id and returns it as a {@link BuyerModel}.
     */
    public BuyerModel getBuyerByLocalId(int id) {
        List<BuyerModel> result = BuyerSqlUtils.getBuyersWith(BuyerModelTable.ID, id).getAsModel();
        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    /**
     * Returns all visible buyers as {@link BuyerModel}s.
     */
    public List<BuyerModel> getVisibleBuyers() {
        return BuyerSqlUtils.getBuyersWith(BuyerModelTable.IS_VISIBLE, 1).getAsModel();
    }

    /**
     * Given a PN buyer ID, returns the buyer as a
     * {@link BuyerModel}.
     */
    public BuyerModel getBuyerByBuyerId(long buyerId) {
        if (buyerId == 0) {
            return null;
        }

        List<BuyerModel> buyers = BuyerSqlUtils.getBuyersWith(BuyerModelTable.BUYER_ID, buyerId).getAsModel();

        if (buyers.isEmpty()) {
            return null;
        } else {
            return buyers.get(0);
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    @Override
    public void onAction(Action action) {
        IAction actionType = action.getType();
        if (!(actionType instanceof BuyerAction)) {
            return;
        }

        switch ((BuyerAction) actionType) {
            case FETCH_BUYER:
                fetchBuyer((BuyerModel) action.getPayload());
                break;
            case FETCH_BUYERS:
                mBuyerRestClient.fetchBuyers();
                break;
            case FETCHED_BUYERS:
                handleFetchedBuyersPNRest((BuyersModel) action.getPayload());
                break;
            case UPDATE_BUYER:
                updateBuyer((BuyerModel) action.getPayload());
                break;
        }
    }

    private void fetchBuyer(BuyerModel buyer) {
        if (buyer.isUsingPnRestApi()) {
            mBuyerRestClient.fetchBuyer(buyer);
        }
    }

    private void updateBuyer(BuyerModel buyerModel) {
        OnBuyerChanged event = new OnBuyerChanged(0);
        if (buyerModel.isError()) {
            // TODO: what kind of error could we get here?
            event.error = BuyerErrorUtils.genericToBuyerError(buyerModel.error);
        } else {
            try {
                event.rowsAffected = BuyerSqlUtils.insertOrUpdateBuyer(buyerModel);
            } catch (DuplicateBuyerException e) {
                event.error = new BuyerError(BuyerErrorType.DUPLICATE_BUYER);
            }
        }
        emitChange(event);
    }

    private void handleFetchedBuyersPNRest(BuyersModel fetchedBuyers) {
        OnBuyerChanged event = new OnBuyerChanged(0);
        if (fetchedBuyers.isError()) {
            // TODO: what kind of error could we get here?
            event.error = BuyerErrorUtils.genericToBuyerError(fetchedBuyers.error);
        } else {
            UpdateBuyersResult res = createOrUpdateBuyers(fetchedBuyers);
            event.rowsAffected = res.rowsAffected;
            if (res.duplicateBuyerFound) {
                event.error = new BuyerError(BuyerErrorType.DUPLICATE_BUYER);
            }
            BuyerSqlUtils.removePNRestBuyersAbsentFromList(fetchedBuyers.getBuyers());
        }
        emitChange(event);
    }

    private UpdateBuyersResult createOrUpdateBuyers(BuyersModel buyers) {
        UpdateBuyersResult result = new UpdateBuyersResult();
        for (BuyerModel buyer : buyers.getBuyers()) {
            try {
                result.rowsAffected += BuyerSqlUtils.insertOrUpdateBuyer(buyer);
            } catch (DuplicateBuyerException caughtException) {
                result.duplicateBuyerFound = true;
            }
        }
        return result;
    }
}
