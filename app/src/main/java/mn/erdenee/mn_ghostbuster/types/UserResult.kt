package mn.erdenee.mn_ghostbuster.types

import mn.erdenee.mn_ghostbuster.types.MemberDetail
data class UserResult(
    val id: Int,
    val username: String,
    val password: String,
    val member: MemberDetail?
)
