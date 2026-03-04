package com.sionic.ai.util

open class ApiException(message: String) : RuntimeException(message)

class BadRequestException(message: String) : ApiException(message)

class UnauthorizedException(message: String) : ApiException(message)
