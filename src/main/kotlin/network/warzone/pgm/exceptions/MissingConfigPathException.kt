package network.warzone.pgm.exceptions

class MissingConfigPathException(path: String) : Exception("Missing path: $path")