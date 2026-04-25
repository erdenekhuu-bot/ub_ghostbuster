package mn.erdenee.mn_ghostbuster.types

data class CaseBody(
    val id:Int,
    val title:String,
    val description:String,
    val image: MediaBody,
    val address: String
)

data class MediaBody(
    val id:Int,
    val file:String,
)
