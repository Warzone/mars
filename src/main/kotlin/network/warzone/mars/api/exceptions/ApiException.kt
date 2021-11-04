package network.warzone.mars.api.exceptions

import network.warzone.mars.api.http.ApiExceptionType

data class ApiException(val code: ApiExceptionType, override val message: String) : RuntimeException()