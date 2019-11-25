package com.plutonem.android.fluxc.model;

import com.plutonem.android.fluxc.Payload;
import com.plutonem.android.fluxc.network.BaseRequest.BaseNetworkError;
import com.yarolegovich.wellsql.core.Identifiable;
import com.yarolegovich.wellsql.core.annotation.Column;
import com.yarolegovich.wellsql.core.annotation.PrimaryKey;
import com.yarolegovich.wellsql.core.annotation.Table;

import org.wordpress.android.util.StringUtils;

@Table
public class AccountModel extends Payload<BaseNetworkError> implements Identifiable {
    @PrimaryKey(autoincrement = false)
    @Column private int mId;

    // Account attributes
    @Column private String mUserName;
    @Column private long mUserId;
    @Column private String mDisplayName;
    @Column private String mPhone;
    @Column private boolean mHasUnseenNotes;

    // Account Settings attributes
    @Column private String mDate;

    public AccountModel() {
        init();
    }

    @Override
    public int getId() {
        return mId;
    }

    @Override
    public void setId(int id) {
        mId = id;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || !(other instanceof AccountModel)) return false;

        AccountModel otherAccount = (AccountModel) other;

        return getId() == otherAccount.getId()
                && StringUtils.equals(getUserName(), otherAccount.getUserName())
                && getUserId() == otherAccount.getUserId()
                && StringUtils.equals(getDisplayName(), otherAccount.getDisplayName())
                && StringUtils.equals(getDate(), otherAccount.getDate())
                && getHasUnseenNotes() == otherAccount.getHasUnseenNotes();
    }

    public void init() {
        mUserName = "";
        mUserId = 0;
        mDisplayName = "";
        mPhone = "";
        mDate = "";
    }

    /**
     * Copies Account attributes from another {@link AccountModel} to this instance.
     */
    public void copyAccountAttributes(AccountModel other) {
        if (other == null) return;
        setUserName(other.getUserName());
        setUserId(other.getUserId());
        setDisplayName(other.getDisplayName());
        setPhone(other.getPhone());
        setHasUnseenNotes(other.getHasUnseenNotes());
    }

    /**
     * Copies Account Settings attributes from another {@link AccountModel} to this instance.
     */
    public void copyAccountSettingsAttributes(AccountModel other) {
        if (other == null) return;
        setUserName(other.getUserName());
        setDate(other.getDate());
        setDisplayName(other.getDisplayName());
    }

    public long getUserId() {
        return mUserId;
    }

    public void setUserId(long userId) {
        mUserId = userId;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String displayName) {
        mDisplayName = displayName;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getDate() {
        return mDate;
    }

    public boolean getHasUnseenNotes() {
        return mHasUnseenNotes;
    }

    public void setHasUnseenNotes(boolean hasUnseenNotes) {
        mHasUnseenNotes = hasUnseenNotes;
    }
}
