package com.plutonem.android.fluxc.utils;

import com.plutonem.android.fluxc.network.BaseRequest.BaseNetworkError;
import com.plutonem.android.fluxc.store.BuyerStore.BuyerErrorType;
import com.plutonem.android.fluxc.store.BuyerStore.BuyerError;

public class BuyerErrorUtils {
    public static BuyerError genericToBuyerError(BaseNetworkError error) {
        BuyerErrorType errorType = BuyerErrorType.GENERIC_ERROR;
        if (error.isGeneric()) {
            switch (error.type) {
                case INVALID_RESPONSE:
                    errorType = BuyerErrorType.INVALID_RESPONSE;
                    break;
            }
        }
        return new BuyerError(errorType, error.message);
    }
}
