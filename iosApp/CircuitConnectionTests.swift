import XCTest
import CircuitDomain
@testable import CircuitConnection

final class CircuitConnectionTests: XCTestCase {
    func testSwiftCallsSharedUseCasesAndComputedModelProperty() {
        let circuit = DomainConnection.load()
        XCTAssertEqual(circuit.id, "silverstone")
        XCTAssertEqual(circuit.lengthKm, 5.891, accuracy: 0.0001)
        XCTAssertEqual(circuit.elevationDeltaM?.doubleValue, 24)
        XCTAssertEqual(circuit.turns, 18)
    }
}
