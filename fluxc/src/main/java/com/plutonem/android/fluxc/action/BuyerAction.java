package com.plutonem.android.fluxc.action;

import com.plutonem.android.fluxc.annotations.Action;
import com.plutonem.android.fluxc.annotations.ActionEnum;
import com.plutonem.android.fluxc.annotations.action.IAction;
import com.plutonem.android.fluxc.model.BuyerModel;
import com.plutonem.android.fluxc.model.BuyersModel;

@ActionEnum
public enum BuyerAction implements IAction {
    // Remote actions
    @Action(payloadType = BuyerModel.class)
    FETCH_BUYER,
    @Action
    FETCH_BUYERS,

    // Remote responses
    @Action(payloadType = BuyersModel.class)
    FETCHED_BUYERS,

    // Local actions
    @Action(payloadType = BuyerModel.class)
    UPDATE_BUYER,
    @Action
    REMOVE_PN_BUYERS
}
