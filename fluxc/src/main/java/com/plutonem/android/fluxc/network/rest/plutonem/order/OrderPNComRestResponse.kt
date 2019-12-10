package com.plutonem.android.fluxc.network.rest.plutonem.order

import com.google.gson.annotations.SerializedName

data class OrderPNComRestResponse(
    @SerializedName("ID") val remoteOrderId: Long = 0,
    @SerializedName("buyer_ID") val remoteBuyerId: Long = 0,
    @SerializedName("date") val date: String? = null,
    @SerializedName("modified") val modified: String? = null,
    @SerializedName("shopTitle") val shopTitle: String? = null,
    @SerializedName("productName") val productName: String? = null,
    @SerializedName("orderDetail") val orderDetail: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("account") val account: Account? = null
) {
    data class OrdersResponse(
        @SerializedName("orders") val orders: List<OrderPNComRestResponse>
    )

    data class Account(
        @SerializedName("ID") val id: Long = 0,
        @SerializedName("name") val name: String?
    )
}