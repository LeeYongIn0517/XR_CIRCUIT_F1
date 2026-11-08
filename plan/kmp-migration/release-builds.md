# KMP 릴리스 재현 절차 — 9단계

이 절차는 로컬 빌드·검사만 수행한다. Play/App Store/TestFlight 업로드·심사 제출·계정 변경·프로비저닝 갱신은 포함하지 않는다.

## 기준과 환경

- 기준 커밋 `ba9c0a4`, 작업 브랜치 `kmp/stage-3-data`.
- Gradle wrapper 8.13, Kotlin 2.2.20, AGP 8.11.1, Kotlin JVM toolchain 17. 최초 로컬 Gradle JVM은 Android Studio JBR 21.0.9, CI는 JDK17로 구성했다.
- Android SDK 36, build-tools 35.0.0. minSdk 34(Android 14), targetSdk 36.
- Apple Silicon Mac, Xcode 16.4(16F6), iPhoneOS/iPhoneSimulator 18.5 SDK, iOS 최소 17.0. 이 저장소의 Native 시뮬레이터 타깃은 arm64다.
- 에셋 검사: Python 3.12 + `Pillow==11.0.0`(가상환경 권장).
- `./gradlew :app:tasks --all`과 `xcodebuild -list -project iosApp/CircuitConnection.xcodeproj`에서 아래 task와 `CircuitConnection` scheme을 실제 확인했다.

## 로컬 검사·빌드

저장소 루트에서 실행한다. JAVA_HOME은 해당 머신의 JDK 17 경로로 지정한다. 최초 로컬 빌드에는 Android Studio JBR21을 사용했다.

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
python3 tools/validate_mobile_tracks.py
python3 tools/generate_circuit_regression_fixture.py --check
python3 -m unittest discover -s presentation/src/main/blender/tests -p 'test_*.py'
./gradlew :domain:testAndroidHostTest :data:testAndroidHostTest :domain:iosSimulatorArm64Test :data:iosSimulatorArm64Test :presentation:testMobileReleaseUnitTest :presentation:testXrReleaseUnitTest :app:testMobileReleaseUnitTest :app:testXrReleaseUnitTest --console=plain
./gradlew :app:assembleMobileRelease :app:assembleXrRelease :app:bundleMobileRelease :app:bundleXrRelease --console=plain
xcodebuild -project iosApp/CircuitConnection.xcodeproj -target CircuitConnectionTests -sdk iphonesimulator -configuration Debug SYMROOT="$PWD/build/ios-products" CODE_SIGNING_ALLOWED=NO ARCHS=arm64 build
bash tools/archive-ios.sh "$PWD/build/release/CircuitConnection-unsigned.xcarchive"
python3 tools/check_release_artifacts.py --android --archive "$PWD/build/release/CircuitConnection-unsigned.xcarchive" --output "$PWD/build/release/package-checks.json"
```

Swift 번들 `build`는 XCTest 실행 성공을 뜻하지 않는다. 기존 로컬 런타임 대상 검색 문제와 실기기 회귀는 8단계 잔여 항목으로 유지한다. Native 공통 테스트 실행과 Swift 테스트 실행을 구분한다.

## 산출물과 식별자

- `app/build/outputs/apk/mobile/release/app-mobile-release-unsigned.apk`
- `app/build/outputs/apk/xr/release/app-xr-release-unsigned.apk`
- `app/build/outputs/bundle/mobileRelease/app-mobile-release.aab`
- `app/build/outputs/bundle/xrRelease/app-xr-release.aab`
- `build/release/CircuitConnection-unsigned.xcarchive`

Android 두 variant는 기존 동일 `app.yongin.xr_circuit`, versionName `1.0`, versionCode `1`을 유지한다. 두 산출물을 동일 Play 앱에 함께 업로드할 버전 코드·기기 타기팅은 확정하지 않았다. 모바일은 XR 기기를 명시적으로 제외하지 않으므로 동시 배포 전 중복 타기팅과 고유 버전 코드를 정해야 한다. 기존 Play 앱 버전/인증서와의 일치 여부는 계정에서 확인하지 않았다.

iOS는 기존 `app.yongin.xr-circuit.connection`을 유지하고 `MARKETING_VERSION=1.0`, `CURRENT_PROJECT_VERSION=1`을 명시했다. 정식 App Store 앱 ID로 확정·등록한 것이 아니다. 개발 팀은 미지정이다. iOS 앱 아이콘 리소스가 아직 없으므로 App Store 제출 준비 완료가 아니다.

Release는 Android debuggable=false, iOS Swift `-O`/Kotlin release framework를 사용한다. Android minify는 기존 설정대로 false다. Android HTTP BODY 로그는 FLAG_DEBUGGABLE 조건으로 release에서 꺼지며, XR 선택 로그는 제거했다. API는 기존 HTTPS endpoint를 유지한다. 별도 staging endpoint는 없다.

## 서명

Android는 아래 **네 환경변수를 모두 제공한 경우에만** release signingConfig를 연결한다. 일부만 제공하면 빌드를 중단하며, 전부 없으면 unsigned를 생성한다. debug key로 자동 대체하지 않는다.

- `ANDROID_KEYSTORE_PATH`: 안전한 저장소에서 로컬/CI 임시 경로로 주입한 keystore의 절대 경로.
- `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`.

비밀값은 명령행·문서·Git에 적지 않는다. CI 서명이 필요해지면 보호된 GitHub Environment의 secrets와 임시 파일을 사용하고 빌드 후 파일을 제거한다. 현재 workflow는 signing secrets를 요구하거나 읽지 않으며 unsigned 검사 전용이다. keystore·p12·profile·`.env`는 gitignore에 추가했다. 새 키 발급·기존 키 교체는 수행하지 않았다.

서명된 APK는 `apksigner verify --verbose --print-certs`, AAB는 `jarsigner -verify -verbose -certs`로 확인하고 등록된 업로드 인증서와 비교한다. AAB의 jarsigner 프로세스 종료 코드만으로 서명 유효성을 판정하지 않는다(`jar is unsigned`일 때도 종료 코드 0이 될 수 있다). Android APK는 서명이 있어야 설치 가능하고 AAB는 Play 업로드 전에 업로드 키 서명이 필요하다. [Android 공식 서명 문서](https://developer.android.com/studio/publish/app-signing).

iOS unsigned archive는 설치/업로드 가능한 IPA가 아니다. 서명된 후보 생성에는 확정 앱 ID·팀·유효한 배포 인증서/개인키·앱과 일치하는 provisioning profile이 필요하다. 이번에는 `CODE_SIGNING_ALLOWED=NO`를 사용했다. `-allowProvisioningUpdates`, `-exportArchive`, 업로드 도구는 실행하지 않았다.

## CI

`.github/workflows/kmp-ci.yml`: push/PR/manual 검사. `contents: read`, job timeout, 중복 실행 취소를 설정했다.

- Ubuntu 24.04/JDK17: 에셋·fixture·XR 에셋 검사, Android 공통 및 두 release variant 단위 테스트, APK/AAB 빌드.
- `macos-15`/Apple Silicon/Xcode16.4: Native 공통 테스트, Swift 테스트 번들 컴파일, unsigned device archive.
- signing secret·스토어 API·자동 export·산출물 업로드 action 없음. 산출물은 실행 머신에만 생성된다. workflow 로그는 일반 GitHub Actions 실행 로그로 남는다.

macOS runner의 arm64와 Xcode16.4 포함 여부는 공식 [runner 목록](https://docs.github.com/en/actions/reference/runners/github-hosted-runners), [macOS15 이미지 명세](https://github.com/actions/runner-images/blob/main/images/macos/macos-15-arm64-Readme.md)로 확인했다. runner 이미지 변경으로 고정 Xcode가 없어지면 CI를 실패시키고 검토한다. 자동 최신 버전 대체는 하지 않는다.

이 단계에서는 커밋·원격 푸시·원격 Actions 실행을 하지 않았다. 로컬 검증 성공을 원격 CI 성공으로 표시하지 않는다. 초기 artifact 업로드 설정은 자동 승인 검토가 외부 목적지·전송 내용의 미승인을 이유로 거부하여 제거했고, 최종 CI에는 외부 산출물 전송 기능이 없다.

## 업로드 전 미완료 항목

- [ ] 실제 업로드 키·기존 앱 인증서 일치와 Android signed APK/AAB 검증.
- [ ] Android 동일 앱의 두 variant 버전 코드·중복 기기 타기팅 및 기존 Play 버전 확인.
- [ ] 정식 iOS 앱 ID·버전·개발 팀 확정, 배포 인증서·profile로 signed archive/IPA 생성·검증, 앱 아이콘 및 iPad 화면 방향 Info.plist 설정 준비.
- [ ] 원격 GitHub Actions 최초 실행 결과 확인.
- [ ] 8단계의 XCTest·목표 물리 기기·접근성·XR 전체 조작·에셋 권리/레이아웃 검토 등 출시 차단 항목 해결.
- [ ] 사용자의 별도 10단계 지시와 정확한 앱·빌드·테스트 트랙 지정.

이번 단계의 unsigned 산출물을 배포 가능한 서명 완료 후보로 취급하지 않는다.
