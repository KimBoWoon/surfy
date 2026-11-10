package com.cheeke.surfy.common

import android.os.SystemClock

class AppDoubleBackToExit(
    private val exitIntervalMillis: Long = 2_000L,
    private val currentTimeProvider: () -> Long = SystemClock::elapsedRealtime
) {
    private var lastBackPressedTimeMillis: Long? = null

    fun handleBackPressed(onFirstPress: () -> Unit, onExit: () -> Unit) {
        val currentTimeMillis = currentTimeProvider()
        val lastPressedTimeMillis = lastBackPressedTimeMillis

        if (lastPressedTimeMillis != null && currentTimeMillis - lastPressedTimeMillis <= exitIntervalMillis) {
            lastBackPressedTimeMillis = null
            onExit()
        } else {
            lastBackPressedTimeMillis = currentTimeMillis
            onFirstPress()
        }
    }
}

//class AppDoubleBackToExit @AssistedInject constructor(
//    @Assisted("context") private val context: Context,
//    @Assisted("exitText") private val exitText: String
//) {
//    private val delayMillis = 1500L
//    private var backPressedOnce = false
//
//    @AssistedFactory
//    interface AppDoubleBackToExitFactory {
//        fun create(
//            @Assisted("context") context: Context,
//            @Assisted("exitText") exitText: String
//        ): AppDoubleBackToExit
//    }
//
//    fun onBackPressed(callback: () -> Unit) {
//        when (backPressedOnce) {
//            true -> callback.invoke()
//            false -> {
//                Toast.makeText(context, exitText, Toast.LENGTH_SHORT).show()
//                backPressedOnce = true
//                CoroutineScope(Dispatchers.Main).launch {
//                    delay(delayMillis)
//                    backPressedOnce = false
//                }
//            }
//        }
//    }
//}