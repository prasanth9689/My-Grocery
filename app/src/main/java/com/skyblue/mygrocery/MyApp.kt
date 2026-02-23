package com.skyblue.mygrocery

import android.app.Application
import com.google.firebase.FirebaseApp
import com.skyblue.mygrocery.utils.SessionHandler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp: Application(){
    override fun onCreate() {
        super.onCreate()
        SessionHandler.init(this)
        FirebaseApp.initializeApp(this)
    }
}