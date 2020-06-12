package com.plutonem.android.fluxc.generated;

import com.plutonem.android.fluxc.action.BuyerAction;
import com.plutonem.android.fluxc.annotations.action.Action;
import com.plutonem.android.fluxc.annotations.action.ActionBuilder;
import com.plutonem.android.fluxc.model.BuyerModel;
import com.plutonem.android.fluxc.model.BuyersModel;

public final class BuyerActionBuilder extends ActionBuilder {
    public BuyerActionBuilder() {
    }

    public static Action<BuyerModel> newFetchBuyerAction(BuyerModel payload) {
        return new Action(BuyerAction.FETCH_BUYER, payload);
    }

    public static Action<Void> newFetchBuyersAction() {
        return generateNoPayloadAction(BuyerAction.FETCH_BUYERS);
    }

    public static Action<BuyersModel> newFetchedBuyersAction(BuyersModel payload) {
        return new Action(BuyerAction.FETCHED_BUYERS, payload);
    }

    public static Action<BuyerModel> newUpdateBuyerAction(BuyerModel payload) {
        return new Action(BuyerAction.UPDATE_BUYER, payload);
    }

    public static Action<Void> newRemovePnBuyersAction() {
        return generateNoPayloadAction(BuyerAction.REMOVE_PN_BUYERS);
    }
}