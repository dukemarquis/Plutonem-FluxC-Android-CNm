package com.plutonem.android.fluxc.model;

import androidx.annotation.NonNull;

import com.plutonem.android.fluxc.Payload;
import com.plutonem.android.fluxc.network.BaseRequest;
import com.yarolegovich.wellsql.core.Identifiable;
import com.yarolegovich.wellsql.core.annotation.Column;
import com.yarolegovich.wellsql.core.annotation.PrimaryKey;
import com.yarolegovich.wellsql.core.annotation.Table;

import org.wordpress.android.util.StringUtils;

import java.io.Serializable;

@Table
public class OrderModel extends Payload<BaseRequest.BaseNetworkError> implements Cloneable, Identifiable, Serializable {
    @PrimaryKey
    @Column private int mId;
    @Column private int mLocalBuyerId;
    @Column private long mRemoteBuyerId; // .COM REST API
    @Column private long mRemoteOrderId;
    @Column private String mShopTitle;
    @Column private String mProductDetail;
    @Column private String mOrderDetail;
    @Column private String mDateCreated; // ISO 8601-formatted date in UTC, e.g. 1955-11-05T14:15:00Z
    @Column private String mLastModified; // ISO 8601-formatted date in UTC, e.g. 1955-11-05T14:15:00Z
    @Column private String mRemoteLastModified; // ISO 8601-formatted date in UTC, e.g. 1955-11-05T14:15:00Z
    @Column private String mStatus;
    @Column private String mOrderFormat;

    @Column private long mAccountId;
    @Column private String mAccountDisplayName;

    // Local only
    @Column private boolean mIsLocallyChanged;

    public OrderModel() {}

    @Override
    public void setId(int id) {
        mId = id;
    }

    @Override
    public int getId() {
        return mId;
    }

    public int getLocalBuyerId() {
        return mLocalBuyerId;
    }

    public void setLocalBuyerId(int localTableBuyerId) {
        mLocalBuyerId = localTableBuyerId;
    }

    public long getRemoteBuyerId() {
        return mRemoteBuyerId;
    }

    public void setRemoteBuyerId(long buyerId) {
        mRemoteBuyerId = buyerId;
    }

    public long getRemoteOrderId() {
        return mRemoteOrderId;
    }

    public void setRemoteOrderId(long orderId) {
        mRemoteOrderId = orderId;
    }

    public @NonNull String getShopTitle() {
        return StringUtils.notNullStr(mShopTitle);
    }

    public void setShopTitle(String shopTitle) {
        mShopTitle = shopTitle;
    }

    public @NonNull String getProductDetail() {
        return StringUtils.notNullStr(mProductDetail);
    }

    public void setProductDetail(String productDetail) {
        mProductDetail = productDetail;
    }

    public @NonNull String getOrderDetail() {
        return StringUtils.notNullStr(mOrderDetail);
    }

    public void setOrderDetail(String orderDetail) {
        mOrderDetail = orderDetail;
    }

    public @NonNull String getDateCreated() {
        return StringUtils.notNullStr(mDateCreated);
    }

    public void setDateCreated(String dateCreated) {
        mDateCreated = dateCreated;
    }

    public @NonNull String getLastModified() {
        return StringUtils.notNullStr(mLastModified);
    }

    public void setLastModified(String lastModified) {
        mLastModified = lastModified;
    }

    public @NonNull String getRemoteLastModified() {
        return StringUtils.notNullStr(mRemoteLastModified);
    }

    public void setRemoteLastModified(String remoteLastModified) {
        mRemoteLastModified = remoteLastModified;
    }

    public @NonNull String getStatus() {
        return StringUtils.notNullStr(mStatus);
    }

    public void setStatus(String status) {
        mStatus = status;
    }

    public @NonNull String getOrderFormat() {
        return StringUtils.notNullStr(mOrderFormat);
    }

    public void setOrderFormat(String orderFormat) {
        mOrderFormat = orderFormat;
    }

    public long getAccountId() {
        return mAccountId;
    }

    public void setAccountId(long accountId) {
        this.mAccountId = accountId;
    }

    public String getAccountDisplayName() {
        return mAccountDisplayName;
    }

    public void setAccountDisplayName(String accountDisplayName) {
        mAccountDisplayName = accountDisplayName;
    }

    public boolean isLocallyChanged() {
        return mIsLocallyChanged;
    }

    public void setIsLocallyChanged(boolean isLocallyChanged) {
        mIsLocallyChanged = isLocallyChanged;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || !(other instanceof OrderModel)) return false;

        OrderModel otherOrder = (OrderModel) other;

        return getId() == otherOrder.getId() && getLocalBuyerId() == otherOrder.getLocalBuyerId()
                && getRemoteBuyerId() == otherOrder.getRemoteBuyerId() && getRemoteOrderId() == otherOrder.getRemoteOrderId()
                && getAccountId() == otherOrder.getAccountId()
                && StringUtils.equals(getShopTitle(), otherOrder.getShopTitle())
                && StringUtils.equals(getProductDetail(), otherOrder.getProductDetail())
                && StringUtils.equals(getOrderDetail(), otherOrder.getOrderDetail())
                && StringUtils.equals(getDateCreated(), otherOrder.getDateCreated())
                && StringUtils.equals(getStatus(), otherOrder.getStatus())
                && StringUtils.equals(getOrderFormat(), otherOrder.getOrderFormat())
                && StringUtils.equals(getAccountDisplayName(), otherOrder.getAccountDisplayName());
    }

    @Override
    public OrderModel clone() {
        try {
            return (OrderModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Can't happen
        }
    }
}
