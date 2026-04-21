package mn.erdenee.mn_ghostbuster.types

import mn.erdenee.mn_ghostbuster.types.LocationResult

data class LocationResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<LocationResult>
)
