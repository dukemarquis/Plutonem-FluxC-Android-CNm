package com.plutonem.android.fluxc.action;

import com.plutonem.android.fluxc.annotations.Action;
import com.plutonem.android.fluxc.annotations.action.IAction;
import com.plutonem.android.fluxc.model.BuyersModel;

public enum BuyerAction implements IAction {
    // Remote actions
    @Action
    FETCH_BUYERS,

    // Remote responses
    @Action(payloadType = BuyersModel.class)
    FETCHED_BUYERS,
}
