package fgapps.com.br.iassistant2.services

import android.os.Handler
import android.util.Log
import android.view.View
import fgapps.com.br.iassistant2.activities.MainActivity
import fgapps.com.br.iassistant2.defines.Panels
import fgapps.com.br.iassistant2.utils.Utils

class PanelService(
        mainActivity: MainActivity,
        musicPanel: View,
        voicePanel: View,
        controlsPanel: View,
        typePanel: View,
        shufflePanel: View
) {

    private val mActivity = mainActivity
    private val mMusicPanel = musicPanel
    private val mVoicePanel = voicePanel
    private val mControlsPanel = controlsPanel
    private val mTypePanel = typePanel
    private val mShufflePanel = shufflePanel

    private var mCurrentPanel = Panels.VOICE
    private var mPreviousPanel = Panels.NONE

    fun enablePanel(panel: Panels){
        mCurrentPanel = panel
        Log.d("PANELS ", "CURR PANEL: ${this.mCurrentPanel}")
        if(mPreviousPanel == Panels.NONE) mPreviousPanel = this.mCurrentPanel

        enableMusicPanel(mCurrentPanel == Panels.MUSIC)
        enableVoicePanel(mCurrentPanel == Panels.VOICE)
        enableControlsPanel(mCurrentPanel == Panels.CONTROLS)
        enableTypePanel(mCurrentPanel == Panels.TYPE)
        enableShufflePanel(mCurrentPanel == Panels.SHUFFLE || mCurrentPanel == Panels.MUSIC)

        if(mCurrentPanel == Panels.NONE) enablePanel(mPreviousPanel)
    }

    private fun enableMusicPanel(enable: Boolean){
        if(checkWillBeChanged(mMusicPanel, Panels.MUSIC, enable)) {
            mMusicPanel.visibility = when (enable) {
                true -> View.VISIBLE
                false -> View.INVISIBLE
            }
        }
    }

    private fun enableVoicePanel(enable: Boolean){
        if(checkWillBeChanged(mVoicePanel, Panels.VOICE, enable)) {
            mVoicePanel.visibility = when (enable) {
                true -> View.VISIBLE
                false -> View.INVISIBLE
            }
        }
    }

    private fun enableControlsPanel(enable: Boolean){
        if(checkWillBeChanged(mControlsPanel, Panels.CONTROLS, enable)) {
            mControlsPanel.visibility = when (enable) {
                true -> View.VISIBLE
                false -> View.INVISIBLE
            }
        }
    }

    private fun enableTypePanel(enable: Boolean){
        if(checkWillBeChanged(mTypePanel, Panels.TYPE, enable)) {
            mTypePanel.visibility = when (enable) {
                true -> {
                    Utils.enableKeyboard(mActivity, true, mTypePanel)
                    View.VISIBLE
                }
                false -> {
                    Utils.enableKeyboard(mActivity, false, mTypePanel)
                    View.INVISIBLE
                }
            }
        }
    }

    private fun enableShufflePanel(enable: Boolean){
        if(checkWillBeChanged(mShufflePanel, Panels.MUSIC, enable)) {
            mShufflePanel.visibility = when (enable) {
                true -> View.VISIBLE
                false -> View.INVISIBLE
            }
        }
    }

    private fun checkWillBeChanged(view: View, panel: Panels, visible: Boolean): Boolean{
        if(view.visibility == View.INVISIBLE && visible) // Will become visible
            return true
        if(view.visibility == View.VISIBLE && !visible) { // Will become invisible
            if(panel != Panels.CONTROLS && panel != Panels.TYPE && panel != Panels.NONE && panel != Panels.VOICE){
                mPreviousPanel = panel
            }
            return true
        }
        return false
    }

    private fun enablePreviousPanel(){
        enablePanel(mPreviousPanel)
    }

}