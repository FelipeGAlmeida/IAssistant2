package fgapps.com.br.iassistant2

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    //Variables
    var DEBUG_TAG: String = "IA2 : DEBUG"
    private lateinit var mDetector: GestureDetectorCompat
    private var mHandler: Handler? = null
    private var volume_test = 40;

    //Functions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setBackground()
        setGestures()
    }

    private fun setBackground() {
        val background = background_img
        Glide.with(this)
                .load(R.drawable.back_anim1)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        Log.d(DEBUG_TAG, "BACKGROUND LOAD FAILED")
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        Log.d(DEBUG_TAG, "BACKGROUND LOAD SUCCESS")
                        Handler().postDelayed({ splashscreen_id.visibility = View.GONE }, 500)
                        return false
                    }

                })
                .into(background)
    }

    private fun setGestures(){
        mDetector = GestureDetectorCompat(this, this)
        mDetector.setOnDoubleTapListener(this)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (mDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun onScroll(
            downEvent: MotionEvent,
            moveEvent: MotionEvent,
            distanceX: Float,
            distanceY: Float): Boolean {

        if(mHandler == null){
            mHandler = Handler()
        }

        val screenX = background_img.width
        var delay: Long
        var isMusic: Boolean
        if(downEvent.x < (screenX * 80 / 100)){
            //Log.d(DEBUG_TAG, "onScroll_MUSICA")
            delay = 100
            isMusic = true
        }else{
            //Log.d(DEBUG_TAG, "onScroll_VOLUME")
            delay = 15
            isMusic = false
        }

        mHandler!!.removeCallbacksAndMessages(null)
        mHandler!!.postDelayed({
            analyseGesture(downEvent.x, downEvent.y, moveEvent.x, moveEvent.y, isMusic)
        }, delay)
        return true
    }

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onSingleTapUp: $event")
        return true
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onDoubleTap: $event")
        enableHomeButtons()
        return true
    }

    private fun enableHomeButtons() {
        settings_btn.visibility = View.VISIBLE
        repeat_btn.visibility = View.VISIBLE
        Handler().postDelayed(
                {
                    settings_btn.visibility = View.GONE
                    repeat_btn.visibility = View.GONE
                },3200)
    }

    override fun onLongPress(event: MotionEvent) {
        Log.d(DEBUG_TAG, "onLongPress: $event")
    }

    fun analyseGesture(startX: Float, startY: Float, endX: Float, endY: Float, isMusic: Boolean){

//        if(Math.abs(startX - endX) > Math.abs(startY - endY)) {
//            if (startX < endX) {
//                Log.d(DEBUG_TAG, "onScroll_PARA_DIREITA")
//            } else {
//                Log.d(DEBUG_TAG, "onScroll_PARA_ESQUERDA")
//            }
//        } else {
            if (startY < endY) {
                when(isMusic) {
                    true -> {Log.d(DEBUG_TAG, "MUSICA ANTERIOR")}
                    false-> { volume_test-=2
                              boundVolume()
                              Log.d(DEBUG_TAG, "VOLUME: $volume_test")
                            }
                }
            } else {
                when(isMusic) {
                    true -> {Log.d(DEBUG_TAG, "MUSICA SEGUINTE")}
                    false-> { volume_test+=2
                              boundVolume()
                              Log.d(DEBUG_TAG, "VOLUME: $volume_test")
                    }
                }
            }
//        }
    }

    fun boundVolume(){
        if(volume_test > 100){
            volume_test = 100
            return
        }
        if(volume_test < 0){
            volume_test = 0
            return
        }
    }

    //UNUSED OVERRIDEN FUNCTIONS
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
