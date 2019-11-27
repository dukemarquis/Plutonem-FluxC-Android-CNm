package com.plutonem.android.fluxc.network.rest.plutonem.buyer;

import com.plutonem.android.fluxc.network.Response;

import java.util.List;

public class BuyerPNRestResponse implements Response {
    public class BuyersResponse {
        public List<BuyerPNRestResponse> buyers;
    }

    public long ID;
}
