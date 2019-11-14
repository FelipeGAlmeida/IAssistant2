package fgapps.com.br.iassistant2.utils

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation

class Animations {

    companion object {

        fun fade(view: View, duration: Long, fadeout: Boolean) {

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
                        view.visibility = View.GONE
                    }
                }

                override fun onAnimationStart(animation: Animation?) {
                    if(!fadeout){
                        view.visibility = View.VISIBLE
                    }
                }
            })

            view.startAnimation(alphaAnimation)
        }

    }
}