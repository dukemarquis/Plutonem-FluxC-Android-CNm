package com.plutonem.android.fluxc.model;

import androidx.annotation.IntDef;

import com.plutonem.android.fluxc.Payload;
import com.plutonem.android.fluxc.network.BaseRequest.BaseNetworkError;
import com.yarolegovich.wellsql.core.Identifiable;
import com.yarolegovich.wellsql.core.annotation.Column;
import com.yarolegovich.wellsql.core.annotation.PrimaryKey;
import com.yarolegovich.wellsql.core.annotation.RawConstraints;
import com.yarolegovich.wellsql.core.annotation.Table;

import java.io.Serializable;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

@Table
@RawConstraints({"UNIQUE (BUYER_ID)"})
public class BuyerModel extends Payload<BaseNetworkError> implements Identifiable, Serializable {
    @Retention(SOURCE)
    @IntDef({ORIGIN_UNKNOWN, ORIGIN_PN_REST, ORIGIN_XMLRPC})
    public @interface BuyerOrigin {}
    public static final int ORIGIN_UNKNOWN = 0;
    public static final int ORIGIN_PN_REST = 1;
    public static final int ORIGIN_XMLRPC = 2;

    @PrimaryKey
    @Column private int mId;
    // Only given a value for pn buyers
    @Column private long mBuyerId;
    @Column private boolean mIsPN;
    @Column private int mOrigin = ORIGIN_UNKNOWN; // Does this site come from a PN REST or XMLRPC fetch_buyers call?

    @Override
    public int getId() {
        return mId;
    }

    @Override
    public void setId(int id) {
        mId = id;
    }

    public BuyerModel() {
    }

    public long getBuyerId() {
        return mBuyerId;
    }

    public void setBuyerId(long siteId) {
        mBuyerId = siteId;
    }

    public boolean isPN() {
        return mIsPN;
    }

    public void setIsPN(boolean pn) {
        mIsPN = pn;
    }

    @BuyerOrigin
    public int getOrigin() {
        return mOrigin;
    }

    public void setOrigin(@BuyerOrigin int origin) {
        mOrigin = origin;
    }

    public boolean isUsingPnRestApi() {
        return isPN() || getOrigin() == ORIGIN_PN_REST;
    }
}
