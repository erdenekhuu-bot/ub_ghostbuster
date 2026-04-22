package mn.erdenee.mn_ghostbuster.types

data class RegisterRequest(
    val username: String,
    val password: String,
    val phone: String
)
