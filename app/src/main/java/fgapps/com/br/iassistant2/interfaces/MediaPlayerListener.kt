package fgapps.com.br.iassistant2.interfaces

import fgapps.com.br.iassistant2.music.Music
import fgapps.com.br.iassistant2.defines.MediaPlayerStates

interface MediaPlayerListener {

    fun stateChanged(state: MediaPlayerStates)

    fun musicChanged(curr: Music, prev: Music, next: Music)
}