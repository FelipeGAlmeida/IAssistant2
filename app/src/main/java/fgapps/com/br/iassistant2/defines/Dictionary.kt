package fgapps.com.br.iassistant2.defines

class Dictionary {

    companion object {

        /* *** Constants *** */
        const val MUSIC = "MUSIC"
        const val TIME = "TIME"
        const val PLAY = "PLAY"
        const val PAUSE = "PAUSE"
        const val NEXT = "NEXT"
        const val PREV = "PREV"
        const val FOLDER = "FOLDER"
        const val ALL = "ALL"
        const val ADD = "ADD"

        /* *** Maps *** */
        var actions = mutableMapOf<String, ArrayList<String>>()
        var complements = mutableMapOf<String, ArrayList<String>>()
        var extras = mutableMapOf<String, ArrayList<String>>()

        fun init() {
            /* ***** Actions ***** */
            val a_add = arrayListOf("adiciona", "inseri")
            a_add += arrayListOf("add", "insert")
            actions[ADD] = a_add

            val a_play = arrayListOf("ouvi", "escuta", "reproduzi", "toca", "iniciar", "continu", "retomar")
            a_play += arrayListOf("listen", "play", "start")
            actions[PLAY] = a_play

            val a_next = arrayListOf("pula", "avanca", "troca", "muda")
            a_next += arrayListOf("skip", "change", "go to next")
            actions[NEXT] = a_next

            val a_pause = arrayListOf("pausa", "para", "pare")
            a_pause += arrayListOf("pause", "stop")
            actions[PAUSE] = a_pause

            val a_prev = arrayListOf("volta", "retrocede")
            a_prev += arrayListOf("back", "go to previous")
            actions[PREV] = a_prev

            val a_time = arrayListOf("sao", "ehh")
            a_time += arrayListOf("is it")
            actions[TIME] = a_time

            /* ***** Complements ***** */
            val c_music = arrayListOf("music")
            c_music += arrayListOf("song")
            complements[MUSIC] = c_music

            val c_folder = arrayListOf("pasta")
            c_folder += arrayListOf("folder")
            complements[FOLDER] = c_folder

            val c_time = arrayListOf("hora")
            c_time += arrayListOf("time")
            complements[TIME] = c_time

            /* ***** Extras ***** */
            val e_all = arrayListOf("toda", "todo", "tudo")
            e_all += arrayListOf("all")
            extras[ALL] = e_all

            val e_next = arrayListOf("proxim", "seguinte")
            e_next += arrayListOf("next")
            extras[NEXT] = e_next

            val e_prev = arrayListOf("anterior", "antes")
            e_prev += arrayListOf("previous")
            extras[PREV] = e_prev

            val e_time = arrayListOf("que")
            e_time += arrayListOf("what")
            extras[TIME] = e_time

            val e_unpause = arrayListOf("reproducao", "lista")
            e_unpause += arrayListOf("playlist")
            extras[PLAY] = e_unpause
        }
    }
}