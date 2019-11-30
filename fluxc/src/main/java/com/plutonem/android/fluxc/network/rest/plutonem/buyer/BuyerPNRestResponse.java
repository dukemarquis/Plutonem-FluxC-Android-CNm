package com.plutonem.android.fluxc.network.rest.plutonem.buyer;

import com.plutonem.android.fluxc.network.Response;

import java.util.List;

public class BuyerPNRestResponse implements Response {
    public class BuyersResponse {
        public List<BuyerPNRestResponse> buyers;
    }

    public class Icon {
        public String img;
    }

    public long ID;
    public String name;
    public String description;
    public boolean visible;
    public Icon icon;
}
