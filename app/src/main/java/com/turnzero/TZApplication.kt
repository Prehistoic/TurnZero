package com.turnzero

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TZApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
