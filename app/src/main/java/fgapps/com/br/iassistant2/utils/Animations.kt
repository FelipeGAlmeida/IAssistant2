package fgapps.com.br.iassistant2.utils

import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import fgapps.com.br.iassistant2.activities.MainActivity
import java.util.*

class Animations {

    companion object {

        private var blink_flag: Boolean = true
        private var timer: Timer?= null
        fun blink(mainActivity: MainActivity, view: View, duration: Long){
            timer = Timer()
            timer!!.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    if(blink_flag) fade(mainActivity, view, duration / 2, true)
                    else fade(mainActivity, view, duration / 2, false)
                    blink_flag = !blink_flag
                }

                override fun cancel(): Boolean {
                    view.visibility = View.VISIBLE
                    Log.d("TIMER", "BLINK CANCELED")
                    return super.cancel()
                }
            },0 , duration/2)
        }

        fun stopBlink(){
            if(timer != null){
                timer!!.cancel()
            }
        }

        fun fade(mainActivity: MainActivity, view: View, duration: Long, fadeout: Boolean) {

            fun Boolean.toFloat() = if(this) 1f else 0f

            val alphaAnimation = AlphaAnimation(fadeout.toFloat(), (!fadeout).toFloat())
            alphaAnimation.duration = duration
            alphaAnimation.repeatCount = 0
            alphaAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                    //Not used
                }

                override fun onAnimationEnd(animation: Animation?) {
                    if(fadeout) {
                        view.visibility = View.INVISIBLE
                    }
                }

                override fun onAnimationStart(animation: Animation?) {
                    if(!fadeout){
                        view.visibility = View.VISIBLE
                    }
                }
            })
            mainActivity.runOnUiThread { view.startAnimation(alphaAnimation) }
        }

    }
}