package app.yongin.xr_circuit.data.di
import android.content.Context
import app.yongin.xr_circuit.data.platform.*
import app.yongin.xr_circuit.data.local.CircuitLocalDataSource
import app.yongin.xr_circuit.data.network.DataJson
import app.yongin.xr_circuit.data.remote.openmeteo.*
import app.yongin.xr_circuit.data.repository.*
import app.yongin.xr_circuit.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides @Singleton fun local(@ApplicationContext context: Context) = CircuitLocalDataSource(AndroidAssetReader(context), DataJson, if (app.yongin.xr_circuit.BuildConfig.FLAVOR == "mobile") app.yongin.xr_circuit.data.local.CircuitAssetTarget.MOBILE else app.yongin.xr_circuit.data.local.CircuitAssetTarget.XR)
    @Provides @Singleton fun circuits(local: CircuitLocalDataSource): CircuitRepository = CircuitRepositoryImpl(local)
    @Provides @Singleton fun weather(@ApplicationContext context: Context, local: CircuitLocalDataSource): WeatherRepository =
        WeatherRepositoryImpl(local, OpenMeteoRemoteDataSource(OpenMeteoApiService(androidHttpClient(context, (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0))))
    @Provides @Singleton fun grid(): StartingGridRepository = StartingGridRepositoryImpl()
}
