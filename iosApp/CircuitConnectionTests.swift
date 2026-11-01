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
    func testAllTracksUseTheSameBundleAssetsAndViewport() throws {
        let connection = IosDataConnection()
        defer { connection.close() }
        let manifest = try JSONSerialization.jsonObject(with: Data(contentsOf: Bundle.main.url(forResource: "index", withExtension: "json", subdirectory: "tracks")!)) as! [String: Any]
        let entries = manifest["circuits"] as! [[String: Any]]
        XCTAssertEqual(entries.count, 23)
        for entry in entries {
            let id = entry["circuitId"] as! String
            let detail = try connection.detail(circuitId: id)
            let images = try TrackImages.load(id)
            XCTAssertEqual(images.path.points.count, 512)
            XCTAssertEqual(images.path.corners.count, detail.corners.count)
            XCTAssertEqual(Int(images.base.size.width), images.path.width)
            XCTAssertEqual(images.base.size, images.sectors.size)
            XCTAssertEqual(images.base.size, images.drs.size)
            let v = TrackViewport(imageWidth: Float(images.path.width), imageHeight: Float(images.path.height), width: 600, height: 300)
            XCTAssertNil(v.unproject(x: 20, y: 100, padding: 0.08))
            let first = images.path.sample(0)
            XCTAssertEqual(first.x, images.path.points[0].x, accuracy: 0.0001)
            let nearEnd = images.path.sample(0.99999)
            XCTAssertEqual(nearEnd.x, first.x, accuracy: 0.002)
            XCTAssertEqual(nearEnd.y, first.y, accuracy: 0.002)
        }
    }
    @MainActor
    func testScreenOwnerReleasesObservationAndRestartsWithoutDuplicates() async throws {
        var model: CircuitScreenModel? = CircuitScreenModel()
        weak var weakModel = model
        model?.start()
        model?.start()
        try await Task.sleep(nanoseconds: 100_000_000)
        XCTAssertEqual(model?.state?.catalog.count, 23)
        model?.toggle(.drs)
        model?.select("suzuka")
        try await Task.sleep(nanoseconds: 100_000_000)
        XCTAssertEqual(model?.state?.selectedCircuit?.id, "suzuka")
        XCTAssertTrue(model?.state?.overlays.contains(Overlay.drs) == true)
        model?.corner("t1")
        try await Task.sleep(nanoseconds: 100_000_000)
        XCTAssertEqual(model?.state?.selectedCornerId, "t1")
        model?.stop()
        let stopped = model?.state
        model?.select("silverstone")
        try await Task.sleep(nanoseconds: 100_000_000)
        XCTAssertTrue(model?.state === stopped)
        model?.start()
        try await Task.sleep(nanoseconds: 100_000_000)
        XCTAssertEqual(model?.state?.selectedCircuit?.id, "silverstone")
        model?.stop()
        model = nil
        XCTAssertNil(weakModel)
    }
}
