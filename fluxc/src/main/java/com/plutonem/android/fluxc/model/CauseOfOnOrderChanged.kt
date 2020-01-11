package com.plutonem.android.fluxc.model

sealed class CauseOfOnOrderChanged {
    class RemoveOrder(val localOrderId: Int, val remoteOrderId: Long) : CauseOfOnOrderChanged()
    class UpdateOrder(val localOrderId: Int, val remoteOrderId: Long) : CauseOfOnOrderChanged()
}