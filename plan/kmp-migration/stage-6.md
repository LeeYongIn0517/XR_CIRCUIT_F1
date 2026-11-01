# KMP 6단계 결과 — Android 모바일 2D UI와 XR 빌드 분리

- 상태: **작업 7/7 완료 · 사용자 검토 대기** · 2026-11-01
- 저장소: `/Users/yongin/AndroidStudioProjects/XR_CIRCUIT`
- 기준 HEAD: `fb6218b` · 브랜치: `kmp/stage-3-data` 유지. 시작 시 깨끗한 작업 트리.
- 5단계 검토는 사용자 6단계 실행 지시로 수용했다. 이전 미검증 항목은 면제하지 않았다.
- 커밋·푸시·배포 없음. **7단계 iOS 제품 UI는 실행하지 않았다.**

## 구현

- app과 presentation에 `device` dimension의 `mobile`/`xr` flavor를 적용했다. 동일 Android 공유 코어를 사용하며 XR 전용 KMP 타깃은 만들지 않았다.
- XR 화면·dock·spatial selector·벡터 어댑터는 `presentation/src/xr`로 옮겼다. XR SDK는 `xrImplementation`으로 한정했다. 공유 ViewModel·UI 모델·매퍼·정보 카드는 main에 유지했다.
- 기존 `presentation/src/main/assets` 원본은 생성기·검증기 호환성을 위해 그대로 두고 Gradle에서 XR source set에만 연결했다. 모바일은 `mobile-assets`만 추가한다. 공통 상세 JSON은 두 variant가 공유한다.
- 모바일 가용성은 `CircuitAssetTarget.MOBILE`로 검사한다. 제공 가능한 Silverstone/Suzuka 두 개만 선택 메뉴에 표시한다. XR의 23개 목록 및 3D 경로는 유지한다.
- 모바일에 서킷 선택, 2D 이미지, 섹터/DRS, 45초 시각적 마커, 프로필·랩 기록·날씨, 로딩·오류·재시도, 코너 선택을 구현했다. 마커는 RESUMED 상태에서만 애니메이션하며 실제 레이스 위치가 아니다.
- bitmap·overlay·마커·코너에 같은 TrackViewport를 적용한다. 이미지 읽기·디코딩은 IO dispatcher에서 수행한다. 투명 여백·letterbox를 제외하고 24dp 이내 코너를 선택한다. 별도 48dp 이상 코너 버튼을 제공한다.
- 공유 CircuitStatCard·LapRecordCard를 모바일과 XR 양쪽에서 재사용한다. 모바일 날씨는 큰 글꼴을 위해 유동 높이로 표시하며 XR와 같은 매핑 결과를 사용한다.
- 전체 화면을 스크롤 가능하게 하고 safe drawing inset을 적용했다. 토글은 FlowRow로 줄바꿈한다. 가로 모드 캔버스는 220dp 높이에서 aspect-fit으로 그린다. 화면 전체가 스크롤되므로 작은 가로 화면에서는 트랙까지 스크롤해야 한다.
- 모바일 manifest에는 XR 기능·시작 모드·라이브러리가 없다. XR는 spatial feature를 필수로 선언하고 기존 full-space 시작 property를 유지한다.
- spatial UI가 꺼진 XR 실행에서는 안내 문구와 서킷명을 표시한다. 기존의 빈 화면 분기를 제거했다. 안내는 2D 트랙을 사용하려면 mobile 빌드를 이용하도록 명시한다.
- 기존 미구현 Pins 표시·전략·그리드·리셋은 신규 구현하지 않았고 모바일에 작동하는 컨트롤로 표시하지 않았다.

## 검증

[빌드·공통 테스트 로그](/Users/yongin/Documents/Codex/2026-11-01/5-6-android-2d-ui-xr/outputs/build-tests.log), [최종 모바일 빌드·계측 테스트](/Users/yongin/Documents/Codex/2026-11-01/5-6-android-2d-ui-xr/outputs/mobile-ui-tests.log), [테스트 집계](/Users/yongin/Documents/Codex/2026-11-01/5-6-android-2d-ui-xr/outputs/test-summary.txt).

- 두 debug APK 빌드 성공.
- domain: Android 14 + iOS 14, data: Android 18 + iOS 16, presentation: mobile 3 + xr 3, app: mobile 1 + xr 1. **공통·단위 테스트 70건 통과**. 이전 unflavored stale XML은 집계에서 제외했다.
- Pixel 7 API 34 계측 테스트 **2/2 통과**. 신규 테스트는 서킷 전환, DRS 선택 유지, 마커 OFF, Activity 재생성 후 마커 선택 및 섹터 상태를 확인한다. 다른 1개는 기존 패키지 계측 테스트다.
- 총 **72건 통과**, 실패·오류·skip 0. iOS 공통 테스트 결과에는 Gradle의 UP-TO-DATE 재사용이 포함되며 iOS 제품 UI/XCTest 검증으로 계산하지 않는다.
- [에셋 검증](/Users/yongin/Documents/Codex/2026-11-01/5-6-android-2d-ui-xr/outputs/asset-validation.log): 두 서킷의 512점 투영·PNG 알파 정렬·18코너·섹터/DRS·원본 해시 통과. OS Python에 Pillow가 없어 번들 Python으로 재실행했다.
- [모바일 의존성](/Users/yongin/Documents/Codex/2026-11-01/5-6-android-2d-ui-xr/outputs/mobile-dependencies.log): `androidx.xr` 없음.
- [APK 검사](/Users/yongin/Documents/Codex/2026-11-01/5-6-android-2d-ui-xr/outputs/package-check.json): mobile 34,958,096 bytes, GLB 0, DEX의 `Landroidx/xr/` 참조 0. 8개 필수 이미지/경로 파일은 원본과 바이트 단위 일치. XR 86,397,877 bytes, 기존 GLB 92개·XR 클래스 유지, 모바일 tracks 파일 0.
- [mobile 최종 manifest](/Users/yongin/Documents/Codex/2026-11-01/5-6-android-2d-ui-xr/outputs/mobile-manifest.xml), [XR 최종 manifest](/Users/yongin/Documents/Codex/2026-11-01/5-6-android-2d-ui-xr/outputs/xr-manifest.xml) 확인.
- Pixel 에뮬레이터 실제 화면: 두 서킷, DRS, 실제 날씨, 로컬 프로필·랩 기록 확인. 네트워크 차단 및 `pm clear` 후 날씨 실패를 확인하고, 연결 복원·Retry weather 조작 후 Silverstone 8°C 표시로 복구 확인. 이 값은 실행 시점의 API 값이며 fixture가 아니다.
- 320dp 폭·글꼴 200%에서 토글 줄바꿈 및 스크롤 가능한 날씨 화면 확인. 회전 후 데이터 표시·레이아웃 확인. 접근성 노드의 텍스트·트랙 설명, 토글 selected 상태와 터치 버튼 크기를 점검했다.
- 47초 영상의 1초·23초·46초 프레임을 확인했다. 마커가 경로를 따라 이동하고 45초 차이인 1초/46초 프레임에서 같은 위치 부근으로 돌아오는 것을 관찰했다.
- XR 에뮬레이터 cold start에서 3D 트랙·색상 섹터·프로필·랩 기록·실제 날씨 8°C 표시를 확인했다. full-space-managed 실행 상태이며 AndroidRuntime 앱 크래시 기록 없음. [3D 렌더러 비교](/Users/yongin/Documents/Codex/2026-11-01/5-6-android-2d-ui-xr/outputs/xr-renderer-check.txt): 기존 공간 레이아웃·GLB·overlay·마커 렌더링 부분은 기준 커밋과 바이트 단위로 동일하다.
- `git diff --check` 통과.

## 화면

- [XR 3D 실행 화면](/Users/yongin/Documents/Codex/2026-11-01/5-6-android-2d-ui-xr/outputs/xr-stage6.png)

- [모바일 Silverstone](/Users/yongin/Documents/Codex/2026-11-01/5-6-android-2d-ui-xr/outputs/mobile-silverstone.png)
- [모바일 Suzuka + DRS](/Users/yongin/Documents/Codex/2026-11-01/5-6-android-2d-ui-xr/outputs/mobile-suzuka-drs.png)
- [프로필·실제 날씨](/Users/yongin/Documents/Codex/2026-11-01/5-6-android-2d-ui-xr/outputs/mobile-profile-weather.png)
- [오프라인 오류](/Users/yongin/Documents/Codex/2026-11-01/5-6-android-2d-ui-xr/outputs/mobile-offline.png) / [재시도 복구](/Users/yongin/Documents/Codex/2026-11-01/5-6-android-2d-ui-xr/outputs/mobile-retry.png)
- [320dp·글꼴 200%](/Users/yongin/Documents/Codex/2026-11-01/5-6-android-2d-ui-xr/outputs/mobile-small-largefont-top.png) / [큰 글꼴 날씨](/Users/yongin/Documents/Codex/2026-11-01/5-6-android-2d-ui-xr/outputs/mobile-small-largefont.png)
- [가로 화면에서 전체 트랙](/Users/yongin/Documents/Codex/2026-11-01/5-6-android-2d-ui-xr/outputs/mobile-landscape-track.png)
- [마커 47초 기록](/Users/yongin/Documents/Codex/2026-11-01/5-6-android-2d-ui-xr/outputs/mobile-marker-lap.mp4)

## 재현 명령

저장소 루트에서 Android Studio 번들 JDK를 사용했다. 테스트 도중 두 에뮬레이터를 함께 실행하자 System UI ANR가 발생하여 기기를 하나씩 실행했다. 앱 크래시로 집계하지 않았다.

```sh
export JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home'
./gradlew :app:assembleMobileDebug :app:assembleXrDebug \
  :domain:allTests :domain:testAndroidHostTest :data:allTests :data:testAndroidHostTest \
  :presentation:testMobileDebugUnitTest :presentation:testXrDebugUnitTest \
  :app:testMobileDebugUnitTest :app:testXrDebugUnitTest --console=plain
ANDROID_SERIAL=emulator-5554 ./gradlew :app:connectedMobileDebugAndroidTest --console=plain
./gradlew :app:dependencies --configuration mobileDebugRuntimeClasspath --console=plain
```

APK는 `app/build/outputs/apk/{mobile,xr}/debug/`에 생성된다. 같은 applicationId를 유지하여 같은 기기에 두 variant를 동시에 설치하는 구성은 아니다. 스토어 버전·서명·타기팅 결정은 9단계다.

## 남은 확인

- 물리 Android/XR 기기, TalkBack 실제 음성 탐색, XR 전체 서킷 수동 조작·장시간 부하 검증은 미완료다.
- spatial UI 비활성 fallback의 구현·컴파일을 확인했으며, XR home-space 전환의 실제 조작 검증은 별도로 남긴다.
- release·서명·CI·스토어 업로드는 이번 범위가 아니다. 이전 에셋 재배포 권리 확인과 Swift XCTest 제약도 유지한다.
- 사용자 확인: 모바일 정보 배치·토글·트랙 가독성. 다음 단계는 사용자 지시 대기.

주요 변경 파일: `app/build.gradle.kts`, `app/src/xr/AndroidManifest.xml`, `app/.../RepositoryModule.kt`, `presentation/build.gradle.kts`, `presentation/src/mobile/.../CircuitMobileScreen.kt`, `presentation/src/xr/.../CircuitMainScreen.kt`, 공유 `OverlayModels.kt`·`CircuitUiState.kt`·`CircuitViewModel.kt`, `app/src/androidTestMobile/.../MobileScreenTest.kt`. 상세 코드 위치는 저장소의 동일 모듈 경로를 따른다.
