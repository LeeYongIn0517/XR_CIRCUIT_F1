import SwiftUI
import CircuitData

@main
struct CircuitConnectionApp: App {
    var body: some Scene { WindowGroup { DataConnectionView() } }
}

struct DataConnectionView: View {
    @State private var detail = "로컬 데이터 로딩 중"
    @State private var weather = "날씨 요청 중"
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("KMP 데이터 호출 검증").font(.title2)
            Text(detail)
            Text(weather)
            Text("공유 Repository · 실제 bundle JSON · Open-Meteo").font(.caption)
        }.padding().task {
            let connection = IosDataConnection()
            defer { connection.close() }
            do {
                let circuit = try connection.detail(circuitId: "silverstone")
                detail = "\(circuit.name)\n\(circuit.lengthKm) km · \(circuit.turns) 코너\n고저차 \(circuit.elevationDeltaM?.doubleValue ?? 0) m"
                let result = try await connection.weather(circuitId: "silverstone")
                weather = "기온 \(result.temperatureC) °C\n풍속 \(result.windSpeedKmh) km/h\n관측 \(result.observedAtIso ?? "—")"
            } catch { weather = "데이터 오류: \(error.localizedDescription)" }
        }
    }
}
