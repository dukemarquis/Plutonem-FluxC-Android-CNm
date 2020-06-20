package com.plutonem.android.fluxc.tools

import com.plutonem.android.fluxc.utils.AppLogWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import org.wordpress.android.util.AppLog
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

// The class is open for testing
open class CoroutineEngine
@Inject constructor(private val coroutineContext: CoroutineContext, private val appLog: AppLogWrapper) {
    suspend fun <RESULT_TYPE> withDefaultContext(
        tag: AppLog.T,
        caller: Any,
        loggedMessage: String,
        block: suspend CoroutineScope.() -> RESULT_TYPE
    ): RESULT_TYPE {
        appLog.d(tag, "${caller.javaClass.simpleName}: $loggedMessage")
        return withContext(coroutineContext, block)
    }
}