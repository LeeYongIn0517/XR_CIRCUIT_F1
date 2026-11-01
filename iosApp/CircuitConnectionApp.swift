import SwiftUI
import CircuitData

@main
struct CircuitConnectionApp: App {
    var body: some Scene { WindowGroup { DataConnectionView() } }
}

@MainActor
final class CircuitScreenModel: ObservableObject {
    @Published var state: CircuitState?
    private var connection: IosCircuitConnection?
    private var observation: CircuitObservation?
    func start() {
        guard connection == nil else { return }
        let owner = IosCircuitConnection()
        connection = owner
        observation = owner.observe { [weak self] state in self?.state = state }
    }
    func stop() {
        observation?.cancel()
        observation = nil
        connection?.close()
        connection = nil
    }
    func select(_ id: String) { connection?.selectCircuit(id: id) }
    func retry() { connection?.retry() }
    func toggle() { connection?.toggleOverlay(overlay: .drs) }
}

struct DataConnectionView: View {
    @StateObject private var model = CircuitScreenModel()
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("KMP 공통 상태 검증").font(.title2)
            HStack {
                Button("Silverstone") { model.select("silverstone") }
                Button("Suzuka") { model.select("suzuka") }
            }
            if let state = model.state {
                if let detail = state.detail { Text("\(detail.name) · \(detail.lengthKm) km · \(detail.turns) 코너") }
                if state.weatherLoading { ProgressView() }
                if let weather = state.weather { Text("기온 \(weather.temperatureC) °C · 풍속 \(weather.windSpeedKmh) km/h") }
                if state.weatherError != nil || state.profileError != nil || state.catalogError != nil { Text("데이터를 불러오지 못했습니다") }
                Text("DRS: \(state.overlays.contains(Overlay.drs) ? "ON" : "OFF")")
            }
            HStack { Button("DRS") { model.toggle() }; Button("재시도") { model.retry() } }
            Text("공유 Store · Main 스레드 관찰 · 화면 종료 시 취소").font(.caption)
        }.padding().onAppear { model.start() }.onDisappear { model.stop() }
    }
}
