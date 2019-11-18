package fgapps.com.br.iassistant2.utils

import android.widget.Toast
import fgapps.com.br.iassistant2.activities.MainActivity
import androidx.appcompat.app.AlertDialog


class Utils {

    companion object {

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

        fun showAlertDialog(mainActivity: MainActivity, title: String, message: String){
            val builder = AlertDialog.Builder(mainActivity)
            builder.setTitle("Titulo")
            builder.setMessage("Qualifique este software")
            builder.setPositiveButton("Yes") { arg0, arg1 -> Toast.makeText(mainActivity, "positivo=$arg1", Toast.LENGTH_SHORT).show() }
            builder.setNegativeButton("No") { arg0, arg1 -> Toast.makeText(mainActivity, "negativo=$arg1", Toast.LENGTH_SHORT).show() }
            val alert = builder.create()
            alert.show()
        }
    }

}