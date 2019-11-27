package com.plutonem.android.fluxc.store;

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

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.AppLog.T;

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

    @Subscribe(threadMode = ThreadMode.ASYNC)
    @Override
    public void onAction(Action action) {
        IAction actionType = action.getType();
        if (!(actionType instanceof BuyerAction)) {
            return;
        }

        switch ((BuyerAction) actionType) {
            case FETCH_BUYERS:
                mBuyerRestClient.fetchBuyers();
                break;
            case FETCHED_BUYERS:
                handleFetchedBuyersPNRest((BuyersModel) action.getPayload());
                break;
        }
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
