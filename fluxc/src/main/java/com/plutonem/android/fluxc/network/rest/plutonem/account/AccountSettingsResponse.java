package com.plutonem.android.fluxc.network.rest.plutonem.account;

import com.plutonem.android.fluxc.network.Response;

/**
 * Stores data retrieved from the Plutonem REST API Account Settings endpoint (/me/settings).
 * Field names correspond to REST response keys.
 *
 * See <a href="https://developer.wordpress.com/docs/api/1.1/get/me/settings">documentation</a>
 */
public class AccountSettingsResponse implements Response {
    public long primary_buyer_ID;
}
