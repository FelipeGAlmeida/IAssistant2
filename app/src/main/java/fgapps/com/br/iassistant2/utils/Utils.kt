package fgapps.com.br.iassistant2.utils

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import fgapps.com.br.iassistant2.activities.MainActivity
import java.text.Normalizer
import java.util.*


object Utils {

    fun boundVolumeValues(position: Float, size: Int) : Float {
        var volume = 1-(position/size)

        if(volume >= 0.85) volume = 1f
        if(volume <= 0.15) volume = 0f

        return volume
    }

    fun boundMusicIndexes(size: Int, curr: Int): IntArray{
        var prev = curr - 1
        var next = curr + 1

        if(prev < 0) prev = size - 1
        if(next > size-1) next = 0

        return intArrayOf(curr, prev, next)
    }

    fun normalizeStrings(raw_string: String,
                         accents: Boolean,
                         lowerCase: Boolean,
                         conjunctions: Boolean): String{
        var string = raw_string

        /*** specific corrections ***/
        string = string
                .replace("é", "ehh", true)
                .replace("&", "e")
                //Commands with composed actions
                .replace("ir para", "ir_para", true)
                .replace("pular para", "pular_para", true)
                .replace("go to", "go_to", true)
                .replace("jump to", "jump_to", true)

        if(conjunctions) {
            string = string
                    .replace(" da", "")
                    .replace(" de", "")
                    .replace(" do", "")
        }

        /*** accents correction ***/
        if(accents) {
            string = Normalizer.normalize(string, Normalizer.Form.NFD)
                    .replace(Regex("[^\\p{ASCII}]"), "")
        }

        /*** lowerCase correction ***/
        if(lowerCase) string = string.toLowerCase()

        /*** dot at end correction ***/
        if(string.endsWith(".")) string = string.dropLast(1)

        return string
    }

    fun getCurrentTime(asString: Boolean): String{
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val m = Calendar.getInstance().get(Calendar.MINUTE)
        val h_zero = if(h<10) "0" else ""
        val m_zero = if(m<10) "0" else ""

        return if(asString) "Agora são $h e $m" else "$h_zero$h:$m_zero$m"
    }

    fun enableKeyboard(mainActivity: MainActivity, enable: Boolean, view: View){
        val imm = mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        when(enable) {
            false -> imm?.hideSoftInputFromWindow(view.windowToken, 0)
            true -> imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
        view.requestFocus()
    }

    fun isHeadsetPlugged(mActivity: MainActivity): Boolean{
        val am = mActivity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

        for (i in devices.indices) {
            val device = devices[i]

            if (device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                    device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES){
                return true // Plugged as wired device
            }
            if (device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP){
                return true // Plugged as Bluetooth Audio (Must be Audio)
            }
        }

        return false
    }

//    fun showAlertDialog(mainActivity: MainActivity, title: String, message: String){
//        val builder = AlertDialog.Builder(mainActivity)
//        builder.setTitle("Titulo")
//        builder.setMessage("Qualifique este software")
//        builder.setPositiveButton("Yes") { arg0, arg1 -> Toast.makeText(mainActivity, "positivo=$arg1", Toast.LENGTH_SHORT).show() }
//        builder.setNegativeButton("No") { arg0, arg1 -> Toast.makeText(mainActivity, "negativo=$arg1", Toast.LENGTH_SHORT).show() }
//        val alert = builder.create()
//        alert.show()
//    }

}