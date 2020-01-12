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
public class OrderModel extends Payload<BaseRequest.BaseNetworkError> implements Cloneable, Identifiable, Serializable,
        OrderImmutableModel {
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

    @Column private String mItemSalesPrice;
    @Column private long mOrderNumber;
    @Column private String mItemDistributionMode;
    @Column private String mOrderPrice;

    @Column private String mOrderName;
    @Column private String mOrderPhoneNumber;
    @Column private String mOrderAddress;

    @Column private long mAccountId;
    @Column private String mAccountDisplayName;

    /**
     * This field stores a hashcode value of the order detail when the user confirmed making the changes visible to
     * the users (Confirm).
     * <p>
     * It is used to determine if the user actually confirmed the changes and if the order was edited since then.
     */
    @Column private int mChangesConfirmedContentHashcode;

    // Local only
    @Column private boolean mIsLocalDraft;
    @Column private boolean mIsLocallyChanged;
    @Column private String mDateLocallyChanged; // ISO 8601-formatted date in UTC, e.g. 1955-11-05T14:15:00Z

    public OrderModel() {}

    @Override
    public void setId(int id) {
        mId = id;
    }

    @Override
    public int getId() {
        return mId;
    }

    @Override
    public int getLocalBuyerId() {
        return mLocalBuyerId;
    }

    public void setLocalBuyerId(int localTableBuyerId) {
        mLocalBuyerId = localTableBuyerId;
    }

    @Override
    public long getRemoteBuyerId() {
        return mRemoteBuyerId;
    }

    public void setRemoteBuyerId(long buyerId) {
        mRemoteBuyerId = buyerId;
    }

    @Override
    public long getRemoteOrderId() {
        return mRemoteOrderId;
    }

    public void setRemoteOrderId(long orderId) {
        mRemoteOrderId = orderId;
    }

    @Override
    public @NonNull String getShopTitle() {
        return StringUtils.notNullStr(mShopTitle);
    }

    public void setShopTitle(String shopTitle) {
        mShopTitle = shopTitle;
    }

    @Override
    public @NonNull String getProductDetail() {
        return StringUtils.notNullStr(mProductDetail);
    }

    public void setProductDetail(String productDetail) {
        mProductDetail = productDetail;
    }

    @Override
    public @NonNull String getOrderDetail() {
        return StringUtils.notNullStr(mOrderDetail);
    }

    public void setOrderDetail(String orderDetail) {
        mOrderDetail = orderDetail;
    }

    @Override
    public @NonNull String getItemSalesPrice() {
        return StringUtils.notNullStr(mItemSalesPrice);
    }

    public void setItemSalesPrice(String itemSalesPrice) {
        mItemSalesPrice = itemSalesPrice;
    }

    @Override
    public @NonNull long getOrderNumber() {
        return mOrderNumber;
    }

    public void setOrderNumber(long orderNumber) {
        mOrderNumber = orderNumber;
    }

    @Override
    public @NonNull String getItemDistributionMode() {
        return StringUtils.notNullStr(mItemDistributionMode);
    }

    public void setItemDistributionMode(String itemDistributionMode) {
        mItemDistributionMode = itemDistributionMode;
    }

    @Override
    public @NonNull String getOrderPrice() {
        return StringUtils.notNullStr(mOrderPrice);
    }

    public void setOrderPrice(String orderPrice) {
        mOrderPrice = orderPrice;
    }

    @Override
    public @NonNull String getOrderName() {
        return StringUtils.notNullStr(mOrderName);
    }

    public void setOrderName(String orderName) {
        mOrderName = orderName;
    }

    @Override
    public @NonNull String getOrderPhoneNumber() {
        return StringUtils.notNullStr(mOrderPhoneNumber);
    }

    public void setOrderPhoneNumber(String orderPhoneNumber) {
        mOrderPhoneNumber = orderPhoneNumber;
    }

    @Override
    public @NonNull String getOrderAddress() {
        return StringUtils.notNullStr(mOrderAddress);
    }

    public void setOrderAddress(String orderAddress) {
        mOrderAddress = orderAddress;
    }

    @Override
    public @NonNull String getDateCreated() {
        return StringUtils.notNullStr(mDateCreated);
    }

    public void setDateCreated(String dateCreated) {
        mDateCreated = dateCreated;
    }

    @Override
    public @NonNull String getLastModified() {
        return StringUtils.notNullStr(mLastModified);
    }

    public void setLastModified(String lastModified) {
        mLastModified = lastModified;
    }

    @Override
    public @NonNull String getRemoteLastModified() {
        return StringUtils.notNullStr(mRemoteLastModified);
    }

    public void setRemoteLastModified(String remoteLastModified) {
        mRemoteLastModified = remoteLastModified;
    }

    @Override
    public @NonNull String getStatus() {
        return StringUtils.notNullStr(mStatus);
    }

    public void setStatus(String status) {
        mStatus = status;
    }

    @Override
    public @NonNull String getOrderFormat() {
        return StringUtils.notNullStr(mOrderFormat);
    }

    public void setOrderFormat(String orderFormat) {
        mOrderFormat = orderFormat;
    }

    @Override
    public long getAccountId() {
        return mAccountId;
    }

    public void setAccountId(long accountId) {
        this.mAccountId = accountId;
    }

    @Override
    public String getAccountDisplayName() {
        return mAccountDisplayName;
    }

    public void setAccountDisplayName(String accountDisplayName) {
        mAccountDisplayName = accountDisplayName;
    }

    @Override
    public int getChangesConfirmedContentHashcode() {
        return mChangesConfirmedContentHashcode;
    }

    public void setChangesConfirmedContentHashcode(int changesConfirmedContentHashcode) {
        mChangesConfirmedContentHashcode = changesConfirmedContentHashcode;
    }

    @Override
    public boolean isLocalDraft() {
        return mIsLocalDraft;
    }

    public void setIsLocalDraft(boolean isLocalDraft) {
        mIsLocalDraft = isLocalDraft;
    }

    @Override
    public boolean isLocallyChanged() {
        return mIsLocallyChanged;
    }

    public void setIsLocallyChanged(boolean isLocallyChanged) {
        mIsLocallyChanged = isLocallyChanged;
    }

    @Override
    public @NonNull String getDateLocallyChanged() {
        return StringUtils.notNullStr(mDateLocallyChanged);
    }

    public void setDateLocallyChanged(String dateLocallyChanged) {
        mDateLocallyChanged = dateLocallyChanged;
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

    /**
     * This method is used along with `mChangesConfirmedContentHashcode`. We store the contentHashcode of
     * the order when the user explicitly confirms that the changes to the order can be confirmed. Beware, that when
     * you modify this method all users will need to re-confirm all the local changes. The changes wouldn't get
     * confirmed otherwise.
     *
     * This is a method generated using Android Studio. When you need to add a new field it's safer to use the
     * generator again. (We can't use Objects.hash() since the current minSdkVersion is lower than 19.
     */
    @Override
    public int contentHashcode() {
        int result;
        result = mId;
        result = 31 * result + mLocalBuyerId;
        result = 31 * result + (int) (mRemoteBuyerId ^ (mRemoteBuyerId >>> 32));
        result = 31 * result + (int) (mRemoteOrderId ^ (mRemoteOrderId >>> 32));
        result = 31 * result + (mDateCreated != null ? mDateCreated.hashCode() : 0);
        result = 31 * result + (mShopTitle != null ? mShopTitle.hashCode() : 0);
        result = 31 * result + (mProductDetail != null ? mProductDetail.hashCode() : 0);
        result = 31 * result + (mItemSalesPrice != null ? mItemSalesPrice.hashCode() : 0);
        result = 31 * result + (int) (mOrderNumber ^ (mOrderNumber >>> 32));
        result = 31 * result + (mItemDistributionMode != null ? mItemDistributionMode.hashCode() : 0);
        result = 31 * result + (mOrderPrice != null ? mOrderPrice.hashCode() : 0);
        result = 31 * result + (mOrderName != null ? mOrderName.hashCode() : 0);
        result = 31 * result + (mOrderPhoneNumber != null ? mOrderPhoneNumber.hashCode() : 0);
        result = 31 * result + (mOrderAddress != null ? mOrderAddress.hashCode() : 0);
        result = 31 * result + (mStatus != null ? mStatus.hashCode() : 0);
        result = 31 * result + (int) (mAccountId ^ (mAccountId >>> 32));
        result = 31 * result + (mAccountDisplayName != null ? mAccountDisplayName.hashCode() : 0);
        result = 31 * result + (mOrderFormat != null ? mOrderFormat.hashCode() : 0);
        return result;
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
