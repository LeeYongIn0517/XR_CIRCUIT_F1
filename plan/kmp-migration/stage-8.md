# KMP 8단계 결과 — 전체 23개 서킷과 세 플랫폼 회귀

- 상태: **전체 서킷 적용·자동 회귀 완료 / 검토 대기 / 일부 수동·실기기 검증 미완료** · 2026-11-02
- 저장소: `/Users/yongin/AndroidStudioProjects/XR_CIRCUIT`
- 기준 HEAD: `b4c6ed6f569cd2ea14dac11d81674af24846c1c5`, 기존 `kmp/stage-3-data` 브랜치 유지.
- 시작 시 7단계 변경 3개 파일과 `plan/kmp-migration/stage-7.md`가 미커밋 상태였다. 모두 보존했다. 적용되는 AGENTS.md는 발견되지 않았다.
- 사용자 8단계 실행 지시로 7단계 검토 수용. 이전 미검증 항목의 면제는 아니다.
- **9단계는 실행하지 않았다. 커밋·푸시·릴리스·배포 없음.**

## 변경 내용

- 모바일 manifest를 2개에서 기존 XR 공통 카탈로그의 **23개 전체**로 확장했다. 공통 카탈로그 순서와 Silverstone 기본 선택을 유지한다. Android/iOS/XR의 제공 ID 집합은 같다. 현재 시즌 참가 목록을 새로 결정한 작업은 아니다.
- 승인된 1024×1024 RGBA·8% 여백·동일 투영 규격으로 각 트랙/섹터/DRS PNG와 path/provenance JSON을 만들었다. 총 69 PNG, 46 JSON, index 1개 = **116개 모바일 에셋**이다.
- 생성기는 ID에서 파일명을 추정하지 않고 공통 카탈로그의 `detailAssetPath`·`waypointsAsset`을 따른다. Melbourne↔albert_park, Singapore↔marina_bay 등의 연결을 보존한다.
- 실제 코너 수(10~22)와 DRS 수를 유지한다. Madring은 기존 상세 데이터대로 DRS 0개이며 투명 DRS 레이어를 제공한다. 미제공 DRS 구간을 만들지 않았다.
- 전체 확장에서 Singapore 섹터 PNG 중심선 일부의 알파가 200 이하로 떨어지는 생성기 문제가 발견됐다. 경로의 모든 연결점에 동일 반경을 채워 수정했고 전체 69 레이어의 실제 픽셀 검증이 통과했다. 기존 두 서킷 PNG도 같은 수정으로 재생성했다.
- 원본은 기존 저장소의 2017/2025 레이아웃이다. 모바일 문구의 일괄 `2025 layout`을 `bundled layout`으로 수정했다. 현행 시즌 레이아웃·사용 권한을 확인했다는 뜻은 아니다.
- 23개 상세 JSON을 고정 snapshot으로 사용하는 Android/iOS 공통 테스트를 추가했다. 카탈로그/모델 일치, 전체 Store 선택·코너·overlay 유지·고정 날씨를 검사한다. `tools/generate_circuit_regression_fixture.py --check`로 원본과 fixture의 변경을 감지한다.
- Android 전체 선택 UI 테스트와 전체 PNG 디코딩 계측을 추가하고, Swift bundle/viewport 테스트를 전체 23개와 실제 코너 수로 확장했다. Swift 테스트는 **컴파일만 성공**했다.
- 삭제 가능한 대체 코드의 범위를 검토했으나 IosDataConnection은 실제 Swift 테스트에서 사용 중이다. 기존 probe와 미완성 기능을 임의 삭제하지 않았다. XR 렌더러·GLB·공통 원본 상세 JSON에는 변경이 없다.

## 플랫폼별 검증

### Android 스마트폰

- 최종 mobile debug APK 빌드 성공. Pixel 7 API 34 계측 **4/4 통과**: 전체 23개 서킷 메뉴 선택과 트랙 로드, DRS·Sectors 유지, 기존 두 서킷 전환·Marker OFF, Activity 재생성 후 상태 유지, 전체 PNG 디코딩, 기존 패키지 테스트.
- 전체 서킷 테스트는 각 선택 후 새 트랙의 accessibility description이 나타날 때까지 기다린다. 모든 서킷의 45초 완주나 전체 코너 버튼 조작을 검사한 것은 아니다.
- 공통 Store의 A→B→A 늦은 응답 무시·오류 후 retry·구독 해제·close, 데이터 계층 offline·잘못된 JSON·취소·캐시 테스트 통과. Android 실제 엔진의 30초 timeout 테스트도 재실행 통과.
- APK **39,855,511 bytes**(6단계 34,958,096 대비 +4,897,415 bytes, 약 14.0%). GLB 0개, DEX의 `Landroidx/xr/` 참조 0, 116개 모바일 파일이 원본과 바이트 일치.
- 최종 실행 화면은 [Android](/Users/yongin/Documents/Codex/2026-11-02/7-8-2/outputs/android-stage8.png), 계측 결과는 [XML](/Users/yongin/Documents/Codex/2026-11-02/7-8-2/outputs/android-ui-tests.xml)·[로그](/Users/yongin/Documents/Codex/2026-11-02/7-8-2/outputs/regression-tests.log).

### iOS 스마트폰

- Xcode 앱 및 Swift 테스트 번들 빌드 성공. iOS 17.2 iPhone SE(3세대) 시뮬레이터에서 **23개 전체 서킷을 직접 선택**해 트랙 로드·DRS ON 유지·프로필/코너 표시를 확인했다. 마지막 메뉴 항목 Yas Marina까지 선택 가능하다.
- 대표 실제 표시: Melbourne 5.278km/14코너/DRS 4, Shanghai 5.451km/16코너/DRS 2, Madring 5.400km/22코너/DRS 0, Yas Marina 5.281km/16코너/DRS 2. fixture와 원본 해시 검증을 함께 사용한다.
- 홈 화면 이동 후 동일 앱 프로세스로 복귀하여 Yas Marina·DRS ON 유지 확인. [홈 화면](/Users/yongin/Documents/Codex/2026-11-02/7-8-2/outputs/ios-home.png), [최종 트랙](/Users/yongin/Documents/Codex/2026-11-02/7-8-2/outputs/ios-stage8.png), [전체 순회 기록](/Users/yongin/Documents/Codex/2026-11-02/7-8-2/outputs/ios-runtime-checks.json).
- 실제 Darwin HTTP 엔진의 **캐시/no-store, 30초 timeout, 취소 3/3 통과**. 로컬 loopback fixture를 사용했다. 이 결과는 실제 앱의 네트워크 차단·복구 UI 검증과 구분한다.
- bundle은 23개/116개 파일 모두 원본과 바이트 일치, GLB 0개. 시뮬레이터 `.app` 비압축 크기 **36,098,053 bytes**이며 App Store 배포 크기나 IPA 크기가 아니다.
- **Swift XCTest 실행 실패:** 시뮬레이터 ID와 SDK를 명시해 재시도했지만 scheme 대상 검색이 `iOS 18.5 is not installed`로 실패했다. Kotlin Native 테스트·앱 실행 관찰·Swift 번들 빌드를 XCTest 실행 통과로 계산하지 않았다. [실행 로그](/Users/yongin/Documents/Codex/2026-11-02/7-8-2/outputs/xctest.log).

### Android XR

- 최종 xr debug APK 빌드 성공. 기존 XR 관련 presentation 3건·app 1건 단위 테스트와 **XR 에셋 19건** 통과.
- 23개 GLB의 모델/섹터/DRS 노드와 공통 상세·경로 파일 연결을 검증했다. APK는 기존과 같은 **86,397,877 bytes**, GLB 92개, 모바일 tracks 파일 0개다. 같은 크기를 APK 전체 해시 동일성으로 해석하지 않는다.
- XR Headset 에뮬레이터에 최종 APK를 설치해 cold start. `full-space-managed` 상태에서 Silverstone 3D·색상 섹터·프로필·랩 기록·실제 날씨를 확인했고 AndroidRuntime 크래시 기록이 없었다. [XR 화면](/Users/yongin/Documents/Codex/2026-11-02/7-8-2/outputs/xr-stage8.png), [상태](/Users/yongin/Documents/Codex/2026-11-02/7-8-2/outputs/xr-activity.log), [크래시 로그](/Users/yongin/Documents/Codex/2026-11-02/7-8-2/outputs/xr-crashes.log).
- XR 전체 23개 수동 선택·DRS 토글 전후·각 45초 완주·홈 공간 전환은 미완료다. 사용 가능한 컴퓨터 조작 앱 목록에서 XR 에뮬레이터 입력 화면이 노출되지 않아 일반 앱처럼 직접 조작하는 경로를 확보하지 못했다. 에셋/공통 테스트와 정지 화면을 전체 XR 조작 통과로 계산하지 않았다.

## 테스트와 성능 집계

- 공통·단위·네이티브 HTTP·Android 계측 **총 81건 통과, failure/error/skip 0**. domain Android 14/iOS 14, data Android 20/iOS 21(통합 3 포함), presentation mobile 3/xr 3, app mobile 1/xr 1, mobile 계측 4. [집계](/Users/yongin/Documents/Codex/2026-11-02/7-8-2/outputs/test-summary.json).
- 별도로 Python XR 에셋 **19/19**와 23개 모바일 에셋 검증 통과. 전체 합계는 100건의 테스트이며, 69개 PNG 검증은 별도의 에셋 검사다. Swift XCTest 실행은 이 수에 포함하지 않는다.
- 첫 Gradle 실행은 `--rerun-tasks`로 202 task를 재실행했다. 후속 계측·fixture 실행은 변경된 task를 실행하고 일부 UP-TO-DATE를 재사용했다. Native HTTP는 추가 opt-in으로 실제 실행했다.
- Pixel 7 API 34 debug, 서킷마다 PNG 3개를 한 번씩 연속 decode: 최소 **68.2ms**, 중앙값 **162.3ms**, p95 **567.3ms**(nearest-rank), 최대 **614.5ms**. 총 비트맵 할당량은 서킷당 **12,582,912 bytes(12MiB)**다. 계측은 각 bitmap을 즉시 recycle하므로 동시 보유량/프로세스 최고 메모리를 측정한 값이 아니다.
- 별도 최종 Silverstone 실행에서 PSS 67,289KiB / RSS 124,300KiB 단일 snapshot. [성능 집계](/Users/yongin/Documents/Codex/2026-11-02/7-8-2/outputs/performance-summary.json), [원시 decode](/Users/yongin/Documents/Codex/2026-11-02/7-8-2/outputs/android-decode-metrics.log), [프로세스 메모리](/Users/yongin/Documents/Codex/2026-11-02/7-8-2/outputs/android-memory.log).
- 1단계 제안은 목표 기기에서 트랙 전환 1초와 60Hz 약 16.7ms frame budget이었다. 이번 decode는 1초 이내지만 UI 전환·GPU 업로드 전체 시간을 포함하지 않는다. **목표 기기 성능 합격으로 판정하지 않는다.** iOS/XR frame-time·지속 메모리·열/장시간 부하는 미측정이다.
- [패키지 검사](/Users/yongin/Documents/Codex/2026-11-02/7-8-2/outputs/package-summary.json), [모바일 에셋 검사](/Users/yongin/Documents/Codex/2026-11-02/7-8-2/outputs/asset-validation.log), [XR 에셋 테스트](/Users/yongin/Documents/Codex/2026-11-02/7-8-2/outputs/xr-assets.log), [fixture 변경 검사](/Users/yongin/Documents/Codex/2026-11-02/7-8-2/outputs/fixture-check.log), `git diff --check` 통과.

## 기능 체크리스트와 남은 문제

- [x] 전체 서킷의 2D 에셋·manifest·프로필·3D 연결 무결성.
- [x] Android 23개 전체 선택과 로드, DRS 상태 유지, Marker OFF 및 재생성.
- [x] iOS 23개 전체 선택과 로드, DRS 상태 유지, 홈 복귀.
- [x] XR 최종 APK의 대표 트랙·프로필·날씨 실행 및 23개 에셋 정합성.
- [x] 고정 fixture로 Android/iOS 모델·전체 선택/코너·overlay·날씨 동일성.
- [x] 공유 offline/malformed/cancel/retry/rapid-selection 및 Android/Darwin 실제 엔진 timeout·cache.
- [ ] XR 23개 전체 수동 전환·토글 전후·마커 완주·홈 공간 전환.
- [ ] 세 플랫폼의 실제 offline→복구 UI 전체 흐름, 전체 코너 직접 터치, 새 전체 에셋에서 45초 완주 반복 및 화면 크기 변경 전면 검증. 이번 상태/viewport 자동 테스트와 6·7단계 관찰을 이 항목의 완료로 대체하지 않는다.
- [ ] Swift XCTest 실행 환경 문제 해결 및 전체 3개 XCTest 실행. 현재는 번들 컴파일만 성공.
- [ ] 물리 Android/iPhone/XR의 설치·접근성 음성 탐색·실기기 성능/메모리/애니메이션 지속 부하. 연결된 목표 물리 기기로 검증하지 않았다.
- [ ] 기존 지도 추적/파생 에셋 재배포 권리와 출시할 시즌 레이아웃 정확성 검토. 출처 URL·해시는 허가 증명이 아니다. 외부 배포 전 해결이 필요하다.

**출시 판단:** 새 빌드/자동 테스트 실패는 발견되지 않았다. 위 미검증 기능, XCTest 실행 환경, 물리 기기 성능·접근성, 에셋 권리·레이아웃 검토가 남아 있으므로 출시 승인 상태가 아니다. Pins 표시·그리드·전략·리셋은 기존 미구현 범위로 유지한다.

## 재현과 인계

저장소 루트, Android Studio 번들 JDK 및 Pillow가 설치된 Python 사용:

```sh
python3 tools/generate_mobile_tracks.py
python3 tools/validate_mobile_tracks.py
python3 tools/generate_circuit_regression_fixture.py --check
python3 -m unittest discover -s presentation/src/main/blender/tests -p 'test_*.py'
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ./gradlew :app:assembleMobileDebug :app:assembleXrDebug :domain:testAndroidHostTest :data:testAndroidHostTest :domain:iosSimulatorArm64Test :data:iosSimulatorArm64Test :presentation:testMobileDebugUnitTest :presentation:testXrDebugUnitTest :app:testMobileDebugUnitTest :app:testXrDebugUnitTest --rerun-tasks --console=plain
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_SERIAL=emulator-5554 ./gradlew :app:connectedMobileDebugAndroidTest --console=plain
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' python3 plan/kmp-migration/test-ios-http.py
xcodebuild -project iosApp/CircuitConnection.xcodeproj -target CircuitConnectionTests -sdk iphonesimulator -configuration Debug SYMROOT="$PWD/build/ios-products" CODE_SIGNING_ALLOWED=NO ARCHS=arm64 build
```

주요 변경 위치: `mobile-assets/tracks/`, `tools/{generate_mobile_tracks,validate_mobile_tracks,generate_circuit_regression_fixture}.py`, `data/src/commonTest/.../AllCircuitRegressionTest.kt`, `app/src/androidTestMobile/.../MobileScreenTest.kt`, `iosApp/CircuitConnectionTests.swift`, 양쪽 모바일 화면의 레이아웃 연도 문구. 기존 7단계 UI 변경과 구분해 기록했다.

[전체 서킷 미리보기](/Users/yongin/Documents/Codex/2026-11-02/7-8-2/outputs/all-circuits.png), [갱신 계획](/Users/yongin/Documents/Codex/2026-11-02/7-8-2/outputs/KMP-step-by-step-plan.md). 저장소 기록은 `plan/kmp-migration/stage-8.md`이며, 원래 7단계 채팅의 계획과 이번 인계본을 같은 상태로 갱신했다. 단계 요약 체크는 기존 규칙대로 주요 구현·자동 회귀 완료를 표시하며, 남은 전체 검증의 세부 체크는 비워 뒀다.

**여기서 멈춘다. 9단계는 사용자 지시 대기.**
