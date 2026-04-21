package mn.erdenee.mn_ghostbuster.types

import mn.erdenee.mn_ghostbuster.types.UserResult

data class MemberResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<UserResult>
)
