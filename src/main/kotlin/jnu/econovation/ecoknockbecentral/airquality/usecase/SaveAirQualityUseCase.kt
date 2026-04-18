package jnu.econovation.ecoknockbecentral.airquality.usecase

import jnu.econovation.ecoknockbecentral.airquality.command.SaveAirQualityCommand
import jnu.econovation.ecoknockbecentral.common.annotation.UseCase

@UseCase
interface SaveAirQualityUseCase {
    fun save(command: SaveAirQualityCommand)
}