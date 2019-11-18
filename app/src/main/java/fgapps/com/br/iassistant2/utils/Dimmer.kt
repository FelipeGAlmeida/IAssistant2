package fgapps.com.br.iassistant2.utils

import fgapps.com.br.iassistant2.activities.MainActivity
import java.util.*

class Dimmer(mainActivity: MainActivity) {

    private val mMainActivity = mainActivity
    private var mTimer: Timer = Timer()

    private var mIdleTime = 15

    fun init(){
        mTimer.scheduleAtFixedRate(mTimerTask, 0, 1000) //Verify if has to Dimmer each second
    }

    private val mTimerTask = object: TimerTask() {
        override fun run() {
            if(mIdleTime == 0) down()
            else if(mIdleTime > 0) mIdleTime--
        }
    }

    fun isDimmeredDown(): Boolean{
        return mIdleTime < 0
    }

    fun down(){
        val attributes = mMainActivity.window.attributes
        attributes.screenBrightness = 0f
        mMainActivity.runOnUiThread {
            mMainActivity.window.attributes = attributes
            mIdleTime = -1
        }
    }

    fun up(){
        val attributes = mMainActivity.window.attributes
        attributes.screenBrightness = -1f
        mMainActivity.runOnUiThread{
            mMainActivity.window.attributes = attributes
            mIdleTime = 15
        }
    }
}