package fgapps.com.br.iassistant2.defines

class Dictionary {

    companion object {
        
        val music = "MUSIC"
        val time = "TIME"
        val play = "PLAY"
        val next = "NEXT"
        val prev = "PREV"
        val folder = "FOLDER"
        val genre = "GENRE"
        val all = "ALL"
        
        var extras = mutableMapOf<String, String>()
        var actions = mutableMapOf<String ,String>()
        var complements = mutableMapOf<String, String>()

        init {
            /* Actions */
            actions[play] = "ouvi"; actions[play] = "escuta"; actions[play] = "reproduzi"
            actions[play] = "toca"; actions[play] = "iniciar"
            actions[play] = "listen"; actions[play] = "play"; actions[play] = "Start"

            actions[next] = "pula"; actions[next] = "avança"; actions[next] = "troca"; actions[next] = "muda"
            actions[next] = "skip"; actions[next] = "change"

            actions[prev] = "volta"; actions[prev] = "retrocede"
            actions[prev] = "back"

            actions[time] = "são"; actions[time] = "é"
            actions[time] = "is"

            /* Complements */
            complements[music] = "music"
            complements[music] = "song"

            complements[folder] = "pasta"
            complements[folder] = "folder"

            complements[genre] = "genero"
            complements[genre] = "genre"

            complements[time] = "hora"
            complements[time] = "time"

            /* Extras */
            extras[all] = "toda"; extras[music] = "todo"
            extras[all] = "all"

            extras[next] = "proxima"; extras[music] = "seguinte"
            extras[next] = "next"

            extras[prev] = "anterio"; extras[music] = "antes"
            extras[prev] = "previous"

            extras[time] = "que"
            extras[time] = "what"
        }
    }
}