package fgapps.com.br.iassistant2.activities

import android.app.IntentService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import fgapps.com.br.iassistant2.services.MusicPlayerService
import fgapps.com.br.iassistant2.services.VoiceService

class NotificationActivity : IntentService(""), ServiceConnection {

    var action: String? = null

    override fun onHandleIntent(service_intent: Intent?) {
        action = service_intent?.extras?.get("notify") as String?

        Intent(this, MusicPlayerService::class.java).also { intent ->
            startService(intent)
            bindService(intent, this, Context.BIND_AUTO_CREATE) // Binds to local service
        }
    }

    override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
        val binder = service as MusicPlayerService.MusicPlayerBinder
        val musicService = binder.getService()

        action?.let {
            when(it){
                "play/pause" -> {
                    if(musicService.isPlaying()) musicService.pause()
                    else musicService.play()
                }
                "next" -> { musicService.next() }
                "prev" -> { musicService.prev() }
                else -> { VoiceService.instance?.listen() }
            }
        }

    }

    override fun onServiceDisconnected(p0: ComponentName?) {    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(this)
    }
}