# KMP 9단계 결과 — CI·릴리스 빌드·서명 준비

- 실행일: **2026-11-08 (Asia/Seoul)**
- 상태: **주요 준비 완료 / 검토 대기 / 서명 차단 / 완전한 배포 준비 미완료**.
- 저장소 `/Users/yongin/AndroidStudioProjects/XR_CIRCUIT`, 기준 커밋 `ba9c0a4`, 기존 `kmp/stage-3-data` 브랜치 유지. 시작 시 작업 트리는 깨끗했고 적용되는 AGENTS.md는 발견되지 않았다.
- 사용자 9단계 실행 지시로 8단계 검토를 수용했다. 기존 미검증 항목은 유지한다.
- **9단계만 실행했다. 스토어 업로드·TestFlight·export·프로비저닝 갱신·10단계·커밋·푸시·원격 CI 실행은 하지 않았다.**

## 변경 내용

- GitHub Actions에 Android/Linux와 Native/iOS/macOS 두 검사 job을 추가했다. 두 공통 타깃 테스트, Android mobile/xr release 단위 테스트와 APK/AAB, Swift 테스트 번들 컴파일 및 iOS unsigned archive를 검사한다. Java17·Xcode16.4·Apple Silicon을 명시했다.
- Android release signing을 네 환경변수로만 연결했다. 미제공 시 unsigned, 일부 제공 시 명확한 오류로 중단한다. debug key로 대체하거나 새 release key를 생성하지 않았다.
- iOS 앱/테스트의 버전을 `1.0 (1)`로 명시했다. 기존 앱 ID·최소 OS·타깃을 유지했다.
- release 패키지에 불필요한 XR 선택 로그를 제거했다. HTTP BODY 로그는 기존 debuggable 조건으로 release에서 비활성이다.
- keystore·인증서·profile·환경파일의 Git 제외 규칙과 Kotlin 임시 세션 제외 규칙을 추가했다.
- `tools/archive-ios.sh`, `tools/check_release_artifacts.py`, `plan/kmp-migration/release-builds.md`에 실제 task/scheme과 재현·서명·업로드 전 확인 절차를 추가했다. XR 렌더링/UI·공유 비즈니스 로직은 변경하지 않았다.

## 검증 결과

- Gradle **BUILD SUCCESSFUL**: 280 task 중 251 실행·29 UP-TO-DATE. mobile/xr Release APK·AAB 모두 생성했다. `sign*ReleaseBundle`이라는 task 이름만으로 서명 성공을 판단하지 않고 실제 파일을 검사했다.
- 공통·release 단위 테스트 **74건, 실패/오류/skip 0**. Android domain14/data20, Native domain14/data18, presentation mobile3/xr3, app mobile1/xr1. 최초 릴리스 실행에서 48건은 UP-TO-DATE 결과를 재사용했고 26건은 실행했다. [집계](/Users/yongin/Documents/Codex/2026-11-08/kmp-step-by-step-plan-md/outputs/test-summary.json), [빌드 로그](/Users/yongin/Documents/Codex/2026-11-08/kmp-step-by-step-plan-md/outputs/logs/release-build.log).
- CI의 Java17 환경에서도 동일 전체 명령을 확인했다. [JDK17 로그](/Users/yongin/Documents/Codex/2026-11-08/kmp-step-by-step-plan-md/outputs/logs/jdk17-build.log). 최초 빌드는 Android Studio JBR21.0.9, Kotlin JVM toolchain17이었다. Java17 확인 실행은 Gradle 증분 결과를 재사용했다.
- iOS **ARCHIVE SUCCEEDED**, 실제 `Release`·`generic/platform=iOS` archive다. Kotlin release framework·Swift `-O` 링크를 사용했다. [archive 로그](/Users/yongin/Documents/Codex/2026-11-08/kmp-step-by-step-plan-md/outputs/logs/ios-archive.log).
- Swift 시뮬레이터 앱/테스트 번들 **BUILD SUCCEEDED**. XCTest 실행 통과가 아니다. 기존 XCTest 실행 환경 문제는 8단계 미완료로 유지한다. [Swift 빌드 로그](/Users/yongin/Documents/Codex/2026-11-08/kmp-step-by-step-plan-md/outputs/logs/ios-swift-tests-build.log).
- 23개 서킷/69 PNG 레이어·원본 fixture 정합성 및 XR 에셋 **19/19 통과**. [모바일 에셋](/Users/yongin/Documents/Codex/2026-11-08/kmp-step-by-step-plan-md/outputs/logs/assets.log), [fixture](/Users/yongin/Documents/Codex/2026-11-08/kmp-step-by-step-plan-md/outputs/logs/fixture.log), [XR](/Users/yongin/Documents/Codex/2026-11-08/kmp-step-by-step-plan-md/outputs/logs/xr-assets.log).
- Android APK/AAB 및 iOS archive의 116개 모바일 파일 원본 바이트 일치, 모바일 GLB0/XR DEX 참조0, XR GLB92/모바일 파일0 확인. manifest·Info.plist·서명 검사도 수행했다. [검사 원본](/Users/yongin/Documents/Codex/2026-11-08/kmp-step-by-step-plan-md/outputs/package-checks.json).
- Android 실제 manifest: 두 variant min34/target36, debuggable=false, INTERNET 권한. XR만 `android.software.xr.api.spatial` 필수 및 full-space-managed 설정. 모바일은 XR feature를 요구하지 않는다.
- iOS 실제 Info.plist: 최소17.0, arm64 device, iPhone/iPad, bundle ID/버전 아래와 일치. AppIcon 리소스 미포함과 iPad 방향 설정 경고가 있어 제출 전 해결해야 한다.
- YAML 파싱·shell 문법·Xcode project plist·`git diff --check` 통과. 서명변수 일부 제공 검사는 예상한 오류로 실패하여 불완전 서명을 차단함을 확인했다. [검사 로그](/Users/yongin/Documents/Codex/2026-11-08/kmp-step-by-step-plan-md/outputs/logs/partial-signing-check.log).
- 원격 GitHub CI는 실행하지 않았다. 로컬 검사 결과를 원격 CI 성공으로 표현하지 않는다. 기존 HTTP native 추가 통합 3건·Android 계측·실기기/XR 회귀는 이번 단계에서 재실행하지 않았다.

## 릴리스 산출물과 서명

전부 **unsigned**다. 서명 전에는 설치/스토어 업로드용 배포 후보가 아니다.

- [Android mobile APK](/Users/yongin/Documents/Codex/2026-11-08/kmp-step-by-step-plan-md/outputs/artifacts/app-mobile-release-unsigned.apk): 32,589,030 bytes.
- [Android mobile AAB](/Users/yongin/Documents/Codex/2026-11-08/kmp-step-by-step-plan-md/outputs/artifacts/app-mobile-release-unsigned.aab): 14,582,798 bytes.
- [Android XR APK](/Users/yongin/Documents/Codex/2026-11-08/kmp-step-by-step-plan-md/outputs/artifacts/app-xr-release-unsigned.apk): 76,443,171 bytes.
- [Android XR AAB](/Users/yongin/Documents/Codex/2026-11-08/kmp-step-by-step-plan-md/outputs/artifacts/app-xr-release-unsigned.aab): 32,753,877 bytes.
- [iOS Release xcarchive ZIP](/Users/yongin/Documents/Codex/2026-11-08/kmp-step-by-step-plan-md/outputs/artifacts/CircuitConnection-unsigned.xcarchive.zip): 서명 없는 device archive. IPA export는 하지 않았다.
- [SHA-256 목록](/Users/yongin/Documents/Codex/2026-11-08/kmp-step-by-step-plan-md/outputs/artifacts/SHA256SUMS.txt). 출력 AAB 이름에 `unsigned`를 명시했으며 Gradle 원본 경로에서는 `app-*-release.aab`다.

**Android:** 두 variant 모두 `app.yongin.xr_circuit`, `versionName=1.0`, `versionCode=1`. 프로젝트 keystore0, 명시적 signing 환경변수4개 모두 미제공. APK의 `apksigner verify`는 서명 누락으로 실패했고 AAB는 `jarsigner`가 `jar is unsigned`로 확인했다. 두 AAB의 서명 블록도 없다. 기존 Play 앱/업로드 인증서와의 연결 여부는 확인하지 않았다.

**iOS:** `app.yongin.xr-circuit.connection`, `1.0 (1)`, 개발 팀 미지정. 키체인 `security find-identity -v -p codesigning` 결과 유효 identity0. 로컬 profile4개 존재만 확인했으며 앱과의 일치·유효성을 검증하거나 사용하지 않았다. 실제 archive 앱은 `codesign --verify`에서 `code object is not signed at all`, embedded profile 없음으로 확인했다. 계정·팀을 추정하거나 새 인증서를 발급하지 않았다.

## 완료·남은 항목

- [x] CI 구성과 실제 task/scheme 확인, 재현 문서 작성.
- [x] Android mobile/xr release APK/AAB와 iOS Release archive 로컬 생성.
- [x] 식별자·버전·환경·필수 기능·최소 OS·에셋·release 로그 확인.
- [x] 서명 비밀값 주입 경로 준비 및 실제 unsigned 상태 검증.
- [x] 계획 문서의 8단계 검토 수용 및 9단계 완료 작업 체크 반영.
- [ ] signed APK/AAB·signed archive/IPA 생성 및 인증서/기존 앱과의 일치 검증. **서명 자격 증명 미제공으로 차단.**
- [ ] GitHub Actions 최초 원격 실행 확인. workflow는 로컬 변경 상태이며 푸시하지 않았다.
- [ ] Android 두 variant의 같은 Play 앱 동시 배포 시 고유 versionCode·모바일/XR 중복 기기 타기팅 결정. 현재 두 코드가1이고 모바일은 XR을 명시적으로 제외하지 않는다.
- [ ] 정식 iOS ID/팀 확정 및 AppIcon·iPad 화면 방향 설정 정리.
- [ ] 8단계의 전체 XR 조작·XCTest·물리 기기 성능/접근성·에셋 권리/레이아웃 등 미완료 검증.
- [ ] 10단계 사용자 지시와 정확한 앱/빌드/테스트 트랙 지정. **스토어 업로드 없음.**

단계 요약 체크는 기존 문서 규칙대로 주요 준비 작업 완료를 표시한다. **서명된 배포 후보를 만들 수 있다는 완료 기준은 아직 충족하지 않았고, 완전한 배포 준비 완료나 출시 승인으로 표시하지 않았다.**

자동 승인 검토가 최초 CI의 artifact 업로드 설정을 외부 목적지·전송 내용에 대한 명시적 승인 부족으로 거부했다. 그 기능을 제거했으며, 최종 CI에는 artifact 업로드·스토어 전송이 없다. 산출물은 로컬에 보관했다.

[재현·서명·업로드 전 확인 절차](/Users/yongin/Documents/Codex/2026-11-08/kmp-step-by-step-plan-md/outputs/release-builds.md), [CI 설정 사본](/Users/yongin/Documents/Codex/2026-11-08/kmp-step-by-step-plan-md/outputs/ci/kmp-ci.yml), [갱신 계획](/Users/yongin/Documents/Codex/2026-11-08/kmp-step-by-step-plan-md/outputs/KMP-step-by-step-plan.md). 저장소 기록은 `plan/kmp-migration/stage-9.md`다. **9단계 결과 검토 대기 상태로 멈춘다.**
