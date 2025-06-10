package com.example.paymates

import android.app.Application
import com.example.paymates.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class PayMatesApplication() : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin{
            androidContext(this@PayMatesApplication)
            modules(appModule)
        }
    }
}