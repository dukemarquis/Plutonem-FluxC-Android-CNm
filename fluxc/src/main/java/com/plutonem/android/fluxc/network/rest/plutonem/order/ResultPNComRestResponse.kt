package com.plutonem.android.fluxc.network.rest.plutonem.order

import com.google.gson.annotations.SerializedName

data class ResultPNComRestResponse (
    @SerializedName("code") val code: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("message") val message: String? = null
)