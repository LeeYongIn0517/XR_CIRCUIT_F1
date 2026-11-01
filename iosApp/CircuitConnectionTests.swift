import XCTest
import CircuitData
@testable import CircuitConnection
final class CircuitConnectionTests: XCTestCase {
    func testSharedRepositoryLoadsBundledDetail() throws {
        let connection = IosDataConnection()
        defer { connection.close() }
        let circuit = try connection.detail(circuitId: "silverstone")
        XCTAssertEqual(circuit.id, "silverstone")
        XCTAssertEqual(circuit.lengthKm, 5.891, accuracy: 0.0001)
        XCTAssertEqual(circuit.elevationDeltaM?.doubleValue, 24)
        XCTAssertEqual(circuit.turns, 18)
    }
}
