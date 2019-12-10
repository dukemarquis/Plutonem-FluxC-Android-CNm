package com.plutonem.android.fluxc.model

sealed class CauseOfOnOrderChanged {
    class UpdateOrder(val localOrderId: Int, val remoteOrderId: Long) : CauseOfOnOrderChanged()
}