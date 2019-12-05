package com.plutonem.android.fluxc.action

import com.plutonem.android.fluxc.annotations.Action
import com.plutonem.android.fluxc.annotations.ActionEnum
import com.plutonem.android.fluxc.annotations.action.IAction
import com.plutonem.android.fluxc.model.list.ListDescriptorTypeIdentifier

@ActionEnum
enum class ListAction : IAction {
    @Action(payloadType = ListDescriptorTypeIdentifier::class)
    LIST_DATA_INVALIDATED,
}