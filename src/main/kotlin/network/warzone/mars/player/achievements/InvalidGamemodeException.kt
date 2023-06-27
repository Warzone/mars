package network.warzone.mars.player.achievements

@Deprecated("This exception will either be removed or refined in the final product.")
class InvalidGamemodeException(gameModeString: String) : Exception("Invalid game mode: $gameModeString") {

}