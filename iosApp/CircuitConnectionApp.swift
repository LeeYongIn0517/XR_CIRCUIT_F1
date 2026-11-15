import SwiftUI
import CircuitData

@main
struct CircuitConnectionApp: App {
    var body: some Scene { WindowGroup { SplashHost() } }
}

private struct SplashHost: View {
    @State private var showingSplash = true

    var body: some View {
        ZStack {
            CircuitScreen()
                .accessibilityHidden(showingSplash)
                .allowsHitTesting(!showingSplash)
            if showingSplash {
                GeometryReader { geometry in
                    ZStack {
                        Image("SplashBackground")
                            .resizable()
                            .frame(width: geometry.size.width, height: geometry.size.height)
                        Image("SplashBrandMark")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 240, height: 240)
                    }
                    .frame(width: geometry.size.width, height: geometry.size.height)
                }
                .ignoresSafeArea()
                .accessibilityHidden(true)
            }
        }
        .task {
            guard showingSplash else { return }
            do { try await Task.sleep(for: .milliseconds(700)) }
            catch { return }
            showingSplash = false
        }
    }
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

private enum CircuitDetailTab: String, CaseIterable {
    case profile = "Circuit Profile"
    case conditions = "Track Conditions"
    case corners = "Key Corners"
}

// Mirrors presentation/theme and the Material 3 cards used by CircuitMobileScreen.
private enum CircuitStyle {
    private static func color(_ light: UInt32, _ dark: UInt32) -> Color {
        func uiColor(_ hex: UInt32) -> UIColor {
            UIColor(red: CGFloat((hex >> 16) & 255) / 255,
                    green: CGFloat((hex >> 8) & 255) / 255,
                    blue: CGFloat(hex & 255) / 255, alpha: 1)
        }
        return Color(uiColor: UIColor { $0.userInterfaceStyle == .dark ? uiColor(dark) : uiColor(light) })
    }
    static let primary = color(0x2563EB, 0xADC6FF)
    static let onPrimary = color(0xFFFFFF, 0x001A41)
    static let primaryContainer = color(0xEFF6FF, 0x002E69)
    static let onPrimaryContainer = color(0x1D4ED8, 0xD8E2FF)
    static let secondaryContainer = color(0xBFDBFE, 0x002E69)
    static let surface = color(0xFFFFFF, 0x1A1C1E)
    static let onSurface = color(0x0F172A, 0xE2E2E6)
    static let muted = color(0x64748B, 0x8C9198)
    static let outline = color(0x94A3B8, 0x8C9198)
    static let border = color(0xE2E8F0, 0x42474E)

    static func font(_ size: CGFloat, weight: Font.Weight = .regular,
                     design: Font.Design = .default, relativeTo style: Font.TextStyle = .body) -> Font {
        let textStyle: UIFont.TextStyle
        switch style {
        case .caption2: textStyle = .caption2
        case .caption: textStyle = .caption1
        case .title2: textStyle = .title2
        case .title: textStyle = .title1
        default: textStyle = .body
        }
        let uiWeight: UIFont.Weight = weight == .bold ? .bold : weight == .medium ? .medium : .regular
        let base = design == .monospaced
            ? UIFont.monospacedSystemFont(ofSize: size, weight: uiWeight)
            : UIFont.systemFont(ofSize: size, weight: uiWeight)
        return Font(UIFontMetrics(forTextStyle: textStyle).scaledFont(for: base))
    }
}

private struct CircuitButtonStyle: ButtonStyle {
    var filled = false
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(CircuitStyle.font(14, weight: .medium))
            .multilineTextAlignment(.center)
            .padding(.horizontal, 24)
            .padding(.vertical, 10)
            .frame(minHeight: 48)
            .foregroundStyle(filled ? CircuitStyle.onPrimary : CircuitStyle.primary)
            .background(filled ? CircuitStyle.primary : Color.clear, in: Capsule())
            .overlay(Capsule().strokeBorder(filled ? Color.clear : CircuitStyle.outline, lineWidth: 1))
            .opacity(configuration.isPressed ? 0.7 : 1)
    }
}

private struct CircuitChip: View {
    let title: String
    @Binding var selected: Bool
    var body: some View {
        Button { selected.toggle() } label: {
            Text(title)
                .font(CircuitStyle.font(14, weight: .medium))
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .foregroundStyle(selected ? CircuitStyle.onPrimaryContainer : CircuitStyle.muted)
                .background(selected ? CircuitStyle.secondaryContainer : Color.clear,
                            in: RoundedRectangle(cornerRadius: 8))
                .overlay(RoundedRectangle(cornerRadius: 8)
                    .strokeBorder(selected ? Color.clear : CircuitStyle.outline, lineWidth: 1))
                .frame(minHeight: 48)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(title)
        .accessibilityValue(selected ? "On" : "Off")
        .accessibilityAddTraits(selected ? .isSelected : [])
    }
}

// Wrap chips like Android FlowRow, including when accessibility text grows.
private struct CircuitChipLayout: Layout {
    private func positions(_ subviews: Subviews, width: CGFloat) -> (points: [CGPoint], size: CGSize) {
        var points: [CGPoint] = []
        var x: CGFloat = 0
        var y: CGFloat = 0
        var rowHeight: CGFloat = 0
        var usedWidth: CGFloat = 0
        for view in subviews {
            let size = view.sizeThatFits(.unspecified)
            if x > 0 && x + size.width > width {
                x = 0
                y += rowHeight
                rowHeight = 0
            }
            points.append(CGPoint(x: x, y: y))
            usedWidth = max(usedWidth, x + size.width)
            x += size.width + 8
            rowHeight = max(rowHeight, size.height)
        }
        return (points, CGSize(width: usedWidth, height: y + rowHeight))
    }
    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        positions(subviews, width: proposal.width ?? .infinity).size
    }
    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let layout = positions(subviews, width: bounds.width)
        for (index, view) in subviews.enumerated() {
            view.place(at: CGPoint(x: bounds.minX + layout.points[index].x,
                                   y: bounds.minY + layout.points[index].y),
                       proposal: ProposedViewSize(view.sizeThatFits(.unspecified)))
        }
    }
}

private struct CircuitStatCard: View {
    let name: String
    let value: String
    var unit: String? = nil
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(name.uppercased())
                .font(CircuitStyle.font(10, relativeTo: .caption2)).tracking(0.5)
                .foregroundStyle(CircuitStyle.muted)
                .frame(minHeight: 15)
            HStack(alignment: .firstTextBaseline, spacing: 4) {
                Text(value)
                    .font(CircuitStyle.font(24, weight: .bold, design: .monospaced, relativeTo: .title2))
                if let unit {
                    Text(unit).font(CircuitStyle.font(12, weight: .bold, design: .monospaced, relativeTo: .caption))
                }
            }
            .tracking(1.2)
            .foregroundStyle(CircuitStyle.primary)
            .frame(minHeight: 32, alignment: .bottomLeading)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(12)
        .background(CircuitStyle.surface.opacity(0.5), in: RoundedRectangle(cornerRadius: 4))
        .overlay(RoundedRectangle(cornerRadius: 4).strokeBorder(CircuitStyle.border, lineWidth: 1))
    }
}

private struct CircuitLapRecordCard: View {
    let time: String
    let driver: String
    let team: String
    let year: Int32
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack {
                Text("Lap Record").foregroundStyle(CircuitStyle.primary)
                Spacer()
                Text(String(year)).foregroundStyle(CircuitStyle.outline)
            }.font(CircuitStyle.font(10, relativeTo: .caption2)).tracking(0.5)
                .frame(minHeight: 15)
            Text(time)
                .font(CircuitStyle.font(28, weight: .medium, design: .monospaced, relativeTo: .title))
                .foregroundStyle(CircuitStyle.onPrimaryContainer)
                .padding(.top, 4)
                .frame(minHeight: 42, alignment: .leading)
            Text("\(driver) (\(team))")
                .font(CircuitStyle.font(14)).tracking(0.25)
                .foregroundStyle(CircuitStyle.muted)
                .frame(minHeight: 20)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(CircuitStyle.primaryContainer, in: RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).strokeBorder(CircuitStyle.secondaryContainer, lineWidth: 1))
    }
}

struct CircuitScreen: View {
    @StateObject private var model = CircuitScreenModel()
    @State private var selectedTab: CircuitDetailTab = .profile
    @State private var marker = true
    @State private var assetRetry = 0
    var body: some View {
        GeometryReader { geometry in
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    Text("CIRCUIT / 2D").font(CircuitStyle.font(14, weight: .medium))
                    if let state = model.state {
                        Menu {
                            ForEach(state.catalog, id: \.id) { entry in
                                Button(entry.displayName) { model.select(entry.id) }
                            }
                        } label: {
                            Text(state.selectedCircuit?.displayName ?? "Select circuit")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(CircuitButtonStyle())
                        .accessibilityLabel("Select circuit")
                        .accessibilityValue(state.selectedCircuit?.displayName ?? "None")
                        if state.catalogError != nil || state.profileError != nil || state.selectionError != nil || state.catalog.isEmpty {
                            Text("Circuit information unavailable")
                            Button("Retry circuit") { model.retry() }.buttonStyle(CircuitButtonStyle(filled: true))
                        }
                        if let selected = state.selectedCircuit {
                            CircuitChipLayout {
                                CircuitChip(title: "Sectors", selected: overlay(.sectors))
                                CircuitChip(title: "DRS", selected: overlay(.drs))
                                CircuitChip(title: "Marker", selected: $marker)
                            }.frame(maxWidth: .infinity, alignment: .leading)
                            TrackView(circuitID: selected.id, state: state, marker: marker, retryKey: assetRetry,
                                      height: geometry.size.width > geometry.size.height ? 220 : min(max(0, geometry.size.width - 32), 380),
                                      retry: { assetRetry += 1; model.retry() }, select: { code in
                                if let corner = state.detail?.corners.first(where: { $0.turnCode == code }) { model.corner(corner.id) }
                            }).id(selected.id)
                            Text("45-second visual lap · bundled layout")
                                .font(CircuitStyle.font(12, relativeTo: .caption)).tracking(0.4)
                        }
                        detailTabs
                        switch selectedTab {
                        case .profile:
                            if let detail = state.detail {
                                CircuitStatCard(name: "Length", value: String(format: "%.3f", detail.lengthKm), unit: "km")
                                CircuitStatCard(name: "Turns", value: "\(detail.turns)")
                                CircuitStatCard(name: "DRS Zones", value: "\(detail.drsZoneCount)")
                                if let elevation = detail.elevationDeltaM {
                                    CircuitStatCard(name: "Elevation", value: String(format: "%.0f", elevation.doubleValue), unit: "m")
                                }
                                if let lap = detail.lapRecord {
                                    CircuitLapRecordCard(time: lap.time, driver: lap.driver, team: lap.team, year: lap.year)
                                }
                            } else if state.profileError == nil { ProgressView().accessibilityLabel("Loading circuit profile") }
                        case .conditions:
                            if state.weatherLoading {
                                Text("Loading weather…")
                                ProgressView().frame(maxWidth: .infinity)
                            } else if state.weatherError != nil {
                                Text("Weather unavailable")
                                Button("Retry weather") { model.retry() }.buttonStyle(CircuitButtonStyle(filled: true))
                            } else if let weather = state.weather {
                                VStack(alignment: .leading, spacing: 8) {
                                    Text(String(format: "%.0f°C", weather.temperatureC))
                                        .font(CircuitStyle.font(28, weight: .medium, relativeTo: .title))
                                    Text(String(format: "Precip: %.1f mm", weather.precipitationMm))
                                    Text("\(weather.precipitationProbabilityPercent?.intValue ?? 0)% Rain")
                                    Text("Wind: \(Int(weather.windSpeedKmh.rounded()))km/h \(compass(weather.windDirectionDeg))")
                                }
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(16)
                                .background(CircuitStyle.surface, in: RoundedRectangle(cornerRadius: 12))
                                .shadow(color: .black.opacity(0.12), radius: 2, y: 1)
                            }
                        case .corners:
                            ForEach(state.detail?.corners ?? [], id: \.id) { corner in
                                Button { model.corner(corner.id) } label: {
                                    Text("\(corner.id == state.selectedCornerId ? "● " : "")\(corner.turnCode) · \(corner.name)")
                                        .frame(maxWidth: .infinity)
                                }
                                .buttonStyle(CircuitButtonStyle())
                                .accessibilityAddTraits(corner.id == state.selectedCornerId ? .isSelected : [])
                            }
                        }
                    } else { ProgressView("Loading circuits…") }
                }.padding(16)
            }
            .background(CircuitStyle.surface.ignoresSafeArea())
        }
        .font(CircuitStyle.font(16))
        .foregroundStyle(CircuitStyle.onSurface)
        .tint(CircuitStyle.primary)
        .onAppear { model.start() }.onDisappear { model.stop() }
    }
    private var detailTabs: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 0) {
                ForEach(CircuitDetailTab.allCases, id: \.self) { tab in
                    Button { selectedTab = tab } label: {
                        Text(tab.rawValue)
                            .font(CircuitStyle.font(14, weight: .medium))
                            .fixedSize(horizontal: true, vertical: false)
                            .padding(.vertical, 12)
                            .frame(minHeight: 48)
                            .foregroundStyle(selectedTab == tab ? CircuitStyle.primary : CircuitStyle.muted)
                            .overlay(alignment: .bottom) {
                                if selectedTab == tab {
                                    UnevenRoundedRectangle(topLeadingRadius: 3, topTrailingRadius: 3)
                                        .fill(CircuitStyle.primary).frame(height: 3)
                                }
                            }
                            .padding(.horizontal, 16)
                            .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                    .accessibilityAddTraits(selectedTab == tab ? .isSelected : [])
                }
            }
        }
        .background(alignment: .bottom) { CircuitStyle.border.frame(height: 1) }
        .accessibilityElement(children: .contain)
        .accessibilityLabel("Circuit details tabs")
    }
    private func overlay(_ value: Overlay) -> Binding<Bool> {
        Binding(get: { model.state?.overlays.contains(value) ?? false }, set: { _ in model.toggle(value) })
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
    let height: CGFloat
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
                                cornerMarker(&context, v.project(point: TrackPoint2D(x: corner.x, y: corner.y)))
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
                }.frame(height: height)
                    .accessibilityElement(children: .ignore)
                    .accessibilityLabel("\(state.selectedCircuit?.displayName ?? circuitID) 2D track")
                    .accessibilityValue("Selected corner \(state.detail?.corners.first(where: { $0.id == state.selectedCornerId })?.turnCode ?? "none")")
                    .accessibilityHint("Select the Key Corners tab below to choose a corner")
            } else if failed { Text("Track image unavailable"); Button("Retry track", action: retry).buttonStyle(CircuitButtonStyle(filled: true)) }
            else { ProgressView("Loading track…").frame(height: height) }
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
    private func cornerMarker(_ context: inout GraphicsContext, _ point: TrackPoint2D) {
        let center = CGPoint(x: CGFloat(point.x), y: CGFloat(point.y))
        let accent = Color(red: 1, green: 173.0 / 255, blue: 102.0 / 255)
        let radius: CGFloat = 10
        let glowRadius: CGFloat = 20
        let glow = Path(ellipseIn: CGRect(x: center.x - glowRadius, y: center.y - glowRadius,
                                          width: glowRadius * 2, height: glowRadius * 2))
        context.fill(glow, with: .radialGradient(Gradient(stops: [
            .init(color: .clear, location: 0),
            .init(color: accent.opacity(0.08), location: 0.4),
            .init(color: accent.opacity(0.28), location: 0.5),
            .init(color: .clear, location: 1)
        ]), center: center, startRadius: 0, endRadius: glowRadius))
        // Keep the track visible through the center and emphasize the boundary.
        let ring = Path(ellipseIn: CGRect(x: center.x - radius, y: center.y - radius,
                                          width: radius * 2, height: radius * 2))
        context.fill(ring, with: .color(accent.opacity(0.14)))
        context.stroke(ring, with: .color(accent.opacity(0.95)), lineWidth: 2)
    }
    private func dot(_ context: inout GraphicsContext, _ p: TrackPoint2D, radius: CGFloat, color: Color) {
        context.fill(Path(ellipseIn: CGRect(x: CGFloat(p.x)-radius, y: CGFloat(p.y)-radius, width: radius*2, height: radius*2)), with: .color(color))
    }
    private func distance(_ p: TrackPathData.Corner, _ v: TrackViewport, _ x: Float, _ y: Float) -> Float {
        let projected = v.project(point: TrackPoint2D(x: p.x, y: p.y))
        return hypot(projected.x-x, projected.y-y)
    }
}
