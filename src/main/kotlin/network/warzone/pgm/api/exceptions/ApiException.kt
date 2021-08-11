package network.warzone.pgm.api.exceptions

import network.warzone.pgm.api.http.ApiExceptionType

data class ApiException(val code: ApiExceptionType, override val message: String) : RuntimeException()