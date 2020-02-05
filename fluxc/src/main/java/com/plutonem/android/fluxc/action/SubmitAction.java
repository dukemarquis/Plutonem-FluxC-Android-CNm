package com.plutonem.android.fluxc.action;

import com.plutonem.android.fluxc.annotations.Action;
import com.plutonem.android.fluxc.annotations.ActionEnum;
import com.plutonem.android.fluxc.annotations.action.IAction;
import com.plutonem.android.fluxc.store.OrderStore.RemoteDecryptionPayload;
import com.plutonem.android.fluxc.store.OrderStore.RemoteInfoPayload;
import com.plutonem.android.fluxc.store.OrderStore.RemoteOrderPayload;

@ActionEnum
public enum SubmitAction implements IAction {
    // Remote responses
    @Action(payloadType = RemoteOrderPayload.class)
    PUSHED_ORDER, // Proxy for OrderAction.PUSHED_ORDER
    @Action(payloadType = RemoteInfoPayload.class)
    SIGNED_INFO,
    @Action(payloadType = RemoteDecryptionPayload.class)
    DECRYPTED_RESULT
}
