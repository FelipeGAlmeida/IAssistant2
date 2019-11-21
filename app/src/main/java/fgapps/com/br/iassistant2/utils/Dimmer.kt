package fgapps.com.br.iassistant2.utils

import fgapps.com.br.iassistant2.activities.MainActivity
import fgapps.com.br.iassistant2.defines.Constants
import fgapps.com.br.iassistant2.defines.Settings
import java.util.*

class Dimmer(mainActivity: MainActivity) {

    private val mMainActivity = mainActivity
    private var mTimer: Timer = Timer()

    private var mIdleTime = Settings.DIMMER_TIME

    fun init(){
        mTimer.scheduleAtFixedRate(mTimerTask, 0, Constants.SECOND) //Verify if has to Dimmer each second
    }

    private val mTimerTask = object: TimerTask() {
        override fun run() {
            if(mIdleTime == 0) down()
            else if(mIdleTime > 0) mIdleTime--
        }
    }

    fun isDimmedDown(): Boolean{
        return mIdleTime == Constants.DIMMERDOWN_TIME
    }

    fun down(){
        val attributes = mMainActivity.window.attributes
        attributes.screenBrightness = Constants.MIN_DIMMER
        mMainActivity.runOnUiThread {
            mMainActivity.window.attributes = attributes
            mIdleTime = Constants.DIMMERDOWN_TIME
        }
    }

    fun up(){
        val attributes = mMainActivity.window.attributes
        attributes.screenBrightness = Constants.MAX_DIMMER
        mMainActivity.runOnUiThread{
            mMainActivity.window.attributes = attributes
            mIdleTime = Settings.DIMMER_TIME
        }
    }
}