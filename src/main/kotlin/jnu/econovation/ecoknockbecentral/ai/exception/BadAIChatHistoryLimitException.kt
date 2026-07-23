package jnu.econovation.ecoknockbecentral.ai.exception

import jnu.econovation.ecoknockbecentral.common.exception.client.ClientException
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode

class BadAIChatHistoryLimitException : ClientException(ErrorCode.BAD_AI_CHAT_HISTORY_LIMIT)
