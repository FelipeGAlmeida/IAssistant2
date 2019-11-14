package fgapps.com.br.iassistant2.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import fgapps.com.br.iassistant2.R
import fgapps.com.br.iassistant2.gestures.GestureController
import fgapps.com.br.iassistant2.interfaces.MusicChangeListener
import fgapps.com.br.iassistant2.interfaces.ShowButtonsListener
import fgapps.com.br.iassistant2.interfaces.VolumeChangeListener
import fgapps.com.br.iassistant2.music.MusicLoader
import fgapps.com.br.iassistant2.utils.Animations
import fgapps.com.br.iassistant2.utils.Permissions
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),
                    MusicChangeListener,
                    VolumeChangeListener,
                    ShowButtonsListener {

    /*** Variables ***/
    private lateinit var mDetector: GestureDetectorCompat
    private lateinit var mGestureController: GestureController

    var mWidth: Int = 0

    /*** Functions ***/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setBackground()
        setGestures()
        setControls()
    }

    private fun setBackground() {
        val background = background_img
        mWidth = background.width
        Glide.with(this)
                .load(R.drawable.back_anim1)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        Handler().postDelayed({
                            Animations.fade(splashscreen_id, 800, true)
                            if(Permissions.checkPermission(this@MainActivity,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Permissions.READ_EXTERNAL_STORAGE_CODE)) loadMusics()
                        }, 800)
                        return false
                    }

                })
                .into(background)
    }

    private fun setGestures(){
        mGestureController = GestureController(this)
        mDetector = GestureDetectorCompat(this, mGestureController)
        mDetector.setOnDoubleTapListener(mGestureController)
        mGestureController.setGestureDetector(mDetector)
    }

    private fun setControls(){
        settings_btn.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                startActivity(intent)
            }

        })
    }

    private fun loadMusics(){
        val musics = MusicLoader.loadAllMusic(this)
        for (music in musics) {
            Log.v("MUSICS", "music: ${music.name}")
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (mDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    /*** Gesture controller response functions ***/
    override fun nextMusic() {
        Toast.makeText(this, "Música seguinte", Toast.LENGTH_LONG).show()
    }

    override fun prevMusic() {
        Toast.makeText(this, "Música anterior", Toast.LENGTH_LONG).show()
    }

    override fun volumeUp(volume: Int) {
        Toast.makeText(this, "Aumentou volume: $volume", Toast.LENGTH_SHORT).show()
    }

    override fun volumeDown(volume: Int) {
        Toast.makeText(this, "abaixou volume: $volume", Toast.LENGTH_SHORT).show()
    }

    override fun showButtons() {
        Animations.fade(settings_btn, 300, false)
        Animations.fade(repeat_btn, 300, false)
        Handler().postDelayed(
                {
                    Animations.fade(settings_btn, 300,true)
                    Animations.fade(repeat_btn, 300,true)
                },3200)
    }

    /*** Request permission Callback ***/
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            Permissions.READ_EXTERNAL_STORAGE_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    loadMusics()
                } else {
                    this.finishAndRemoveTask() //Can't proceed without this permission
                }
                return
            }
        }
    }

    /*** Back button action ***/
    override fun onBackPressed() {
        return
    }
}
