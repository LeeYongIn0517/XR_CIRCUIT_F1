# KMP 5단계 결과 — 2D 에셋 규격과 리소스 분리

- 상태: **작업 9/9 완료 / 사용자 검토 대기** (2026-11-01)
- 기준 HEAD: `6474c4200ef6c21f3e6f750f2a875272f6bde813`
- 작업 브랜치: `kmp/stage-3-data` 유지. 시작 시 깨끗한 작업 트리. 커밋·푸시 없음.
- 4단계는 사용자 5단계 실행 지시로 검토 수용. 기존 미검증 항목은 유지.
- **6단계는 실행하지 않았다.** Android flavor·제품 UI 구현은 다음 단계다.

## 구현과 검토 자료

[조작 가능한 미리보기](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-7/outputs/stage5-preview.html)에서 서킷 선택, 섹터/DRS/코너/마커 표시, 정사각·세로·가로 영역, 코너 선택 및 여백 무시를 확인할 수 있다. HTML 파일을 브라우저에서 열면 네트워크 없이 동작한다. 45초 마커는 시각적 루프이며 실시간 차량 위치가 아니다.

- [Silverstone](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-7/outputs/silverstone-stage5.png): 18코너, 3섹터, 2 DRS 구간.
- [Suzuka](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-7/outputs/suzuka-stage5.png): 18코너, 3섹터, 1 DRS 구간. 출발선을 가로지르는 DRS와 입체 교차의 경로 순서 유지.
- 원본은 기존 저장소의 FIA 지도 기반 경로. 임의 생성 이미지를 사용하지 않았다.
- Suzuka의 높이는 2D에서 평면화한다. 교차점은 같은 화면 위치를 공유하지만 경로 인덱스가 다르므로 마커가 다른 길로 점프하지 않는다.
- 코너 위치를 제공하고 선택을 시연한다. 작은 화면에서 모든 코너 이름을 동시에 표시하면 겹칠 수 있다. 제품 UI의 라벨 배치는 6·7단계에서 결정한다.

## 규격 v1

1. `circuitId`는 기존 공통 상세 정보·API 식별자와 연결하는 키다. 기존 catalog의 XR 경로 필드는 호환성을 위해 유지하고, 모바일 표현 정보는 `mobile-assets/tracks/index.json`으로 분리한다.
2. `track.png`, `sectors.png`, `drs.png`는 **1024×1024, RGBA PNG, 투명 배경**이다. 모두 동일 여백·종횡비·투영을 사용한다. 경로 중심선은 최소 8% 여백을 둔다. 선 굵기 때문에 실제 불투명 영역은 8% 경계 바깥으로 일부 확장된다.
3. glTF의 **+X → 화면 오른쪽, +Z → 화면 아래**, Y 높이는 제거한다. 회전 0°, 추가 반전 없음. 두 축에 같은 scale을 사용하고 경로 bounds 중심을 (0.5, 0.5)에 놓는다.
4. `scale = 0.84 / max(xmax−xmin, zmax−zmin)`; `u = (X−centerX)×scale+0.5`, `v = (Z−centerZ)×scale+0.5`. 이 변환 값을 path JSON에 기록한다. 원본 PNG·overlay·좌표를 한 생성기에서 만들며 플랫폼별 보정을 하지 않는다.
5. `path.json`의 512점은 좌상단 원점, x 오른쪽·y 아래, 0~1 정규화다. 순서는 기존 XR 경로의 출발선·주행 방향을 유지한다. 마지막 점에서 첫 점으로 연결되는 폐곡선이다.
6. `progress`는 기존 **3D 경로 누적 거리 / 전체 폐곡선 거리**다. 마지막→첫 점 구간까지 포함해 전체 길이를 계산하고 첫 점 0, 마지막 점 <1로 저장한다. Suzuka의 높이를 포함하는 기존 XR 진행률을 보존한다. `f = (elapsed / 45초) mod 1`; 인접 progress 사이를 선형 보간하며 마지막 구간의 끝 progress는 1로 취급한다.
7. 섹터의 waypoint 범위는 시작 포함·끝 제외, 마지막 끝값 512는 출발선이다. 그림은 선분의 연결 끝까지 그려 seam을 방지한다. DRS의 end ≤ start는 출발선을 경유하는 범위다. DRS는 섹터 위에 더 얇은 녹색 선으로 합성한다.
8. 코너는 기존 상세 JSON의 `turnCode`와 `waypointIndex`를 그대로 사용한다. 각 코너의 x/y는 해당 경로점과 정확히 동일하다.
9. 화면에는 **동일한 aspect-fit**을 한 번 적용한다. `s=min(viewWidth/imageWidth, viewHeight/imageHeight)`, 가운데 정렬 offset을 계산하고 모든 레이어·마커·코너에 공유한다. `TrackViewport`가 순방향/역방향 변환을 제공한다.
10. 터치는 inverse transform 후 letterbox 및 원본의 8% 바깥 여백을 거부한다. 나머지 투명 영역을 트랙으로 간주하지 않는다. 미리보기는 코너 중심에서 화면 24px 이내일 때만 선택한다. 제품 UI는 같은 좌표 변환과 플랫폼의 접근성 터치 단위를 적용한다.

## 가용성과 패키징

- `CircuitAssetTarget.XR` (기존 기본값): 상세 JSON + GLB + XR waypoint가 모두 있어야 한다.
- `CircuitAssetTarget.MOBILE`: 상세 JSON + manifest의 track/sectors/drs PNG + path JSON이 모두 있어야 한다. **GLB는 검사하지 않는다.**
- 잘못된 manifest 버전·중복 ID·경로·크기는 오류로 전달한다. manifest나 필수 파일이 없으면 해당 모바일 서킷을 제공하지 않는다.
- iOS 연결은 기존 두 ID 하드코딩 대신 모바일 가용성 검사를 사용한다. Xcode 빌드가 `data/assets/circuits`와 `mobile-assets/tracks`만 복사한다.
- Android 모바일은 6단계에서 `mobile-assets`를 모바일 source set에 연결한다. 현재 XR 앱에는 이 폴더를 추가하지 않았다. 기존 GLB와 XR source set은 변경하지 않았다.
- 모바일 폴더 및 실제 iOS `.app`의 **GLB 0개**를 확인했다. Android 모바일 APK는 아직 만들지 않았으며, 최종 APK의 XR SDK/GLB 제외는 6단계 검증이다.

## 원본·출처·사용 권한

원본: `presentation/src/main/assets/{silverstone,suzuka}_waypoints.json`, `data/assets/circuits/{silverstone,suzuka}.json`. 편집 가능한 추적 정의: `presentation/src/main/blender/tracks/{silverstone,suzuka}.json`.

각 `provenance.json`에 기존 원본의 source URL, layoutYear, 경로·상세 JSON의 SHA-256, 파생 절차와 권리 상태를 기록했다. 이 작업은 저장소에 이미 있는 지도 추적 경로를 재사용한 것이며, 새로운 지리 측량이나 공식 2026 레이아웃 확인이 아니다. **2025 DRS 레이아웃**이다.

기존 원본에서 재배포를 명시적으로 허용하는 라이선스는 확인하지 못했다. 출처 기재를 사용 허가로 간주하지 않으며, 외부 배포 전 권리 확인은 미완료로 남긴다. 이 단계에서 에셋을 외부 게시·배포하지 않았다.

## 재현 명령

저장소 루트에서 실행한다. 생성/검증 도구에는 Python과 Pillow가 필요하다. 이번 실행은 Codex 번들 Python/Pillow를 사용했다.

```sh
python3 tools/generate_mobile_tracks.py
python3 tools/validate_mobile_tracks.py
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ./gradlew :domain:allTests :data:allTests :app:assembleDebug --console=plain
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ./gradlew :domain:testAndroidHostTest :data:testAndroidHostTest :presentation:testDebugUnitTest :app:testDebugUnitTest --console=plain
xcodebuild -project iosApp/CircuitConnection.xcodeproj -target CircuitConnection -sdk iphonesimulator -configuration Debug SYMROOT="$PWD/build/ios-products" CODE_SIGNING_ALLOWED=NO ARCHS=arm64 build
```

`allTests`는 이번 구성에서 Android host 테스트를 실행하지 않아 별도 명시 호출했다. Xcode scheme + generic destination 방식은 기존 `iOS 18.5 is not installed` 대상 검색 오류가 있어, 이전 단계와 동일한 target 방식으로 빌드했다.

## 검증 결과

- [에셋 검증](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-7/outputs/stage5-assets.log): 두 서킷의 투영 수식, 512점 순서·진행률, 18개 코너 일치, 실제 PNG 알파 정렬, 모든 섹터와 DRS 구간, 원본 해시, GLB 제외 통과.
- [Gradle 빌드·iOS 공통 테스트](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-7/outputs/stage5-build.log): 성공.
- [Android 테스트](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-7/outputs/stage5-android-tests.log): 성공. [최종 집계](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-7/outputs/stage5-test-summary.txt): domain Android 14/iOS 14, data Android 18/iOS 16, presentation 3, app 1 — **66건 통과, 실패·오류·skip 0**.
- [iOS 앱 빌드](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-7/outputs/stage5-ios-build.log): target 방식 성공. 실제 번들의 8개 필수 에셋이 원본과 바이트 단위로 일치, GLB 없음.
- 브라우저 자동 검증: HTML 렌더링, 서킷·aspect 변경, overlay 토글, 여백 터치 거부, JavaScript 오류 없음. [검증 화면](/Users/yongin/Documents/Codex/2026-11-01/kmp-step-by-step-plan-md-7/outputs/stage5-preview-check.png).
- 기존 XR 런타임 에셋 변경 없음. XR APK 빌드 및 관련 단위 테스트로 회귀 확인.

## 남은 확인과 다음 단계

- [ ] 사용자: 이미지 스타일·형상·가독성과 레이어 표시 검토. 승인 전 전체 서킷 확장하지 않음.
- [ ] 외부 배포용 원본/파생 에셋 사용 권한 확인.
- [ ] 물리 기기·서명·Swift XCTest 실행 및 전체 XR 수동 조작: 이전 단계의 미검증 상태 유지.
- [ ] Android/iOS 제품 UI에 실제 렌더러 연결과 기기 화면 검증: 6·7단계 범위.
- 다음 단계: **사용자 지시 대기**.
