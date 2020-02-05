package com.plutonem.android.fluxc.store;

import com.plutonem.android.fluxc.Dispatcher;
import com.plutonem.android.fluxc.action.SubmitAction;
import com.plutonem.android.fluxc.annotations.action.Action;
import com.plutonem.android.fluxc.annotations.action.IAction;
import com.plutonem.android.fluxc.generated.OrderActionBuilder;
import com.plutonem.android.fluxc.store.OrderStore.RemoteDecryptionPayload;
import com.plutonem.android.fluxc.store.OrderStore.RemoteInfoPayload;
import com.plutonem.android.fluxc.store.OrderStore.RemoteOrderPayload;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.AppLog.T;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UploadStore extends Store {
    @Inject
    public UploadStore(Dispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void onRegister() {
        AppLog.d(T.API, "SubmitStore onRegister");
    }

    // Ensure that events reach the UploadStore before their main stores (OrderStore)
    @Subscribe(threadMode = ThreadMode.ASYNC, priority = 1)
    @Override
    public void onAction(Action action) {
        IAction actionType = action.getType();
        if (actionType instanceof SubmitAction) {
            onSubmitAction((SubmitAction) actionType, action.getPayload());
        }
    }

    private void onSubmitAction(SubmitAction actionType, Object payload) {
        switch (actionType) {
            case PUSHED_ORDER:
//                handleOrderUploaded((RemoteOrderPayload) payload);
                mDispatcher.dispatch(OrderActionBuilder.newPushedOrderAction((RemoteOrderPayload) payload));
                break;
            case SIGNED_INFO:
                mDispatcher.dispatch(OrderActionBuilder.newSignedInfoAction((RemoteInfoPayload) payload));
                break;
            case DECRYPTED_RESULT:
                mDispatcher.dispatch(OrderActionBuilder.newDecryptedResultAction((RemoteDecryptionPayload) payload));
                break;
        }
    }
}
