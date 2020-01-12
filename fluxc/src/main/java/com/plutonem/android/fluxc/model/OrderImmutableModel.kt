package com.plutonem.android.fluxc.model

interface OrderImmutableModel {
    val id: Int
    val localBuyerId: Int
    val remoteBuyerId: Long
    val remoteOrderId: Long
    val shopTitle: String
    val productDetail: String
    val orderDetail: String
    val itemSalesPrice: String
    val orderNumber: Long
    val itemDistributionMode: String
    val orderPrice: String
    val orderName: String
    val orderPhoneNumber: String
    val orderAddress: String
    val dateCreated: String
    val lastModified: String
    val remoteLastModified: String
    val status: String
    val orderFormat: String
    val accountId: Long
    val accountDisplayName: String?
    val changesConfirmedContentHashcode: Int
    val isLocalDraft: Boolean
    val isLocallyChanged: Boolean
    val dateLocallyChanged: String

    fun contentHashcode(): Int
}