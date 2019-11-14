package fgapps.com.br.iassistant2.music

data class Music(var id: Long,
                 var name: String,
                 var title: String,
                 var artist: String,
                 var folder: String){

    override fun equals(other: Any?): Boolean {
        if(other is Music)
            return this.id == other.id
        return false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}