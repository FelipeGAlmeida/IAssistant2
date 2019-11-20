package fgapps.com.br.iassistant2.utils

import java.text.Normalizer
import java.util.*


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

        fun normalizeStrings(raw_string: String,
                             accents: Boolean,
                             lowerCase: Boolean,
                             conjunctions: Boolean): String{
            var string = raw_string

            /*** specific corrections ***/
            string = string
                    .replace("é", "ehh")
                    .replace("&", "e")
                    //Commands with composed actions
                    .replace("ir para", "ir_para")
                    .replace("pular para", "pular_para")
                    .replace("go to", "go_to")
                    .replace("jump to", "jump_to")

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

            return string
        }

        fun getCurrentTime(): String{
            val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val m = Calendar.getInstance().get(Calendar.MINUTE)
            val h_zero = if(h<10) "0" else ""
            val m_zero = if(m<10) "0" else ""

            return "$h_zero$h:$m_zero$m"
        }

//        fun showAlertDialog(mainActivity: MainActivity, title: String, message: String){
//            val builder = AlertDialog.Builder(mainActivity)
//            builder.setTitle("Titulo")
//            builder.setMessage("Qualifique este software")
//            builder.setPositiveButton("Yes") { arg0, arg1 -> Toast.makeText(mainActivity, "positivo=$arg1", Toast.LENGTH_SHORT).show() }
//            builder.setNegativeButton("No") { arg0, arg1 -> Toast.makeText(mainActivity, "negativo=$arg1", Toast.LENGTH_SHORT).show() }
//            val alert = builder.create()
//            alert.show()
//        }
    }

}