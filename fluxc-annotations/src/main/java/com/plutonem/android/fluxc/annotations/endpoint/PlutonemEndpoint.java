package com.plutonem.android.fluxc.annotations.endpoint;

public class PlutonemEndpoint {
    private static final String PLUTONEM_REST_PREFIX = " http://121.199.68.232:5004";

    private final String mEndpoint;

    public PlutonemEndpoint(String endpoint) {
        mEndpoint = endpoint;
    }

    public PlutonemEndpoint(String endpoint, long id) {
        this(endpoint + id + "/");
    }

    public PlutonemEndpoint(String endpoint, String value) {
        this(endpoint + value + "/");
    }

    public String getEndpoint() {
        return mEndpoint;
    }

    public String getUrlV0() {
        return PLUTONEM_REST_PREFIX + mEndpoint;
    }
}
