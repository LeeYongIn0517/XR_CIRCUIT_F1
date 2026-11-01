# KMP 4단계 재실행 결과

**재실행 결과 (2026-11-01):** 기존 4단계 체크 표시를 근거로 삼지 않고, 실제 3단계 코드(HEAD `3bf220a349230a461db9e4095cb3c72d4bff405c`, 깨끗한 작업 트리)에서 4단계를 다시 구현했다. 작업 6/6 완료 / 사용자 검토 대기. 기존 `kmp/stage-3-data` 브랜치에 작업했으며 커밋·푸시는 하지 않았다.

- [x] `domain/commonMain`에 CircuitStore·CircuitState·Overlay·CircuitError를 추가했다. 선택·요청 세대·재시도·overlay 상태를 공통화했다.
- [x] Android ViewModel은 Store 액션 전달과 기존 UI 모델 변환만 담당한다. 아이콘·색상·표시 문자열은 Android UI에 유지한다.
- [x] iOS의 IosCircuitConnection과 Swift CircuitScreenModel이 상태 관찰·해제·취소·오류 표시를 담당한다. 검증 화면에서 두 서킷 선택·DRS·재시도를 연결했다.
- [x] 경로 보간과 정규화를 순수 Kotlin으로 옮겼다. XR는 결과를 Vector3로 변환하며 기존 렌더링 프레임/45초 루프는 플랫폼에 유지한다.
- [x] 공통 테스트 Android 12/12, iOS 12/12, data Android 17/17·iOS 15/15, Android presentation 3/3·app 1/1: **총 60건 통과**. 새 상태·보간 테스트 7개를 두 플랫폼에서 실행했다.
- [x] A→B→A의 이전 응답 무시, 이전 grid 오류 무시, overlay 유지, 없는 서킷, 오류 후 재시도, 관찰 해제/재관찰, 종료 후 응답 무시, 코너 선택, 거리 래핑을 검증했다.
- [x] XR APK 및 iOS 기기/시뮬레이터 framework, SwiftUI 검증 앱 빌드 성공. XR 앱 설치·cold start·프로필·날씨·3D 트랙·섹터 화면 확인.
- [x] iOS 시뮬레이터에서 Silverstone→Suzuka→Silverstone 전환, DRS ON 유지, 재요청 후 상세·실제 날씨 표시 확인.
- [ ] 물리 기기·서명·Swift XCTest 실행 및 전체 XR 수동 조작은 미검증으로 유지한다. 이번 Swift 검증은 앱 빌드와 실행 관찰이며 XCTest 실행 성공으로 계산하지 않았다.

**소유·스레드 규칙:** 모든 Store 액션과 관찰은 소유 플랫폼의 UI 디스패처에서 호출한다. Store는 부모 scope 아래 SupervisorJob을 소유하고, close 시 요청 세대를 무효화하고 자식 작업을 취소한다. 부모 scope는 취소하지 않는다. Android는 viewModelScope/Main 및 onCleared를 사용한다. iOS는 Main.immediate에서 관찰하고 Swift @MainActor 소유자가 onAppear에 한 번 연결, onDisappear에 observation.cancel → connection.close 순서로 정리한다. callback은 weak self를 사용한다. close 후 늦은 응답·액션은 상태를 변경하지 않는다.

**범위 경계:** iOS catalog는 검증용 Silverstone/Suzuka JSON 두 항목만 사용한다. 제품용 모바일/XR 가용성 정책·2D 에셋·제품 UI는 5단계 이후 범위로 남긴다. 기존 미구현 Pins 표시·출발 그리드·뷰 리셋을 새로 구현하지 않았다.

**실행 자료:** [XR 화면](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-6/outputs/xr-stage4.png), [iOS 화면](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-6/outputs/ios-stage4.png), [Gradle 빌드](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-6/outputs/build.log), [최종 테스트](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-6/outputs/tests.log), [iOS 빌드](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-6/outputs/ios-build.log). 프로젝트 상세 기록은 `plan/kmp-migration/stage-4.md`다. **5단계는 실행하지 않았다.**


## 재현 명령

프로젝트 루트에서 JDK 17을 사용한다.

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./gradlew :domain:testAndroidHostTest :domain:iosSimulatorArm64Test :data:testAndroidHostTest :data:iosSimulatorArm64Test :presentation:testDebugUnitTest :app:testDebugUnitTest --console=plain
./gradlew :data:linkDebugFrameworkIosSimulatorArm64 :data:linkDebugFrameworkIosArm64 :app:assembleDebug --console=plain
xcodebuild -project iosApp/CircuitConnection.xcodeproj -target CircuitConnection -sdk iphonesimulator -configuration Debug SYMROOT="$PWD/build/ios-products" CODE_SIGNING_ALLOWED=NO ARCHS=arm64 build
```

기존 HTTP 통합 스크립트·XR 에셋 전체 테스트는 이번에 다시 실행하지 않았다. 현재 data Android 테스트에는 기존 OkHttp 통합 2개가 포함되고, iOS data는 공통 15개다.
