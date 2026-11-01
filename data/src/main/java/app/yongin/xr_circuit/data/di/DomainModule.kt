package app.yongin.xr_circuit.data.di

import app.yongin.xr_circuit.domain.repository.*
import app.yongin.xr_circuit.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Android owns dependency injection; shared domain remains platform independent. */
@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides fun catalog(repository: CircuitRepository) = GetCircuitCatalogUseCase(repository)
    @Provides fun detail(repository: CircuitRepository) = GetCircuitDetailUseCase(repository)
    @Provides fun weather(repository: WeatherRepository) = GetTrackWeatherUseCase(repository)
    @Provides fun grid(repository: StartingGridRepository) = GetStartingGridUseCase(repository)
}
