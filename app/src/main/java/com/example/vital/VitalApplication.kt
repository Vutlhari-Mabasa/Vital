package com.example.vital

import android.app.Application
import android.content.Context

class VitalApplication : Application() {
    
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.attachBaseContext(base))
    }
}

