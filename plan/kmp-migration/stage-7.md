# KMP 7단계 결과 — iOS 2D 제품 화면 연결

- 상태: **구현 완료 · 검토 대기 / 일부 런타임 검증 미완료** · 2026-11-01
- 저장소: `/Users/yongin/AndroidStudioProjects/XR_CIRCUIT`
- 기준 HEAD: `b4c6ed6f569cd2ea14dac11d81674af24846c1c5` · 기존 `kmp/stage-3-data` 브랜치 유지. 시작 시 작업 트리 깨끗함.
- 사용자 7단계 실행 지시로 6단계 검토를 수용했다. 이전 미검증 항목은 면제하지 않았다.
- 커밋·푸시·배포 없음. **8단계는 실행하지 않았다.**

## 구현한 내용

- 최소 iOS 연결 화면을 SwiftUI 제품 화면으로 확장했다. 공유 모바일 catalog에서 제공하는 Silverstone/Suzuka만 선택 메뉴에 표시한다.
- 같은 `IosCircuitConnection → CircuitStore → Repository`를 사용한다. 서킷 선택·코너 선택·섹터/DRS 토글·재시도는 Store 액션으로 전달하며 Swift에 요청 세대·캐시·선택 규칙을 재구현하지 않았다.
- `IosCircuitConnection.selectCorner` 한 개를 추가했다. Android/XR와 공유 상태의 기존 구현은 변경하지 않았다.
- Android와 같은 PNG·path.json을 bundle에서 비동기로 읽는다. 공유 `TrackViewport`를 이미지·오버레이·코너·마커·역좌표 터치 판정에 함께 사용한다. 24pt 이내 코너 탭과 별도의 최소 48pt 높이 코너 버튼을 구현했다.
- 섹터/DRS 스위치, 마커 ON/OFF, 45초 시각적 루프, 선택 코너 강조, 프로필·랩 기록·날씨, 로딩·오류·재시도를 연결했다. 마커는 플랫폼 렌더링 상태이며 실시간 차량 데이터가 아니다.
- ScrollView와 safe area, Dynamic Type, 헤더·토글·트랙의 접근성 설명 및 코너 선택 상태를 적용했다. 프로필 카드와 코너 버튼은 고정 높이 대신 내용에 따라 늘어난다.
- `@MainActor` 화면 소유자가 구독을 한 번만 시작한다. 종료 시 observation.cancel → connection.close 순서로 scope와 HTTP client를 정리한다. callback은 weak self이며 이미지 작업은 화면 작업 취소를 전달하고 취소 후 결과를 게시하지 않는다.
- TimelineView는 background/inactive에서 정지하며 foreground에서 45초 루프를 다시 시작한다. foreground 전환만으로 Store를 새로 만들지는 않는다.
- Pins 표시·그리드·전략·리셋은 기존 미구현 범위로 유지하고 작동하는 컨트롤로 추가하지 않았다.

## Android와 기능 비교

비교 기준은 6단계 Android 결과와 이번 iOS 실행·공통 테스트다. 이번 단계에서 Android 화면 전체를 다시 수동 조작한 것은 아니다.

- **서킷 목록·전환:** 양쪽 모두 모바일 manifest의 Silverstone/Suzuka 두 개다. iOS에서 Silverstone→Suzuka→Silverstone을 조작해 이름·트랙·프로필·날씨 변경을 확인했다.
- **프로필:** Silverstone 5.891km / 18코너 / DRS 2개 / 고저차 24m / 랩 기록 1:27.097. Suzuka 5.807km / 18코너 / DRS 1개. 공유 상세 JSON과 Android 표시 규칙에 일치한다. Suzuka의 미제공 고저차·랩 기록은 만들지 않는다.
- **오버레이:** 양쪽 모두 공유 Store의 Sectors/DRS 선택을 사용한다. iOS DRS ON 및 Sectors OFF가 서킷 변경 후 유지됨을 확인했다. 화면 표현은 Android chip, iOS switch로 다르다.
- **코너:** 동일 domain id와 turnCode를 연결한다. iOS T1 버튼 조작 후 접근성 선택 상태와 트랙의 선택 코너 값이 T10→T1로 바뀌었다. 트랙 직접 탭 판정은 구현·컴파일을 확인했으며 실제 좌표 탭 검증은 남긴다.
- **마커:** 동일 path.json의 progress 좌표와 45초 루프를 사용한다. iOS ON/OFF와 서킷 변경 후 OFF 유지 확인. 1·23·46초 기록에서 이동 및 한 바퀴 복귀 확인. 앱 복귀 시 루프가 재시작하며 실제 레이스 위치가 아니다.
- **날씨:** 같은 공유 Repository와 모델을 사용하며 Android와 동일한 기온·강수량·강수확률·풍속/방향을 표시한다. iOS 실행 시 Silverstone 8°C·9km/h N, Suzuka 17°C·6km/h SW를 확인했다. 실시간 응답은 호출 시각이 달라질 수 있으므로 수치 동등성의 fixture 증명과 구분한다.
- **오류·재시도:** 양쪽 모두 공유 loading/error/retry 상태를 사용한다. iOS 분기 구현·컴파일 및 공통 실패·취소 테스트를 확인했다. iOS 앱의 실제 네트워크 차단→재시도 UI 시나리오는 미검증이다.
- **미구현 기능:** 양쪽 모두 신규 Pins·전략·그리드·리셋을 추가하지 않았다.

## 검증 결과와 증거

- Xcode 16.4 / iOS 최소 17.0. iOS 17.2 iPhone 15 Pro 및 iPhone SE(3세대) 시뮬레이터 실행 성공.
- [시뮬레이터 앱 빌드](/Users/yongin/Documents/Codex/2026-11-01/6-7-ios-2d-2/outputs/ios-build.log), [최종 앱·Swift 테스트 번들 빌드](/Users/yongin/Documents/Codex/2026-11-01/6-7-ios-2d-2/outputs/ios-test-build.log), [unsigned iPhone 앱 빌드](/Users/yongin/Documents/Codex/2026-11-01/6-7-ios-2d-2/outputs/ios-device-build.log) 성공. 기기용 framework도 링크 성공. 서명 완료나 실기기 실행을 뜻하지 않는다.
- [iOS 공통 테스트](/Users/yongin/Documents/Codex/2026-11-01/6-7-ios-2d-2/outputs/shared-tests.log): domain 14 + data 16 = 30건, 실패·오류·skip 0. domain은 UP-TO-DATE 재사용, data 테스트는 실제 재실행했다.
- [Android 공통 테스트](/Users/yongin/Documents/Codex/2026-11-01/6-7-ios-2d-2/outputs/android-shared-tests.log): domain 14 + data 18 = 32건, 실패·오류·skip 0. 기존 UP-TO-DATE 결과를 재사용했다. Android 추가 data 2건은 플랫폼 엔진 테스트다. 두 타깃의 공통 fixture·Store 코드 결과를 비교하며 서로 다른 테스트 수를 UI 테스트 수로 합산하지 않는다.
- 공통 테스트는 빠른 선택의 이전 응답 무시, 오류 후 retry, overlay 유지, 코너 선택, 구독 해제·재관찰, close 후 응답 무시, 경로/viewport와 데이터 계약을 포함한다.
- [패키지·테스트 집계](/Users/yongin/Documents/Codex/2026-11-01/6-7-ios-2d-2/outputs/package-test-summary.json): manifest·PNG·path·provenance 총 11개 파일이 원본과 바이트 단위 일치. iOS bundle GLB 0개.
- 화면에서 Silverstone/Suzuka 전환, DRS ON 유지, 섹터/마커 OFF 유지, 코너 선택을 확인했다. 실제 UI 접근성 트리에 토글 값, 트랙 설명, 코너 선택 trait가 노출됐다.
- iPhone 15 Pro의 홈 화면 이동→앱 복귀 후 DRS ON·프로필·날씨 유지 확인. 중복 구독은 소유자 guard와 해제 경로를 코드 검토하고, 새 Swift 수명주기 테스트의 컴파일까지 확인했다.
- 최대 Dynamic Type(`accessibility-extra-extra-extra-large`)의 상단·토글 줄바꿈 및 safe area 확인. 검증 후 기본 `large`로 복원했다. iPhone SE 375pt 폭에서 트랙 정렬 확인. 가로 회전 후 데이터 유지 확인. 가로 화면은 트랙이 첫 화면 아래로 이어진다.
- [마커 루프 영상](/Users/yongin/Documents/Codex/2026-11-01/6-7-ios-2d-2/outputs/ios-marker-lap.mp4), [1초](/Users/yongin/Documents/Codex/2026-11-01/6-7-ios-2d-2/outputs/ios-marker-1.png), [23초](/Users/yongin/Documents/Codex/2026-11-01/6-7-ios-2d-2/outputs/ios-marker-23.png), [46초](/Users/yongin/Documents/Codex/2026-11-01/6-7-ios-2d-2/outputs/ios-marker-46.png): 흰 마커 이동 및 약 45초 뒤 같은 위치 부근 복귀를 확인했다.
- `git diff --check` 통과. XR 소스·렌더러·Android manifest·에셋에는 변경이 없다. 이번 단계에서 XR 빌드·실행은 반복하지 않았다.

## 미완료 검증과 제약

- **Swift XCTest 실행 차단:** [실행 로그](/Users/yongin/Documents/Codex/2026-11-01/6-7-ios-2d-2/outputs/xctest.log). 시뮬레이터가 simctl로 실행되지만 Xcode scheme은 `iOS 18.5 is not installed`로 대상 검색에 실패한다. 기존 테스트 1개와 신규 에셋/수명주기 테스트 2개는 번들 컴파일만 통과했고 실행 통과로 집계하지 않는다.
- **물리 iPhone:** 연결 목록의 iPhone 14 Pro가 `unavailable` 상태여서 실행하지 못했다. 서명·실기기 검증은 남긴다.
- **접근성·터치:** VoiceOver 실제 음성 탐색, 전체 터치 스크롤 동선, 직접 트랙 탭 및 가로 모드 하단 내용까지의 조작은 최종 수동 확인이 필요하다. 자동화의 스크롤/드래그 시도에서 화면 이동을 확인하지 못해 통과로 표시하지 않았다. 실제 앱의 스크롤 장애로 원인을 확정한 것은 아니다.
- **오류·누수:** iOS 실제 오프라인 복구, Instruments 장시간 메모리/네트워크 누수 검증, 백그라운드 장시간 중단 후 복귀는 미검증이다. 공통 close/cancel 테스트와 코드 검토가 실제 장시간 프로파일링을 대신하지 않는다.
- 전체 23개 서킷 확장·세 플랫폼 전면 회귀는 8단계 범위이며 수행하지 않았다. 이전 XR 미검증 및 에셋 권리 검토 잔여도 유지한다.

## 주요 파일과 재현

변경 파일은 저장소의 `iosApp/CircuitConnectionApp.swift`, `iosApp/CircuitConnectionTests.swift`, `data/src/iosMain/kotlin/app/yongin/xr_circuit/data/platform/IosCircuitConnection.kt`와 `plan/kmp-migration/stage-7.md`다.

저장소 루트에서 Android Studio 번들 JDK를 JAVA_HOME으로 지정한다.

```sh
./gradlew :domain:iosSimulatorArm64Test :data:iosSimulatorArm64Test :data:linkDebugFrameworkIosArm64 --console=plain
./gradlew :domain:testAndroidHostTest :data:testAndroidHostTest --console=plain
xcodebuild -project iosApp/CircuitConnection.xcodeproj -target CircuitConnectionTests -sdk iphonesimulator -configuration Debug SYMROOT="$PWD/build/ios-products" CODE_SIGNING_ALLOWED=NO ARCHS=arm64 build
xcodebuild -project iosApp/CircuitConnection.xcodeproj -target CircuitConnection -sdk iphoneos -configuration Debug SYMROOT="$PWD/build/ios-device-products" CODE_SIGNING_ALLOWED=NO ARCHS=arm64 build
```

## 사용자 확인과 다음 단계

- iOS 화면의 정보 배치·switch 조작·큰 글꼴·작은 화면에서 스크롤 동선을 검토한다.
- [Suzuka + DRS](/Users/yongin/Documents/Codex/2026-11-01/6-7-ios-2d-2/outputs/ios-suzuka-drs.png), [작은 iPhone SE](/Users/yongin/Documents/Codex/2026-11-01/6-7-ios-2d-2/outputs/ios-small-screen.png), [최대 글꼴](/Users/yongin/Documents/Codex/2026-11-01/6-7-ios-2d-2/outputs/ios-large-type.png), [가로 화면](/Users/yongin/Documents/Codex/2026-11-01/6-7-ios-2d-2/outputs/ios-landscape.png), [최종 Silverstone](/Users/yongin/Documents/Codex/2026-11-01/6-7-ios-2d-2/outputs/ios-marker-46.png).
- [갱신한 단계별 계획](/Users/yongin/Documents/Codex/2026-11-01/6-7-ios-2d-2/outputs/KMP-step-by-step-plan.md)에 완료 구현과 남은 검증을 분리했다.
- **여기서 멈춘다. 8단계는 사용자 지시 대기.**
