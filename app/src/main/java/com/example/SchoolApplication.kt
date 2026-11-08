package com.example

import android.app.Application
import com.example.data.repository.SchoolRepository

class SchoolApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SchoolRepository.initialize(this)
    }
}
