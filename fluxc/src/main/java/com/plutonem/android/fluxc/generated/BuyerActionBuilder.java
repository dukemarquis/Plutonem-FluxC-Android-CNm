package com.plutonem.android.fluxc.generated;

import com.plutonem.android.fluxc.action.BuyerAction;
import com.plutonem.android.fluxc.annotations.action.Action;
import com.plutonem.android.fluxc.annotations.action.ActionBuilder;
import com.plutonem.android.fluxc.model.BuyersModel;

public final class BuyerActionBuilder extends ActionBuilder {
    public BuyerActionBuilder() {
    }

    public static Action<Void> newFetchBuyersAction() {
        return generateNoPayloadAction(BuyerAction.FETCH_BUYERS);
    }

    public static Action<BuyersModel> newFetchedBuyersAction(BuyersModel payload) {
        return new Action(BuyerAction.FETCHED_BUYERS, payload);
    }
}
