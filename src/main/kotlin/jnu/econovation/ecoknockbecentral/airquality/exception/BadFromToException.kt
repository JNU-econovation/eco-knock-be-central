package jnu.econovation.ecoknockbecentral.airquality.exception

import jnu.econovation.ecoknockbecentral.common.exception.client.ClientException
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode

class BadFromToException : ClientException(ErrorCode.BAD_FROM_TO)