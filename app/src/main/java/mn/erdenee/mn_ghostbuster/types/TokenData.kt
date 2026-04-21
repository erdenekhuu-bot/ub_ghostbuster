package mn.erdenee.mn_ghostbuster.types


data class LoginResponse(
    val tokens: TokenData? = null
)
data class TokenData(
    val access: String,
    val refresh: String
)
