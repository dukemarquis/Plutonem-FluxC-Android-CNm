//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.plutonem.android.fluxc.generated;

import com.plutonem.android.fluxc.action.AccountAction;
import com.plutonem.android.fluxc.annotations.action.Action;
import com.plutonem.android.fluxc.annotations.action.ActionBuilder;
import com.plutonem.android.fluxc.network.rest.plutonem.account.AccountRestClient.IsAvailableResponsePayload;

public final class AccountActionBuilder extends ActionBuilder {
    public AccountActionBuilder() {
    }

    public static Action<String> newIsAvailablePhoneAction(String payload) {
        return new Action(AccountAction.IS_AVAILABLE_PHONE, payload);
    }

    public static Action<IsAvailableResponsePayload> newCheckedIsAvailableAction(IsAvailableResponsePayload payload) {
        return new Action(AccountAction.CHECKED_IS_AVAILABLE, payload);
    }
}
