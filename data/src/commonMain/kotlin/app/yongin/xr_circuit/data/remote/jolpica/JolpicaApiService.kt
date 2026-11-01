package app.yongin.xr_circuit.data.remote.jolpica
import app.yongin.xr_circuit.data.network.*
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
class JolpicaApiService(private val client: HttpClient) {
    suspend fun getCircuit(
        circuitId: String
    ): JolpicaCircuitResponseDto = DataJson.decodeFromString(client.get(ApiBases.JOLPICA + "circuits/${circuitId.encodeURLPathPart()}.json") {

    }.bodyAsText())
}
