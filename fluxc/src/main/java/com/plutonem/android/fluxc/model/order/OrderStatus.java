package com.plutonem.android.fluxc.model.order;

import java.util.List;

public enum OrderStatus {
    DELIVERING,
    RECEIVING,
    FINISHED;

    public String toString() {
        switch (this) {
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
