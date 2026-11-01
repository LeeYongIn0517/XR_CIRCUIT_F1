import SwiftUI
import CircuitData

@main
struct CircuitConnectionApp: App {
    var body: some Scene { WindowGroup { CircuitScreen() } }
}

/// One owner per screen. All shared actions and callbacks run on the main actor.
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
    func corner(_ id: String) { connection?.selectCorner(id: id) }
    func retry() { connection?.retry() }
    func toggle(_ overlay: Overlay) { connection?.toggleOverlay(overlay: overlay) }
}

// Bundle decoding and rendering are platform responsibilities; selection stays in CircuitStore.
struct TrackPathData: Decodable, Sendable {
    struct Point: Decodable, Sendable { let x: Float; let y: Float; let progress: Float }
    struct Corner: Decodable, Sendable { let id: String; let x: Float; let y: Float }
    let width: Int
    let height: Int
    let points: [Point]
    let corners: [Corner]
    func sample(_ progress: Float) -> TrackPoint2D {
        let i = points.lastIndex { $0.progress <= progress } ?? 0
        let a = points[i], b = points[(i + 1) % points.count]
        let end: Float = i == points.count - 1 ? 1 : b.progress
        let t = min(1, max(0, (progress - a.progress) / (end - a.progress)))
        return TrackPoint2D(x: a.x + (b.x-a.x)*t, y: a.y + (b.y-a.y)*t)
    }
}
struct TrackImages {
    let path: TrackPathData
    let base: UIImage
    let sectors: UIImage
    let drs: UIImage
    static func load(_ id: String) throws -> TrackImages {
        func url(_ name: String) throws -> URL {
            guard let url = Bundle.main.url(forResource: name, withExtension: nil, subdirectory: "tracks/\(id)") else {
                throw CocoaError(.fileNoSuchFile)
            }
            return url
        }
        func image(_ name: String) throws -> UIImage {
            guard let image = UIImage(contentsOfFile: try url(name).path) else { throw CocoaError(.fileReadCorruptFile) }
            return image
        }
        let path = try JSONDecoder().decode(TrackPathData.self, from: Data(contentsOf: url("path.json")))
        guard path.width > 0, path.height > 0, path.points.count > 1,
              path.points.allSatisfy({ $0.x.isFinite && $0.y.isFinite && $0.progress.isFinite }),
              zip(path.points, path.points.dropFirst()).allSatisfy({ $0.progress < $1.progress }) else {
            throw CocoaError(.fileReadCorruptFile)
        }
        return try TrackImages(path: path, base: image("track.png"), sectors: image("sectors.png"), drs: image("drs.png"))
    }
}

struct CircuitScreen: View {
    @StateObject private var model = CircuitScreenModel()
    @State private var marker = true
    @State private var assetRetry = 0
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    Text("CIRCUIT / 2D").font(.caption.weight(.semibold)).foregroundStyle(.secondary)
                    if let state = model.state {
                        Menu {
                            ForEach(state.catalog, id: \.id) { entry in
                                Button(entry.displayName) { model.select(entry.id) }
                            }
                        } label: {
                            HStack { Text(state.selectedCircuit?.displayName ?? "Select circuit"); Spacer(); Image(systemName: "chevron.down") }
                                .frame(minHeight: 48)
                        }.accessibilityLabel("Select circuit").accessibilityValue(state.selectedCircuit?.displayName ?? "None")
                        if state.catalogError != nil || state.profileError != nil || state.selectionError != nil || state.catalog.isEmpty {
                            Text("Circuit information unavailable")
                            Button("Retry circuit") { model.retry() }.frame(minHeight: 48)
                        }
                        if let selected = state.selectedCircuit {
                            VStack(alignment: .leading) {
                                Toggle("Sectors", isOn: overlay(.sectors))
                                Toggle("DRS", isOn: overlay(.drs))
                                Toggle("Marker", isOn: $marker)
                            }.toggleStyle(.switch)
                            TrackView(circuitID: selected.id, state: state, marker: marker, retryKey: assetRetry,
                                      retry: { assetRetry += 1; model.retry() }, select: { code in
                                if let corner = state.detail?.corners.first(where: { $0.turnCode == code }) { model.corner(corner.id) }
                            }).id(selected.id)
                            Text("45-second visual lap · bundled layout").font(.caption).foregroundStyle(.secondary)
                        }
                        Text("Circuit Profile").font(.title2.bold()).accessibilityAddTraits(.isHeader)
                        if let detail = state.detail {
                            info("Length", String(format: "%.3f km", detail.lengthKm))
                            info("Turns", "\(detail.turns)")
                            info("DRS Zones", "\(detail.drsZoneCount)")
                            if let elevation = detail.elevationDeltaM { info("Elevation", String(format: "%.0f m", elevation.doubleValue)) }
                            if let lap = detail.lapRecord {
                                VStack(alignment: .leading, spacing: 6) {
                                    Text("Lap Record").font(.headline)
                                    Text(lap.time).font(.title2.monospacedDigit())
                                    Text("\(lap.driver) · \(lap.team) · \(String(lap.year))")
                                }.frame(maxWidth: .infinity, alignment: .leading).padding().background(.quaternary, in: RoundedRectangle(cornerRadius: 12))
                            }
                        } else if state.profileError == nil { ProgressView("Loading circuit profile…") }
                        Text("Track Conditions").font(.title2.bold()).accessibilityAddTraits(.isHeader)
                        if state.weatherLoading { ProgressView("Loading weather…") }
                        else if state.weatherError != nil {
                            Text("Weather unavailable")
                            Button("Retry weather") { model.retry() }.frame(minHeight: 48)
                        } else if let weather = state.weather {
                            VStack(alignment: .leading, spacing: 8) {
                                Text(String(format: "%.0f°C", weather.temperatureC)).font(.largeTitle)
                                Text(String(format: "Precip: %.1f mm", weather.precipitationMm))
                                Text("\(weather.precipitationProbabilityPercent?.intValue ?? 0)% Rain")
                                Text("Wind: \(Int(weather.windSpeedKmh.rounded()))km/h \(compass(weather.windDirectionDeg))")
                            }.frame(maxWidth: .infinity, alignment: .leading).padding().background(.quaternary, in: RoundedRectangle(cornerRadius: 12))
                        }
                        Text("Key Corners").font(.title2.bold()).accessibilityAddTraits(.isHeader)
                        ForEach(state.detail?.corners ?? [], id: \.id) { corner in
                            Button { model.corner(corner.id) } label: {
                                HStack { Text("\(corner.turnCode) · \(corner.name)"); Spacer(); if corner.id == state.selectedCornerId { Image(systemName: "checkmark.circle.fill") } }
                                    .frame(minHeight: 48).contentShape(Rectangle())
                            }.accessibilityAddTraits(corner.id == state.selectedCornerId ? .isSelected : [])
                        }
                    } else { ProgressView("Loading circuits…") }
                }.padding(16)
            }.navigationTitle("Circuit").navigationBarTitleDisplayMode(.inline)
        }.onAppear { model.start() }.onDisappear { model.stop() }
    }
    private func overlay(_ value: Overlay) -> Binding<Bool> {
        Binding(get: { model.state?.overlays.contains(value) ?? false }, set: { _ in model.toggle(value) })
    }
    private func info(_ name: String, _ value: String) -> some View {
        VStack(alignment: .leading, spacing: 4) { Text(name).font(.caption).foregroundStyle(.secondary); Text(value).font(.title3) }
            .frame(maxWidth: .infinity, alignment: .leading).padding().background(.quaternary, in: RoundedRectangle(cornerRadius: 12))
    }
    private func compass(_ degrees: Double) -> String {
        let directions = ["N", "NE", "E", "SE", "S", "SW", "W", "NW"]
        return directions[((Int((degrees / 45).rounded()) % 8) + 8) % 8]
    }
}

struct TrackView: View {
    let circuitID: String
    let state: CircuitState
    let marker: Bool
    let retryKey: Int
    let retry: () -> Void
    let select: (String) -> Void
    @Environment(\.scenePhase) private var scenePhase
    @State private var images: TrackImages?
    @State private var failed = false
    @State private var epoch = Date()
    var body: some View {
        Group {
            if let images {
                GeometryReader { geometry in
                    let v = TrackViewport(imageWidth: Float(images.path.width), imageHeight: Float(images.path.height), width: Float(geometry.size.width), height: Float(geometry.size.height))
                    TimelineView(.animation(minimumInterval: 1.0/30, paused: !marker || scenePhase != .active)) { timeline in
                        Canvas { context, _ in
                            let rect = CGRect(x: CGFloat(v.left), y: CGFloat(v.top), width: CGFloat(v.contentWidth), height: CGFloat(v.contentHeight))
                            context.draw(Image(uiImage: images.base), in: rect)
                            if state.overlays.contains(Overlay.sectors) { context.draw(Image(uiImage: images.sectors), in: rect) }
                            if state.overlays.contains(Overlay.drs) { context.draw(Image(uiImage: images.drs), in: rect) }
                            if let code = state.detail?.corners.first(where: { $0.id == state.selectedCornerId })?.turnCode,
                               let corner = images.path.corners.first(where: { $0.id == code }) {
                                dot(&context, v.project(point: TrackPoint2D(x: corner.x, y: corner.y)), radius: 8, color: .cyan)
                            }
                            if marker {
                                let progress = Float(max(0, timeline.date.timeIntervalSince(epoch)).truncatingRemainder(dividingBy: 45) / 45)
                                dot(&context, v.project(point: images.path.sample(progress)), radius: 5, color: .white)
                            }
                        }.background(Color(red: 0.063, green: 0.106, blue: 0.153))
                    }.gesture(SpatialTapGesture().onEnded { event in
                        let x = Float(event.location.x), y = Float(event.location.y)
                        guard v.unproject(x: x, y: y, padding: 0.08) != nil else { return }
                        if let nearest = images.path.corners.min(by: { distance($0, v, x, y) < distance($1, v, x, y) }),
                           distance(nearest, v, x, y) <= 24 { select(nearest.id) }
                    })
                }.frame(height: 300)
                    .accessibilityElement(children: .ignore)
                    .accessibilityLabel("\(state.selectedCircuit?.displayName ?? circuitID) 2D track")
                    .accessibilityValue("Selected corner \(state.detail?.corners.first(where: { $0.id == state.selectedCornerId })?.turnCode ?? "none")")
                    .accessibilityHint("Use Key Corners buttons below to select a corner")
            } else if failed { Text("Track image unavailable"); Button("Retry track", action: retry).frame(minHeight: 48) }
            else { ProgressView("Loading track…").frame(height: 300) }
        }.task(id: retryKey) {
            images = nil; failed = false
            let id = circuitID
            let task = Task.detached(priority: .userInitiated) { try TrackImages.load(id) }
            defer { task.cancel() }
            do {
                let result = try await withTaskCancellationHandler(operation: { try await task.value }, onCancel: { task.cancel() })
                try Task.checkCancellation()
                images = result; epoch = Date()
            } catch is CancellationError { } catch { if !Task.isCancelled { failed = true } }
        }.onChange(of: scenePhase) { _, phase in if phase == .active { epoch = Date() } }
            .onChange(of: marker) { _, _ in epoch = Date() }
    }
    private func dot(_ context: inout GraphicsContext, _ p: TrackPoint2D, radius: CGFloat, color: Color) {
        context.fill(Path(ellipseIn: CGRect(x: CGFloat(p.x)-radius, y: CGFloat(p.y)-radius, width: radius*2, height: radius*2)), with: .color(color))
    }
    private func distance(_ p: TrackPathData.Corner, _ v: TrackViewport, _ x: Float, _ y: Float) -> Float {
        let projected = v.project(point: TrackPoint2D(x: p.x, y: p.y))
        return hypot(projected.x-x, projected.y-y)
    }
}
