package network.warzone.mars.match.models

data class PartyData(
    val name: String,
    val alias: String,
    val color: String,
    val min: Int,
    val max: Int
)