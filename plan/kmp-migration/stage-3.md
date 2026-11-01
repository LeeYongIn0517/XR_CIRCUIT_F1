# KMP 전환 3단계 결과

작성일: 2026-11-01 (Asia/Seoul)

## 범위와 상태

- 상태: **검토 대기**. 3단계만 구현했으며 4단계는 시작하지 않았다.
- 프로젝트: `/Users/yongin/AndroidStudioProjects/XR_CIRCUIT`
- 기준 커밋: `9f5f10db1a68deb2b6b85da9df491fa7be08d74d`
- 작업 브랜치: `kmp/stage-3-data`. 시작 시 작업 트리는 깨끗했다. 이번 작업의 커밋·푸시·배포는 하지 않았다.
- 사용자의 3단계 실행 지시로 2단계 검토 수용을 기록했다. 기존 미검증 항목의 면제를 뜻하지 않는다.

## 변경 내용

기존 `data` 모듈을 KMP로 전환했다. DTO·JSON 직렬화·매퍼·Repository·원격 데이터 소스가 `data/src/commonMain`에 있으며 Android Context, Retrofit, Hilt, javax.inject 의존성이 없다. Android용 AAR와 iOS 기기/시뮬레이터용 `CircuitData.framework`를 만든다. framework는 기존 domain을 export한다.

`AssetReader`, `TimeProvider` 인터페이스와 Android/iOS 구현을 추가했다. Android는 Context의 assets, iOS는 NSBundle에서 같은 서킷 JSON을 읽는다. Hilt 조립은 `app/src/main/java/app/yongin/xr_circuit/data/di`로 옮겼다. 공유 상태·선택 관리·UI Store는 추출하지 않았다.

Retrofit 호출은 Ktor 3.0.3으로 전환했다. Android는 OkHttp, iOS는 Darwin 엔진을 주입한다. Open-Meteo의 실제 날씨 요청 파라미터와 매핑을 유지했으며 Jolpica/OpenF1의 기존 호출 형태도 옮겼다. 미구현 출발 그리드는 기존처럼 빈 목록을 반환한다. Jolpica 보조 조회는 일반 실패 시 null을 반환하되 코루틴 취소는 다시 던지도록 했다.

JSON 설정 `ignoreUnknownKeys=true`, `isLenient=true`, `explicitNulls=false`를 유지했다. 누락 필수 날씨 값과 잘못된 JSON은 실패하며 HTTP 오류도 호출자에게 전달한다. Android의 디버그 HTTP 로그도 유지했다.

### 캐시·시간 정책

- Repository: 기존 **단일 서킷 항목, 요청 시작 시각 기준 600,000ms TTL** 유지. 정확히 10분이 되면 만료한다. 실패·취소 응답은 캐시에 저장하지 않는다.
- Android HTTP: 기존 `cacheDir/http_cache`, **10MiB**, OkHttp의 서버 HTTP 캐시 정책 유지. connect/read/write 각각 30초.
- iOS HTTP: NSURLCache **디스크 10MiB, 메모리 0**, HTTP 헤더 정책 사용. URLSession 요청 inactivity timeout 30초. Android의 개별 connect/read/write API와 iOS의 URLSession 설정은 플랫폼별로 다르다.
- Ktor Darwin 3.0.3은 요청별 기본값으로 캐시를 무시한다. 실제 통합 테스트에서 이를 발견했고 `configureRequest`에도 `NSURLRequestUseProtocolCachePolicy`를 적용했다. 임의 TTL이나 강제 오프라인 캐시를 추가하지 않았다.
- 두 플랫폼에서 클라이언트 재생성 후 HTTP 캐시 재사용과 `no-store`를 검증했다. OS의 캐시 축출이나 앱 삭제 후 보존을 보장하는 것은 아니다.

### JSON 단일 원본과 iOS 화면

서킷 JSON 24개(목록 1개 + 상세 23개)는 `data/assets/circuits`로 이동했다. Android app의 assets sourceSet과 Xcode build phase가 이 원본을 사용한다. 에셋 생성 스크립트·기존 검증 스크립트 경로도 수정했다. APK 및 iOS bundle의 JSON 24개 모두 원본과 바이트 단위로 일치한다.

XR의 기존 상세·GLB·waypoints 가용성 필터를 유지했다. iOS 검증 화면은 고정 Swift fixture를 제거하고 공유 Repository에서 Silverstone 상세와 실제 날씨를 호출한다. 이는 제품 UI나 모바일 catalog 가용성 설계가 아니다. iOS bundle에 GLB는 없다.

## 검증 결과

- Gradle 최종 빌드 성공: Android XR APK, iOS 기기/시뮬레이터 framework 링크.
- data Android host: **17/17**. 공통 15개 + 실제 OkHttp HTTP 디스크 캐시/no-store 및 30초 read timeout 2개.
- data iOS Simulator: **18/18**. 동일 공통 15개 + 실제 Darwin HTTP 캐시/no-store, 요청 취소, 30초 timeout 3개.
- domain Android/iOS: 각각 **5/5**, presentation **3/3**, app **1/1**. 합계 **49건 통과**(같은 공통 테스트의 플랫폼별 실행 포함).
- 기존 XR 에셋 테스트 **19/19**. 합계 자동 검증 **68건**.
- 공통 계약 테스트는 DTO/매퍼 fixture, 요청 파라미터, 캐시 유효·정확한 만료·단일 항목 교체, HTTP 503 후 재시도, 네트워크 실패, 취소 전파, 잘못된 JSON·필수 필드 누락·에셋 누락을 확인했다.
- APK의 23개 catalog 항목에 상세·GLB·waypoints가 모두 포함됨을 확인했다.
- 공유 소스의 Android/Retrofit/Hilt 의존성 검색 및 `git diff --check` 통과.

### 실제 API 및 화면 — 자동 fixture 테스트와 별도

최종 XR APK를 API 34 XR 에뮬레이터에 설치해 `Success`, cold start `Status: ok`를 확인했다. Silverstone 프로필(5.891km·18코너·24m), 날씨, 기존 3D 트랙·섹터 스트립이 표시됐다. 로그에서 Ktor 요청의 Open-Meteo **HTTP 200**을 확인했다.

iPhone 15 Pro / iOS 17.0 시뮬레이터에서도 최종 앱을 설치·실행했다. 공유 Repository가 실제 bundle JSON을 읽어 같은 상세 정보를 표시했고, 실제 Open-Meteo 날씨 **6.9°C / 6.5km/h / 관측 2026-11-01T07:00**를 표시했다. XR의 7°C / 7km/h는 기존 UI 반올림 표현이며 API 원본 수치는 같았다. 실제 날씨는 호출 시각에 따라 달라질 수 있다.

## 남은 제약과 사용자 확인 항목

- **Swift XCTest 실행 미완료:** scheme 대상 검색이 여전히 `iOS 18.5 is not installed`로 실패한다. Swift 테스트 번들 빌드, Kotlin iOS 테스트 실행, 실제 앱 설치·실행은 별개로 기록했다.
- 물리 iPhone/XR 기기, 서명 검증은 하지 않았다.
- XR 런타임 확인은 위 초기 화면과 데이터 호출 범위다. 서킷 선택 실제 조작, 섹터/DRS 토글 전후 비교, 마커 45초 완주, 전체 서킷 런타임 순회는 이전 단계의 미검증으로 남는다. 빌드·에셋 테스트를 이 항목들의 통과로 계산하지 않았다.
- OpenF1/Jolpica 실제 서버 호출과 출발 그리드 신규 구현은 수행하지 않았다. 현재 앱의 실제 사용 경로인 Open-Meteo를 확인했다.
- iOS 검증 화면의 Swift Task와 Kotlin 작업 취소 연결·상태 관찰 수명 관리는 4단계 범위다. 이번 취소 검증은 공유 Repository와 HTTP 엔진 계층이다.
- 사용자 확인: 공유 데이터 결과·날씨 수치·10분 캐시 및 오류 동작을 검토한다. **다음 단계는 사용자 지시 대기.**

## 재현

프로젝트 루트에서 JDK 17을 사용한다.

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./gradlew :data:testAndroidHostTest :data:iosSimulatorArm64Test \
  :domain:testAndroidHostTest :domain:iosSimulatorArm64Test \
  :data:linkDebugFrameworkIosArm64 :data:linkDebugFrameworkIosSimulatorArm64 \
  :app:assembleDebug :presentation:testDebugUnitTest :app:testDebugUnitTest --console=plain
python3 plan/kmp-migration/test-ios-http.py
python3 -m unittest discover -s presentation/src/main/blender/tests -v
xcodebuild -project iosApp/CircuitConnection.xcodeproj -target CircuitConnection \
  -sdk iphonesimulator -configuration Debug SYMROOT="$PWD/build/ios-products" \
  CODE_SIGNING_ALLOWED=NO ARCHS=arm64 build
```

`test-ios-http.py`가 loopback 포트 18765의 fixture 서버를 실행하고 종료한다. `-Pstage3HttpIntegration`으로만 iOS 통합 테스트 소스를 포함하므로 일반 공통 테스트에는 서버가 필요 없다. 통합 테스트는 외부 실제 날씨 API를 사용하지 않는다. 이전 `:data:testDebugUnitTest` 대신 `:data:testAndroidHostTest`를 사용한다.

## 확인 자료

- [iOS 실제 데이터 화면](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-4/outputs/ios-data.png)
- [XR 실제 데이터 화면](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-4/outputs/xr-data.png)
- [최종 Gradle 결과](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-4/outputs/gradle-final.log)
- [iOS HTTP 통합 테스트](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-4/outputs/ios-http-test.log)
- [iOS 앱 빌드](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-4/outputs/ios-build.log)
- [Swift XCTest 번들 빌드](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-4/outputs/swift-test-build.log)
- [Swift XCTest 실행 제약](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-4/outputs/swift-test.log)
- [XR 에셋 테스트](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-4/outputs/assets-test.log)
- [패키징 일치 검사](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-4/outputs/packaging-check.txt)

구현 확인에 사용한 공식 자료: [Ktor 플랫폼 엔진](https://ktor.io/docs/client-engines.html), [Ktor 3.0.3 Darwin 요청 캐시 기본값 소스](https://github.com/ktorio/ktor/blob/3.0.3/ktor-client/ktor-client-darwin/darwin/src/io/ktor/client/engine/darwin/internal/DarwinRequestUtils.kt).
