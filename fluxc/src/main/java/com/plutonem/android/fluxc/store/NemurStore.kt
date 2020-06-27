package com.plutonem.android.fluxc.store

import com.plutonem.android.fluxc.tools.CoroutineEngine
import org.wordpress.android.util.AppLog
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NemurStore
@Inject constructor(
    private val coroutineEngine: CoroutineEngine
) {
    suspend fun getItemTypes(): List<ItemType> =
        coroutineEngine.withDefaultContext(AppLog.T.STATS, this, "getItemTypes") {
            return@withDefaultContext ItemType.values().toList()
        }

    interface NemurType

    enum class ItemType : NemurType {
        FEATURED_MEDIAS,
        BASE_INFOS,
        GUARANTEES,
        PARAMETERS
    }
}