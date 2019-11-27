package com.plutonem.android.fluxc.model;

import androidx.annotation.NonNull;

import com.plutonem.android.fluxc.Payload;
import com.plutonem.android.fluxc.network.BaseRequest.BaseNetworkError;

import java.util.ArrayList;
import java.util.List;

public class BuyersModel extends Payload<BaseNetworkError> {
    private List<BuyerModel> mBuyers;

    public BuyersModel() {
        mBuyers = new ArrayList<>();
    }

    public BuyersModel(@NonNull List<BuyerModel> sites) {
        mBuyers = sites;
    }

    public List<BuyerModel> getBuyers() {
        return mBuyers;
    }

    public void setBuyers(List<BuyerModel> buyers) {
        this.mBuyers = buyers;
    }
}
