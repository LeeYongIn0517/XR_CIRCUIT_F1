import SwiftUI
import CircuitDomain

// Fixed fixture only: no API, cache or product UI is introduced in stage 2.
final class ConnectionRepository: CircuitRepository {
    func getCatalog() -> [CircuitSummary] {
        [CircuitSummary(id: "silverstone", displayName: "Silverstone",
                        modelAsset: "SilverstoneCircuit.glb", waypointsAsset: "silverstone_waypoints.json",
                        modelWidthMeters: 16.519634, modelHeightMeters: 0.497,
                        modelDepthMeters: 10.007112, legacyOverlayRotation: false,
                        jolpicaCircuitId: nil, openF1CircuitKey: nil, openF1CircuitShortName: nil)]
    }
    func getCircuitDetail(circuitId: String) -> CircuitDetail {
        CircuitDetail(id: circuitId, name: "Silverstone", country: "United Kingdom",
                      lengthKm: 5.891, turns: 18, drsZoneCount: 2,
                      location: GeoLocation(latitude: 52.0786, longitude: -1.0169, locality: nil),
                      characteristics: CircuitCharacteristics(elevationMinM: KotlinDouble(double: 148),
                          elevationMaxM: KotlinDouble(double: 172), overtakingDifficulty: nil,
                          tyreWearIndex: nil, fullThrottlePercent: nil),
                      lapRecord: nil, sectors: [], drsZones: [], corners: [], pitLane: nil)
    }
}

enum DomainConnection {
    static func load() -> CircuitDetail {
        let repository = ConnectionRepository()
        let entry = GetCircuitCatalogUseCase(circuitRepository: repository).invoke().first!
        return GetCircuitDetailUseCase(circuitRepository: repository).invoke(circuitId: entry.id)
    }
}

@main
struct CircuitConnectionApp: App {
    private let circuit = DomainConnection.load()
    var body: some Scene {
        WindowGroup {
            VStack(alignment: .leading, spacing: 16) {
                Text("KMP 도메인 연결 확인").font(.title2)
                Text(circuit.name).font(.headline)
                Text("길이: \(circuit.lengthKm, specifier: "%.3f") km")
                Text("고저차: \(circuit.elevationDeltaM?.doubleValue ?? 0, specifier: "%.0f") m")
                Text("공유 모델 및 UseCase 호출 성공")
                Text("연결 검증용 고정 데이터").font(.caption)
            }.padding()
        }
    }
}
