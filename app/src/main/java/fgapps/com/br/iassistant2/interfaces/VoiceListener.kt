package fgapps.com.br.iassistant2.interfaces

import fgapps.com.br.iassistant2.defines.VoiceStates

interface VoiceListener {

    fun onListenAction(listen: String?, state: VoiceStates)

    fun onSpeakAction(spoke: String?, tip: String?, state: VoiceStates)
}