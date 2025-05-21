// AppLifecycleListener.kt
package com.example.gatoshunter

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class AppLifecycleListener(private val app: AppController) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        // La app entra en primer plano
        app.startMusicService()
    }

    override fun onStop(owner: LifecycleOwner) {
        // La app entra en segundo plano
        app.stopMusicService()
    }
}
