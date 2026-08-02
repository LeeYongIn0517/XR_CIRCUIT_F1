package app.yongin.xr_circuit.data.di

import app.yongin.xr_circuit.data.repository.CircuitRepositoryImpl
import app.yongin.xr_circuit.data.repository.StartingGridRepositoryImpl
import app.yongin.xr_circuit.data.repository.WeatherRepositoryImpl
import app.yongin.xr_circuit.domain.repository.CircuitRepository
import app.yongin.xr_circuit.domain.repository.StartingGridRepository
import app.yongin.xr_circuit.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCircuitRepository(
        impl: CircuitRepositoryImpl,
    ): CircuitRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        impl: WeatherRepositoryImpl,
    ): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindStartingGridRepository(
        impl: StartingGridRepositoryImpl,
    ): StartingGridRepository
}
