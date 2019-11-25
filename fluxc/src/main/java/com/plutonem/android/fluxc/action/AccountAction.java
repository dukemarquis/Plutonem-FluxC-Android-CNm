package com.plutonem.android.fluxc.action;

import com.plutonem.android.fluxc.annotations.Action;
import com.plutonem.android.fluxc.annotations.ActionEnum;
import com.plutonem.android.fluxc.annotations.action.IAction;
import com.plutonem.android.fluxc.network.rest.plutonem.account.AccountRestClient.AccountRestPayload;
import com.plutonem.android.fluxc.network.rest.plutonem.account.AccountRestClient.IsAvailableResponsePayload;

@ActionEnum
public enum AccountAction implements IAction {
    // Remote actions
    @Action
    FETCH_ACCOUNT,          // request fetch of Account information
    @Action(payloadType = String.class)
    IS_AVAILABLE_PHONE,
    @Action(payloadType = IsAvailableResponsePayload.class)
    CHECKED_IS_AVAILABLE,

    // Remote responses
    @Action(payloadType = AccountRestPayload.class)
    FETCHED_ACCOUNT        // response received from Account fetch request
}
