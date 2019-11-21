package fgapps.com.br.iassistant2.gestures

import android.os.Handler
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import fgapps.com.br.iassistant2.activities.MainActivity
import fgapps.com.br.iassistant2.defines.Constants
import fgapps.com.br.iassistant2.utils.Utils

class GestureController(mainActivity: MainActivity) : GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    //Variables
    private lateinit var mDetector: GestureDetectorCompat
    private var mActivity: MainActivity = mainActivity
    private var mHandler: Handler? = null

    fun setGestureDetector(detector: GestureDetectorCompat){
        mDetector = detector
    }

    override fun onScroll(
            downEvent: MotionEvent,
            moveEvent: MotionEvent,
            distanceX: Float,
            distanceY: Float): Boolean {

        if(mHandler == null){
            mHandler = Handler()
        }

        val screenX = mActivity.getAppWidth()
        val delay: Long
        val isMusic: Boolean
        if(downEvent.x < (screenX * Constants.WIDTH_MUSICCHANGE)){
            delay = Constants.DELAY_MUSICCHANGE
            isMusic = true
        }else{
            delay = Constants.DELAY_VOLUMECHANGE
            isMusic = false
        }

        mHandler!!.removeCallbacksAndMessages(null)
        mHandler!!.postDelayed({
            analyseGesture(downEvent.x, downEvent.y, moveEvent.x, moveEvent.y, isMusic)
        }, delay)
        return true
    }

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
        mActivity.singlePress()
        return true
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
        mActivity.doublePress()
        return true
    }

    override fun onLongPress(event: MotionEvent) {
        mActivity.longPress()
    }

    private fun analyseGesture(startX: Float, startY: Float, endX: Float, endY: Float, isMusic: Boolean){

//        if(Math.abs(startX - endX) > Math.abs(startY - endY)) { //left and right moves (unactive)
//            if (startX < endX) {
//                Log.d(DEBUG_TAG, "onScroll_PARA_DIREITA")
//            } else {
//                Log.d(DEBUG_TAG, "onScroll_PARA_ESQUERDA")
//            }
//        } else {
//        }

        if (startY < endY) {
            when(isMusic) {
                true -> { mActivity.prevMusic() }
                false-> { mActivity.volumeChange(Utils.boundVolumeValues(endY, mActivity.getAppHeight())) }
            }
        } else {
            when(isMusic) {
                true -> { mActivity.nextMusic() }
                false-> { mActivity.volumeChange(Utils.boundVolumeValues(endY, mActivity.getAppHeight())) }
            }
        }
    }

    /* Unused override functions */
    override fun onShowPress(p0: MotionEvent?) {
    }
    override fun onDown(event: MotionEvent): Boolean {
        return true
    }
    override fun onSingleTapUp(event: MotionEvent): Boolean {
        return true
    }
    override fun onDoubleTapEvent(event: MotionEvent): Boolean {
        return true
    }
    override fun onFling(
            event1: MotionEvent,
            event2: MotionEvent,
            velocityX: Float,
            velocityY: Float): Boolean {
        return true
    }
}