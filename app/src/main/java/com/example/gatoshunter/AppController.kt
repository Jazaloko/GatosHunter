package com.example.gatoshunter

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ProcessLifecycleOwner

class AppController : Application() {

    override fun onCreate() {
        super.onCreate()
        // Registrar observer del ciclo de vida global
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleListener(this))
    }

    fun startMusicService() {
        val intent = Intent(this, MusicService::class.java)
        startService(intent)
    }

    fun stopMusicService() {
        val intent = Intent(this, MusicService::class.java)
        stopService(intent)
    }
}
