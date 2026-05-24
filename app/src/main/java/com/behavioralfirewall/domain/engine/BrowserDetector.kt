package com.behavioralfirewall.domain.engine

import com.behavioralfirewall.core.constants.AppConstants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BrowserDetector @Inject constructor() {

    fun isBrowser(packageName: String): Boolean {
        return AppConstants.KNOWN_BROWSERS.contains(packageName)
    }

    fun isPlayStore(packageName: String): Boolean {
        return AppConstants.PLAY_STORE_PACKAGE == packageName
    }
}
