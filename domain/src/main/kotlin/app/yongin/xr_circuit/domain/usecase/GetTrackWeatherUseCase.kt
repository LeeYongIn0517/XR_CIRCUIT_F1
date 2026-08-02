package app.yongin.xr_circuit.domain.usecase

import app.yongin.xr_circuit.domain.model.TrackWeather
import app.yongin.xr_circuit.domain.repository.WeatherRepository
import javax.inject.Inject

class GetTrackWeatherUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
) {
    suspend operator fun invoke(circuitId: String): TrackWeather {
        return weatherRepository.getTrackWeather(circuitId)
    }
}
