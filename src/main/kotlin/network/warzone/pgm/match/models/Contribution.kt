package network.warzone.pgm.match.models

import java.util.*

data class Contribution(val playerId: UUID, val percentage: Float, val blockCount: Int)