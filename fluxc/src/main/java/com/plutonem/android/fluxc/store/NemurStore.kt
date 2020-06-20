package com.plutonem.android.fluxc.store

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NemurStore
@Inject constructor(

) {
    interface NemurType

    enum class ItemType : NemurType {
        PARAMETER
    }
}