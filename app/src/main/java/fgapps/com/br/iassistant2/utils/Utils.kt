package fgapps.com.br.iassistant2.utils

class Utils {

    companion object {

        fun boundVolumeValues(volume: Int) : Int {
            if(volume > 100) return 100
            if(volume < 0) return 0
            return volume
        }
    }

}