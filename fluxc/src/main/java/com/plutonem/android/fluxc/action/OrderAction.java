package com.plutonem.android.fluxc.action;

import com.plutonem.android.fluxc.annotations.Action;
import com.plutonem.android.fluxc.annotations.ActionEnum;
import com.plutonem.android.fluxc.annotations.action.IAction;
import com.plutonem.android.fluxc.store.OrderStore.FetchOrderListPayload;

@ActionEnum
public enum OrderAction implements IAction {
    // Remote actions
    @Action(payloadType = FetchOrderListPayload.class)
    FETCH_ORDER_LIST
}
