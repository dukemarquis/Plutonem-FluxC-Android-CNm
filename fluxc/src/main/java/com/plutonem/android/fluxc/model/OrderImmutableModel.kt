package com.plutonem.android.fluxc.model

interface OrderImmutableModel {
    val id: Int
    val localBuyerId: Int
    val remoteBuyerId: Long
    val remoteOrderId: Long
    val shopTitle: String
    val productDetail: String
    val orderDetail: String
    val dateCreated: String
    val lastModified: String
    val remoteLastModified: String
    val status: String
    val orderFormat: String
    val accountId: Long
    val accountDisplayName: String?
    val isLocalDraft: Boolean
    val isLocallyChanged: Boolean
    val dateLocallyChanged: String
}