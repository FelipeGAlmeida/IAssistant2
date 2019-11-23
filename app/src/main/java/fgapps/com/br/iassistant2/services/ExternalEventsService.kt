package fgapps.com.br.iassistant2.services

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import fgapps.com.br.iassistant2.activities.MainActivity
import fgapps.com.br.iassistant2.defines.Constants
import fgapps.com.br.iassistant2.defines.MediaPlayerStates
import fgapps.com.br.iassistant2.utils.Utils
import java.util.*
import kotlin.concurrent.timerTask


class ExternalEventsService(mainActivity: MainActivity, musicService: MusicPlayerService): PhoneStateListener() {

    val mActivity = mainActivity
    val mMusicService = musicService

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                // Audio will start on phone speaker, so pause to avoid unwanted noise
                mMusicService.pause()
                return
            }

            if (action == AudioManager.ACTION_HEADSET_PLUG) {
                if(intent.getIntExtra("state", 0) > 0)
                    if(mMusicService.getPlayerPreviousState() == MediaPlayerStates.STARTED)
                            mMusicService.play()
                return
            }

            if(action == BluetoothDevice.ACTION_ACL_CONNECTED) {
                var timeout = Constants.A2DP_TIMEOUT
                Timer().scheduleAtFixedRate(timerTask {
                    mActivity.let {
                        if(Utils.isHeadsetPlugged(mActivity)){
                            if(mMusicService.getPlayerPreviousState() ==
                                    MediaPlayerStates.STARTED)
                                mMusicService.play()
                            cancel()
                        }
                        if(timeout <= 0) cancel()
                        timeout--
                    }
                }, 0, Constants.A2DP_PERIOD)
                return
            }
        }
    }

    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
        when(state){
            TelephonyManager.CALL_STATE_IDLE ->{
//                if(mMusicService.getPlayerPreviousState() == MediaPlayerStates.STARTED)
//                    mMusicService.play()  //BUG - Called onResume
            }
            else -> mMusicService.pause()
        }
    }

    fun stopExternalMonitoring(){
        mActivity.unregisterReceiver(receiver)
    }

    init {
        // Headset monitoring
        val filter = IntentFilter()
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        filter.addAction(AudioManager.ACTION_HEADSET_PLUG)
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        mActivity.registerReceiver(receiver, filter)

        // Calls monitoring
        val tm = mActivity.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        tm.listen(this, PhoneStateListener.LISTEN_CALL_STATE)
    }

}