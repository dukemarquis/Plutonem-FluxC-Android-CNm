package com.plutonem.android.fluxc.network.rest.plutonem.account;

import com.plutonem.android.fluxc.network.Response;

/**
 * Stores data retrieved from the Plutonem REST API Account endpoint (/me). Field names
 * correspond to REST response keys.
 *
 * See <a href="https://developer.wordpress.com/docs/api/1.1/get/me/">documentation</a>
 */
public class AccountResponse implements Response {
    public long ID;
    public String display_name;
    public String username;
    public String phone;
    public String date;
    public boolean has_unseen_notes;
}
