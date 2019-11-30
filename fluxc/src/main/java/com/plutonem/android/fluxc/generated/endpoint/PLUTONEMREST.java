package com.plutonem.android.fluxc.generated.endpoint;

import com.plutonem.android.fluxc.annotations.Endpoint;
import com.plutonem.android.fluxc.annotations.endpoint.PlutonemEndpoint;

public class PLUTONEMREST {
    @Endpoint("/is-available/")
    public static PLUTONEMREST.Is_availableEndpoint is_available = new PLUTONEMREST.Is_availableEndpoint("/");
    @Endpoint("/me/")
    public static PLUTONEMREST.MeEndpoint me = new PLUTONEMREST.MeEndpoint("/");
    @Endpoint("/buyers/")
    public static PLUTONEMREST.BuyersEndpoint buyers = new PLUTONEMREST.BuyersEndpoint("/");

    public PLUTONEMREST() {
    }

    public static class BuyersEndpoint extends PlutonemEndpoint {
        private static final String BUYERS_ENDPOINT = "buyers/";

        private BuyersEndpoint(String previousEndpoint) {
            super(previousEndpoint + "buyers/");
        }
    }

    public static class MeEndpoint extends PlutonemEndpoint {
        private static final String ME_ENDPOINT = "me/";
        @Endpoint("/me/settings/")
        public PlutonemEndpoint settings;
        @Endpoint("/me/buyers/")
        public PlutonemEndpoint buyers;

        private MeEndpoint(String previousEndpoint) {
            super(previousEndpoint + "me/");
            this.settings = new PlutonemEndpoint(this.getEndpoint() + "settings/");
            this.buyers = new PlutonemEndpoint(this.getEndpoint() + "buyers/");
        }
    }

    public static class Is_availableEndpoint extends PlutonemEndpoint {
        private static final String IS_AVAILABLE_ENDPOINT = "is-available/";
        @Endpoint("/is-available/phone/")
        public PlutonemEndpoint phone;

        private Is_availableEndpoint(String previousEndpoint) {
            super(previousEndpoint + "is-available/");
            this.phone = new PlutonemEndpoint(this.getEndpoint() + "phone/");
        }
    }

}
