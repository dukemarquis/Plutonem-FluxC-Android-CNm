package com.plutonem.android.fluxc.model.order;

import com.plutonem.android.fluxc.model.OrderImmutableModel;

import org.wordpress.android.util.DateTimeUtils;

import java.util.Date;
import java.util.List;

public enum OrderStatus {
    UNKNOWN,
    PAYING,
    DELIVERING,
    RECEIVING,
    FINISHED;

    public String toString() {
        switch (this) {
            case PAYING:
                return "paying";
            case DELIVERING:
                return "delivering";
            case RECEIVING:
                return "receiving";
            case FINISHED:
                return "finished";
            default:
                return "";
        }
    }

    private static synchronized OrderStatus fromStringAndDateGMT(String value, long dateCreatedGMT) {
        if (value == null) {
            return UNKNOWN;
        } else if (value.equals("paying")) {
            return PAYING;
        } else if (value.equals("delivering")) {
            return DELIVERING;
        } else if (value.equals("receiving")) {
            return RECEIVING;
        } else if (value.equals("finished")) {
            return FINISHED;
        } else {
            return UNKNOWN;
        }
    }

    public static synchronized OrderStatus fromOrder(OrderImmutableModel order) {
        String value = order.getStatus();
        long dateCreatedGMT = 0;

        Date dateCreated = DateTimeUtils.dateUTCFromIso8601(order.getDateCreated());
        if (dateCreated != null) {
            dateCreatedGMT = dateCreated.getTime();
        }

        return fromStringAndDateGMT(value, dateCreatedGMT);
    }

    public static String orderStatusListToString(List<OrderStatus> statusList) {
        String statusString = "";
        boolean firstTime = true;

        for (OrderStatus orderStatus : statusList) {
            if (firstTime) {
                firstTime = false;
            } else {
                statusString += ",";
            }
            statusString += orderStatus.toString();
        }

        return statusString;
    }
}
