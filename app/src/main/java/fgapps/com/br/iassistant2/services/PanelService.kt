package fgapps.com.br.iassistant2.services

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

    private val mPanelQueue = arrayListOf(Panels.VOICE) //Panel FIFO Queue

    private var mCurrentPanel = Panels.NONE

    fun enablePanel(panel: Panels){
        if(panel != mPanelQueue[0]) {
            if (panel != Panels.BACK)
                mPanelQueue.add(0, panel)
            else enablePreviousPanel()
        }

        mCurrentPanel = mPanelQueue[0]
        enableMusicPanel(mCurrentPanel == Panels.MUSIC)
        enableVoicePanel(mCurrentPanel == Panels.VOICE)
        enableControlsPanel(mCurrentPanel == Panels.CONTROLS)
        enableTypePanel(mCurrentPanel == Panels.TYPE)
        enableShufflePanel(mCurrentPanel == Panels.SHUFFLE || mCurrentPanel == Panels.MUSIC)
    }

    private fun enableMusicPanel(enable: Boolean){
        if(checkWillBeChanged(mMusicPanel, enable)) {
            mMusicPanel.visibility = when (enable) {
                true -> View.VISIBLE
                false -> View.INVISIBLE
            }
        }
    }

    private fun enableVoicePanel(enable: Boolean){
        if(checkWillBeChanged(mVoicePanel, enable)) {
            mVoicePanel.visibility = when (enable) {
                true -> View.VISIBLE
                false -> View.INVISIBLE
            }
        }
    }

    private fun enableControlsPanel(enable: Boolean){
        if(checkWillBeChanged(mControlsPanel, enable)) {
            mControlsPanel.visibility = when (enable) {
                true -> View.VISIBLE
                false -> View.INVISIBLE
            }
        }
    }

    private fun enableTypePanel(enable: Boolean){
        if(checkWillBeChanged(mTypePanel, enable)) {
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
        if(checkWillBeChanged(mShufflePanel, enable)) {
            mShufflePanel.visibility = when (enable) {
                true -> View.VISIBLE
                false -> View.INVISIBLE
            }
        }
    }

    private fun checkWillBeChanged(view: View, visible: Boolean): Boolean{
        if(view.visibility == View.INVISIBLE && visible) // Will become visible
            return true
        if(view.visibility == View.VISIBLE && !visible) { // Will become invisible
            return true
        }
        return false
    }

    private fun enablePreviousPanel(){
        if(mPanelQueue.size > 1)
            mPanelQueue.removeAt(0)
        enablePanel(mPanelQueue[0])
    }

}