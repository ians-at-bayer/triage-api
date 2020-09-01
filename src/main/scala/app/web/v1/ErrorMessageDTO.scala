package app.web.v1

import io.swagger.annotations.ApiModel

@ApiModel("ErrorMessage")
case class ErrorMessageDTO(errorMessage: String)
