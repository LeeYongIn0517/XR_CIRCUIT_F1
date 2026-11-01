# Stage 2: shared domain and native app connection

Scope: domain only. Data repositories, API/cache, CircuitStore, mobile product UI and 2D assets are unchanged.

## Toolchain

- Kotlin / Compose compiler / serialization plugin: 2.2.20
- AGP: 8.11.1; Gradle wrapper: 8.13
- Hilt: 2.57.2 (2.52 rejects Kotlin 2.2 metadata; Kotlin 2.2 support landed in 2.57)
- JDK: 17 toolchain; Android output remains JVM 11
- Xcode: 16.4; iOS application deployment target: 17.0
- Existing XR and Compose library versions retained.

The original Kotlin 2.1.20 / AGP 8.12.3 / Gradle 8.13 combination was outside the KMP compatibility range for the installed Xcode. Kotlin 2.2.20 supports Xcode 16.4 and AGP through 8.11.1. See https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html.

`domain` uses the Android KMP library plugin (`androidLibrary` DSL for AGP 8.11), with Android, iosArm64 and iosSimulatorArm64 targets. No additional shared module or XR compilation target is needed. `CircuitDomain` is a static framework; Android Hilt providers live in `data/di/DomainModule.kt`.

## Commands (repository root)

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./gradlew :domain:testAndroidHostTest :domain:iosSimulatorArm64Test \
  :domain:linkDebugFrameworkIosArm64 :domain:linkDebugFrameworkIosSimulatorArm64 \
  :app:assembleDebug :data:testDebugUnitTest \
  :presentation:testDebugUnitTest :app:testDebugUnitTest --continue
```

Open `iosApp/CircuitConnection.xcodeproj`, shared scheme `CircuitConnection`. The build phase compiles the matching Kotlin framework automatically. No dependency manager or project generator is needed to open the checked-in Xcode project.

The Swift repository is a fixed test fixture. Swift calls the real GetCircuitCatalogUseCase and GetCircuitDetailUseCase and reads CircuitDetail.elevationDeltaM. No real API or shared data-layer implementation is claimed.

Unsigned simulator build without scheme destination discovery:

```sh
xcodebuild -project iosApp/CircuitConnection.xcodeproj -target CircuitConnection \
  -sdk iphonesimulator -configuration Debug SYMROOT="$PWD/build/ios-products" \
  CODE_SIGNING_ALLOWED=NO ARCHS=arm64 build
```

The development bundle ID `app.yongin.xr-circuit.connection` is provisional and does not reserve a production identifier. Physical iOS device builds require an Apple development team/signing configuration; do not store credentials in this repository.

## Approval history

Stage 1 was reviewed by the user when they explicitly requested stage 2. Its remaining XR interaction/physical-device checks are not waived. Stage 2 requires a separate review; do not automatically start stage 3.

## Verified result (2026-11-01)

Stage 2: review pending; stage 3 not started. Android common tests 5/5, iOS common tests 5/5, data 7/7, presentation 3/3, app 1/1 passed. Both iOS frameworks and the XR debug APK built. The new XR APK installed and cold-launched successfully on the API 34 XR emulator. The iOS app built and ran on iOS 17.0, showing Silverstone, 5.891 km and the shared 24 m elevation calculation.

The Swift XCTest bundle compiles, but scheme-based test execution is blocked by Xcode destination discovery reporting that iOS 18.5 is not installed. Do not count XCTest as executed. Direct target builds plus simctl install/launch work. Physical-device testing and full XR interaction regression are outstanding. D8 emits ARCore stack-map warnings, while the build succeeds.

Detailed report and logs: `/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md/outputs/stage2/KMP-stage-2-result.md`.
