package fgapps.com.br.iassistant2.defines

class Dictionary {

    companion object {
        
        val music = "MUSIC"
        val time = "TIME"
        val play = "PLAY"
        val pause = "PAUSE"
        val next = "NEXT"
        val prev = "PREV"
        val folder = "FOLDER"
        val all = "ALL"

        var actions = mutableMapOf<String, ArrayList<String>>()
        var complements = mutableMapOf<String, ArrayList<String>>()
        var extras = mutableMapOf<String, ArrayList<String>>()

        fun init() {
            /* ***** Actions ***** */
            val a_play = arrayListOf("ouvi", "escuta", "reproduzi", "toca", "iniciar", "continu", "retomar")
            a_play += arrayListOf("listen", "play", "start")
            actions[play] = a_play

            val a_next = arrayListOf("pula", "avanca", "troca", "muda")
            a_next += arrayListOf("skip", "change", "go to next")
            actions[next] = a_next

            val a_pause = arrayListOf("pausa", "para", "pare")
            a_pause += arrayListOf("pause", "stop")
            actions[pause] = a_pause

            val a_prev = arrayListOf("volta", "retrocede")
            a_prev += arrayListOf("back", "go to previous")
            actions[prev] = a_prev

            val a_time = arrayListOf("sao", "ehh")
            a_time += arrayListOf("is")
            actions[time] = a_time

            /* ***** Complements ***** */
            val c_music = arrayListOf("music")
            c_music += arrayListOf("song")
            complements[music] = c_music

            val c_folder = arrayListOf("pasta")
            c_folder += arrayListOf("folder")
            complements[folder] = c_folder

            val c_time = arrayListOf("hora")
            c_time += arrayListOf("time")
            complements[time] = c_time

            /* ***** Extras ***** */
            val e_all = arrayListOf("toda", "todo", "tudo")
            e_all += arrayListOf("all")
            extras[all] = e_all

            val e_next = arrayListOf("proxim", "seguinte")
            e_next += arrayListOf("next")
            extras[next] = e_next

            val e_prev = arrayListOf("anterior", "antes")
            e_prev += arrayListOf("previous")
            extras[prev] = e_prev

            val e_time = arrayListOf("que")
            e_time += arrayListOf("what")
            extras[time] = e_time

            val e_unpause = arrayListOf("reproducao", "lista")
            e_unpause += arrayListOf("playlist")
            extras[play] = e_unpause
        }
    }
}