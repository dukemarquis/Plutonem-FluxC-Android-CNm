package com.plutonem.android.fluxc.generated;

import com.plutonem.android.fluxc.action.SubmitAction;
import com.plutonem.android.fluxc.annotations.action.Action;
import com.plutonem.android.fluxc.annotations.action.ActionBuilder;
import com.plutonem.android.fluxc.store.OrderStore.RemoteOrderPayload;
import com.plutonem.android.fluxc.store.OrderStore.RemoteInfoPayload;

public class SubmitActionBuilder extends ActionBuilder {
    public SubmitActionBuilder() {
    }

    public static Action<RemoteOrderPayload> newPushedOrderAction(RemoteOrderPayload payload) {
        return new Action(SubmitAction.PUSHED_ORDER, payload);
    }

    public static Action<RemoteInfoPayload> newSignedInfoAction(RemoteInfoPayload payload) {
        return new Action(SubmitAction.SIGNED_INFO, payload);
    }
}
