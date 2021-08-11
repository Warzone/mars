package network.warzone.pgm.api.http

data class ApiExceptionResponse (val code: ApiExceptionType, val message: String, val error: Boolean = true)