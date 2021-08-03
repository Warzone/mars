package network.warzone.pgm.api.exceptions

import network.warzone.pgm.api.ErrorCode

class ApiException(val code: ErrorCode, override val message: String) : RuntimeException()