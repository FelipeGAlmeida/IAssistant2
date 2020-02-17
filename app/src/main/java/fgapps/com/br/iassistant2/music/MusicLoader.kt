package fgapps.com.br.iassistant2.music

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import fgapps.com.br.iassistant2.defines.Constants
import fgapps.com.br.iassistant2.utils.Permissions
import fgapps.com.br.iassistant2.utils.Utils


class MusicLoader {

    companion object {

        var allMusic: ArrayList<Music> = ArrayList()

        /* ***** Load all the musics in the phone ***** */
        fun loadAllMusic(context: Context) {

            val musicResolver = context.contentResolver
            val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val musicCursor = musicResolver.query(musicUri,
                                        null,
                                        null,
                                        null,
                                        null)

            if(musicCursor != null && musicCursor.moveToFirst()){
                //Get columns
                val titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val c = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA)

                do {
                    val thisId = musicCursor.getLong(idColumn)
                    val thisTitle = musicCursor.getString(titleColumn)
                    val thisArtist = musicCursor.getString(artistColumn)
                    var thisFolder = musicCursor.getString(c)
                    val path = thisFolder.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val thisName = path[path.size - 1].dropLast(4)
                    thisFolder = path[path.size - 2]
                    if (!thisName.contains("AUD-") && !thisFolder.contains("WhatsApp Audio"))
                        allMusic.add(Music(thisId, thisName, thisTitle, thisArtist, thisFolder))
                    else
                        Log.v("MUSIC_LIST", "Song $thisName wasn't added!")
                }while(musicCursor.moveToNext())

                musicCursor.close()
            }
        }

        fun getPlaylistFromIds(ids: ArrayList<Long>): ArrayList<Music>{
            val playlist = ArrayList<Music>()
            for(id in ids){
                playlist.add(allMusic[allMusic.indexOf(Music(id, "", "", "", ""))])
            }
            return playlist
        }

        /* ***** Get a playlist based on specific input ***** */
        fun getPlaylistFromPayload(payload: String, isFolder: Boolean): ArrayList<Music>{

            val playlist = ArrayList<Music>()
            for(music in allMusic){

                val source = when(isFolder){
                    false -> music.name
                    true -> music.folder
                }

                if(match(source, payload))
                    playlist.add(music)
            }

            if(!isFolder && playlist.isEmpty()){
                deepSearch(playlist, payload)
            }

            return playlist
        }

        fun deepSearch(playlist: ArrayList<Music>, payload: String){
            val words = payload.split(" ")
            val n_music = allMusic.size
            val max_match = words.size
            val matches = IntArray(n_music)
            for (i in 0 until n_music) {
                val s = allMusic.get(i)
                for (word in words) {
                    if (match(s.name, word) || match(s.artist, word) || match(s.title, word)) {
                        matches[i]++
                    }
                }
            }

            for (i in matches.indices) {
                if (matches[i] == max_match) { // We check if was a max match
                    if (!playlist.contains(allMusic[i]))
                        playlist.add(0, allMusic[i]) // Then we put in the beginning
                } else if(matches[i] == max_match - 1 &&
                        max_match > Constants.MIN_MATCHES){ // Nearly max matches are allowed too
                    if (!playlist.contains(allMusic[i]))
                        playlist.add(allMusic[i]) // Else, we put in the end
                }
            }
        }

        private fun match(raw_source: String, raw_match: String): Boolean {

            val source = Utils.normalizeStrings(raw_source, true, true, false)
            val match = Utils.normalizeStrings(raw_match, true, true, false)

            if (source.toLowerCase().contains(match)) { // If source contains the match, it matches
                return true
            }

            val source_char = source.toCharArray()
            val match_char = match.toCharArray()

            if (source_char[0] != match_char[0] || // If the start are not equals, the rest isn't too
                    match.length < Constants.MIN_MATCHNAME) { // If the source is too small, should not be
                return false
            }

            var inc_match = false
            var match_idx = 0
            var source_idx = 0
            var equality = 0
            while (source_idx < source_char.size) {
                if (source_idx < (match_char.size + (match_char.size - equality)) && inc_match) match_idx++
                if (source_char[source_idx] == match_char[match_idx]) {
                    equality++
                    inc_match = true
                } else
                    inc_match = false
                if (match_idx == match_char.size - 1) break
                source_idx++
            }

            return equality >= source_idx - Constants.ALLOWED_ERRORS
        }

    }
}