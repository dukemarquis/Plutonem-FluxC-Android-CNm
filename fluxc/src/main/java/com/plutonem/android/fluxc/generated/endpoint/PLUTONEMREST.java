package com.plutonem.android.fluxc.generated.endpoint;

import com.plutonem.android.fluxc.annotations.Endpoint;
import com.plutonem.android.fluxc.annotations.endpoint.PlutonemEndpoint;

public class PLUTONEMREST {
    @Endpoint("/is-available/")
    public static PLUTONEMREST.Is_availableEndpoint is_available = new PLUTONEMREST.Is_availableEndpoint("/");

    public PLUTONEMREST() {
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
