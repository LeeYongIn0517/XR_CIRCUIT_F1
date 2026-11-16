# OSM 실제 지도 기반 F1 서킷 표시 계획

- 검토 기준일: 2026-11-15
- 대상: `mobile-assets/tracks/index.json`에 등록된 앱의 23개 서킷
- 목적: 검은 배경의 독립 트랙 도식 대신 실제 지리 지도 위에 서킷 경로와 앱 정보를 겹쳐 보여준다.
- 상태: 조사 및 실행 계획. 이 문서는 앱 코드나 화면을 아직 변경하지 않았다.

## 결론

**가능하다.** 2026-11-15에 OpenStreetMap(OSM) 데이터 API로 앱의 23개 서킷 주변과 관련 relation/way를 확인한 결과, 모든 서킷에서 해당 코스로 판단할 수 있는 OSM 후보 객체를 찾았다. 다만 “객체가 등록되어 있음”만으로 F1의 현재 레이스 경로가 완전하고 정확하다고 볼 수는 없다.

후보를 구성 요소, 연결성, 루프 여부, 길이 및 코스 이름으로 분류한 1차 결과는 다음과 같다.

| 등급 | 개수 | 의미 | 진행 판단 |
|---|---:|---|---|
| A | 17 | 앱 경로 후보가 단일 루프로 구성됨 | 지도에 올리는 기술 검증을 시작할 수 있다. 현재 시즌/피트레인/교차 레벨은 서킷별 검수 필요 |
| B | 5 | 경로가 분기되거나 일부만 매핑된 네트워크 | relation 그대로 그리지 말고 F1 주행 경로를 편집·검수해야 한다 |
| C | 1 | 경로에 공사 태그가 남음 | 준공/공식 개장 전까지 정식 배포 대상에서 보류하거나 임시 표시 |

등급 A도 **FIA/F1 인증, 공식 최신 레이아웃, 트랙 운영자 승인, 법률 검토를 뜻하지 않는다.** OSM은 공동 편집 지도 데이터이며 F1 데이터베이스가 아니다. 또한 지도 위에 OSM 경로를 표시하는 것과 기존 FIA 지도에서 따라 그린 `path.json`의 권리 문제는 별도로 검토해야 한다. OSM 경로로 바꾸면 기존 FIA 유래 파일의 출처·권리 문제가 자동으로 해결되는 것은 아니다.

## 서킷별 OSM 조사 결과

관계 ID 링크는 현재 확인한 대표 후보이다. A/B/C는 **OSM 기하 품질의 1차 분류**이지 콘텐츠 사용 승인 등급이 아니다. OSM 데이터는 계속 편집되므로 구현 전에 다시 확인한다.

| 앱 서킷 | 등급 | 대표 OSM 객체 | 후보 길이 | 검수 메모 |
|---|:---:|---|---:|---|
| Melbourne | A | [relation 280443](https://www.openstreetmap.org/relation/280443) | 5.308 km | 단일 루프. 코너와 시즌 정보는 별도 검수 |
| Shanghai | A | [relation 2094941](https://www.openstreetmap.org/relation/2094941) | 5.459 km | 카트 코스와 구분 필요 |
| Suzuka | A | [relation 284570](https://www.openstreetmap.org/relation/284570) | 5.807 km | 교차부의 bridge/tunnel/layer 정보와 레벨 보존 필요 |
| Miami | B | [relation 20204222](https://www.openstreetmap.org/relation/20204222) | 2.187 km | 현재 relation 하위 way는 2개 분리 구간. 주변 raceway 중 F1 주행 경로 선별 필요 |
| Montreal | A | [relation 284595](https://www.openstreetmap.org/relation/284595) | 4.362 km | 닫힌 단일 way 확인 |
| Monaco | B | [relation 148194](https://www.openstreetmap.org/relation/148194) | 3.388 km | 일반 도로와 raceway가 섞여 분기·끝점 잔존. ePrix 관계와 구별 필요 |
| Barcelona | A | [relation 284540](https://www.openstreetmap.org/relation/284540) | 4.661 km | chicane 없는 레이아웃 선택. 다른 버전/MotoGP 관계 존재 |
| Red Bull Ring | B | [relation 5309181](https://www.openstreetmap.org/relation/5309181) | 4.817 km | 여러 레이아웃 또는 연결 구간이 남아 주 경로 선택 필요 |
| Silverstone | A | [relation 51160](https://www.openstreetmap.org/relation/51160) | 5.881 km | Grand Prix 경로. 중복 member 제거 후 단일 루프 |
| Spa-Francorchamps | A | [relation 284560](https://www.openstreetmap.org/relation/284560) | 6.995 km | 주 코스를 피트레인·카트 코스와 구별. 고저차 별도 |
| Hungaroring | A | [relation 284557](https://www.openstreetmap.org/relation/284557) | 4.367 km | pitlane 제외 시 단일 루프 |
| Zandvoort | A | [relation 13545573](https://www.openstreetmap.org/relation/13545573) | 4.253 km | 뱅킹과 고도는 경로선만으로 표현되지 않음 |
| Monza | A | [relation 284565](https://www.openstreetmap.org/relation/284565) | 5.794 km | 피트레인·옛 오벌·다른 코스 구별 |
| Madring | C | [relation 18813472](https://www.openstreetmap.org/relation/18813472) | 5.442 km | 일부 member에 `construction=raceway`; 앱의 2025 공개 설계와 현행/완공 경로 대조 필요 |
| Baku | A | [relation 11266687](https://www.openstreetmap.org/relation/11266687) | 5.969 km | 일반 도로 member 포함. 경기 방향과 일상 도로 oneway 구별 |
| Sepang | A | [relation 284496](https://www.openstreetmap.org/relation/284496) | 5.546 km | North Track 등과 구별. 앱은 2017 레이아웃임을 표시 |
| Singapore | A | [relation 421263](https://www.openstreetmap.org/relation/421263) | 4.967 km | 앱 표기 길이와 차이가 있어 코너/시즌별 변경 확인 |
| Austin | A | [relation 6537729](https://www.openstreetmap.org/relation/6537729) | 5.502 km | Grand Prix 경로. Short Track 제외 |
| Mexico City | A | [relation 16251935](https://www.openstreetmap.org/relation/16251935) | 4.312 km | Mexican GP 관계. E-Prix 경로 제외 |
| Interlagos | A | [relation 6781071](https://www.openstreetmap.org/relation/6781071) | 4.308 km | 주 코스를 다른 면/관계 객체와 구별 |
| Las Vegas | B | [relation 16696508](https://www.openstreetmap.org/relation/16696508) | 6.323 km | 일반 도로와 평행·대체 구간이 있어 단일 F1 경로 선정 필요 |
| Lusail | B | [relation 21297662](https://www.openstreetmap.org/relation/21297662) | 5.895 km | 주행로 분기/끝점이 남음. 카트 코스와 구별하고 주 경로 편집 필요 |
| Yas Marina | A | [relation 11378665](https://www.openstreetmap.org/relation/11378665) | 5.297 km | Corkscrew 등 다른 레이아웃 제외 |

길이는 relation/way에서 추출한 후보 선형의 기하 길이이며, 앱에 현재 표시되는 서킷 길이나 공인 측량값이 아니다. 일부 앱 길이와 비교해 이상치를 찾는 데만 썼다. 지도 기하 스냅샷 및 검사 결과는 저장소의 [`output/osm-audit/2026-11-15/`](../output/osm-audit/2026-11-15/)에 있다. [기하 미리보기](../output/osm-audit/2026-11-15/geometry-overview.png)에는 등급 A/B/C 분류와 선형 후보를 표시했다. 이 미리보기에는 지도 바탕이 없으므로 지리적 위치 정확성의 시각 검증은 아직 남아 있다.

## 앱에 적용하는 권장 방식

### 지도와 트랙 데이터를 분리한다

1. **지도 바탕:** MapLibre Native를 이용해 도로, 건물, 공원 등이 있는 실제 지도를 표시한다. MapLibre는 Android와 iOS용 Native 엔진을 제공한다. 현재 앱은 Android에서 Compose 기반 화면을 쓰고 iOS는 SwiftUI 앱이므로, 첫 구현에서는 공통 지도 데이터 모델을 만들고 Android/iOS에 각 플랫폼 MapLibre 뷰를 연결하는 방식이 현실적이다. 사용 중인 Compose Multiplatform/SwiftUI 브리지에 맞는 MapLibre 연동은 작은 프로토타입으로 먼저 확인한다.
2. **트랙 경로:** OSM relation/way에서 검수된 GPS 좌표를 추출해 별도 GeoJSON 형태로 관리하고 지도 위 선(overlay)으로 그린다. 시작선, 코너 번호, 섹터, DRS 등 앱 고유 정보는 독립 레이어로 둔다. 좌표계를 GeoJSON 표준 순서인 `[longitude, latitude]`로 통일한다.
3. **자산 출처:** 각 트랙 경로 데이터에 OSM 객체 ID, 버전/수집 시각, 선택한 relation member와 제외 사유, 검수 상태, ODbL 고지를 기록한다. A/B/C 및 “2025 경로” 등의 버전·정확도 표기를 데이터와 UI에서 일치시킨다.
4. **기존 도식:** 현재 검은 배경 트랙 이미지를 지우기 전에, 지도 미지원·오프라인·접근성 상황의 대체 보기로 남길지 검토한다. 지도만 추가하고 기존 이미지를 그대로 번들링하면 해당 이미지의 FIA 자료 출처와 권리 검토는 계속 필요하다.

### OSM 데이터와 지도 타일의 라이선스를 따로 관리한다

- OSM 지리 데이터는 ODbL 1.0이며, 지도 화면에 읽기 쉬운 **“© OpenStreetMap contributors”**와 저작권/라이선스 안내 링크를 보이게 둔다. 데이터베이스를 추출·수정·배포하는 방식은 ODbL의 파생 데이터베이스 및 공유 의무가 생길 수 있으므로, 트랙 좌표를 앱에 번들하기 전 저장 방식과 제공 범위를 검토한다.
- OSM 데이터가 이용 가능하다는 사실은 OSMF의 기본 타일 서버를 무제한 앱 서비스로 이용해도 된다는 뜻이 아니다. `tile.openstreetmap.org` 타일은 사용 정책상 현재 화면에서의 일반적인 상호작용 요청, 명확한 출처 표시, 적절한 User-Agent와 캐싱이 필요하고 대량 다운로드/오프라인 사전 저장은 금지되며 서비스 보장도 없다.
- 배포 전에는 앱의 수익화·예상 사용량·오프라인 요구를 기준으로 **상용 사용 및 오프라인 권한이 명시된 타일 제공자**를 선정하거나 자체 타일 호스팅을 검토한다. 타일 서버/스타일/폰트/위성영상은 각각 별도 조건을 확인하고, OSM 데이터 라이선스와 혼동하지 않는다.
- OSM으로부터 얻은 사실 좌표를 지도 위에 표시해도 다른 권리 문제가 자동으로 없어지지는 않는다. F1, FIA, 서킷 운영자의 상표·공식 로고·공식 지도 표현과 기존 앱 트랙 자산은 별도 검토한다. 특히 현재 `presentation/src/main/blender/tracks/*.json`과 파생 `mobile-assets/tracks/*/path.json`은 FIA 지도 페이지를 시각적으로 따라 그렸다고 기록되어 있으므로, 새 OSM 기반 좌표로 교체할지, 기존 도식은 허가를 받을지, 출시에서 제외할지 결정해야 한다.

## 단계별 구현 계획

### 0단계 — 범위 및 사용 조건 확정

- 수익화 계획, 예상 월간 지도 조회량, 온라인 전용/오프라인 여부, 해외 지도 제공 범위를 확정한다.
- 지도 제공자를 선정하고 Android/iOS SDK, 타일 스타일/폰트/데이터 사용권, attribution UI, 캐싱, API 키 제한, 호출량/요금, 서비스 종료 대비책을 기록한다.
- 공개 OSM 타일을 임시 프로토타입에 쓸 경우에도 실제 앱에서 해당 타일 정책을 충족하도록 하고, 출시 서비스의 기본 가정으로 삼지 않는다.
- ODbL 좌표 데이터를 번들하는 배포 형태와 파생 DB 제공/요청 대응 방식을 검토한다.

**완료 조건:** 선택한 타일 서비스의 양 플랫폼 사용 조건과 비용/제한을 확인했고, 앱의 ODbL 고지 및 데이터 제공 방식을 결정했다.

### 1단계 — 프로토타입 서킷 3개

- A등급 중 Monaco 또는 Singapore 같은 도심형, Suzuka 같은 교차/레벨형, Spa 같은 고저차형을 선정한다.
- 지도 중심점/초기 줌을 저장하고, 경로선이 실제 도로 위치에 맞춰 그려지는지 확인한다.
- 단일 relation을 그대로 쓰지 말고, 관계 member를 추출해 피트레인/카트/구형 구성/중복 way를 제거한다. Suzuka 등 교차 구간에서 지면 레벨이 다른 선을 평면 투영해 생기는 오해도 확인한다.
- Android 모바일과 iOS 기기에서 지도 로딩, 줌/팬, 화면 회전, 저속/오프라인 오류, 화면 읽기, attribution이 서로 가리지 않는지 검토한다.
- 지도 위 F1 경로선과 시작/끝 표시를 먼저 검증한 후 코너·섹터·DRS 오버레이를 추가한다.

**완료 조건:** 지도와 선택한 경로의 정렬이 눈으로 확인되고, attribution이 항상 읽히며, Android/iOS에서 동일한 경로 ID와 표시 상태를 사용한다.

### 2단계 — 공통 데이터 파이프라인 및 23개 경로 검수

- OSM relation ID 기반 수집/변환 스크립트를 만든다. 앱 실행 중 Overpass/API를 반복 질의하지 않고, 검수된 버전 고정 GeoJSON 산출물을 생성한다.
- 각 좌표 데이터에 원본 relation/way ID, OSM version, OSM timestamp, 다운로드 날짜, 변환 도구/규칙, ODbL 출처를 기록한다.
- A 17개를 우선 수동 검수한다. 현재 F1 경로와의 차이, 피트레인/대체 레이아웃, 방향, 교차 레벨, city circuit의 경기 외 도로 연결을 확인한다.
- B 5개(Miami, Monaco, Red Bull Ring, Las Vegas, Lusail)는 지도에서 비교한 뒤 F1 주행 중심선을 수동으로 큐레이션한다. 출처/편집 이력은 남긴다.
- C 1개(Madring)는 `construction=*` 태그와 실제 개장 레이아웃을 확인하고, 공식 운영 상태가 확인될 때까지 미검수 배지 또는 비노출 처리한다.
- 서킷마다 “검수됨/검수 필요/공사 중/시즌 기준” 상태와 마지막 확인일을 명시한다. OSM 수정으로 geometry가 바뀌면 diff 검토를 요구한다.

**완료 조건:** 앱 23개 항목 각각에 선택한 OSM 객체, 정제 규칙, 시즌/기준일, 검수 상태가 있고, 모든 B/C 항목은 승인된 편집 경로 또는 명시적인 비노출 결정이 있다.

### 3단계 — 제품 UI와 접근성

- 서킷 상세 화면의 대표 시각 영역에 실제 지도와 경로선, 시작선, 코너 마커를 표시한다. 지도 위치가 권리/데이터 출처라는 설명은 지도 메타데이터 영역에 간결하게 둔다.
- 지도에서 “지도 보기 / 단순 트랙 도식” 전환을 제공할지 프로토타입으로 결정한다. 단순 도식에도 경로 이미지 권리 확인은 별도로 필요하다.
- 섹터/DRS 표시 토글은 검증된 시즌 데이터만 제공한다. 지도 길이와 공식 랩 길이 또는 앱 데이터 길이를 같은 숫자인 것처럼 혼합하지 않는다.
- 타일 실패, 네트워크 없음, 미검수 경로에 대해서는 적절한 안내와 대체 화면을 제공한다.
- 앱 내 지도 데이터 출처·ODbL 안내, 앱 개인정보처리방침의 지도 SDK/타일 요청에 따른 제3자 통신 설명을 추가한다. 위치 권한이 불필요하면 요청하지 않는다.

### 4단계 — 출시 점검

- Google Play/App Store 제출 패키지에서 이전 FIA 추적 이미지를 포함할지 확인하고, 출처/권리가 미해결인 파일은 제외하거나 허가를 확보한다.
- OSM attribution이 지도 위에 가려지지 않고 작은 화면에서도 읽히는지 확인한다.
- 앱 번들에 포함된 OSM 데이터/GeoJSON에 맞는 ODbL 고지와 소스 제공 경로를 점검한다.
- 타일 제공자의 국가별 커버리지, 데이터 요금, 요청 한도, API 키 보호, 서비스 중단 및 attribution 정책을 재검토한다.
- 최종 지도 경로를 지도 배경과 함께 각 국가/플랫폼에서 육안 검수한다. 실제 권리·약관 검토가 필요한 항목은 배포 전에 한국 법률 전문가 또는 권리자에게 확인한다.

## 저장소 변경 예정 위치

구현을 시작할 때 아래 위치를 기준으로 연결한다. 경로는 현재 트리에서 확인한 후보이며, 실제 파일 분할은 1단계 기술 검증 후 확정한다.

| 영역 | 현재 위치 | 계획된 역할 |
|---|---|---|
| 모바일 트랙 자산 목록 | `mobile-assets/tracks/index.json` | 서킷 ID에 지도 위치와 GeoJSON 경로/검수 상태를 연결 |
| 현재 지도 좌표 경로 | `mobile-assets/tracks/*/path.json` | OSM 버전 정보가 있는 정제 GeoJSON 또는 새 스키마로 교체 검토 |
| 자산 적재 | `data/src/commonMain/.../data/local/MobileTrackManifest.kt` | 지도 중심/경로 메타데이터를 읽고 검증 |
| Android 화면 | `presentation/src/mobile/java/.../screen/CircuitMobileScreen.kt` | MapLibre Android 뷰와 트랙/정보 레이어 표시 |
| iOS 화면 | `iosApp/CircuitConnectionApp.swift` | MapLibre iOS 뷰를 SwiftUI 화면에 연결 |
| 소스 지도 추적 원본 | `presentation/src/main/blender/tracks/*.json` | 기존 FIA 유래 자산 처리 결정과 파이프라인에서의 분리 |

MapLibre는 지도 렌더링 엔진이며 타일 데이터/호스팅을 자동으로 제공하는 서비스가 아니다. 의존성 버전·라이선스·Android/iOS 화면 연결 방식은 프로토타입 단계에서 현재 프로젝트의 Android Compose 및 SwiftUI 구성과 맞춰 결정한다.

## 출시 전 보류 조건

- B/C 서킷이 최종 지도에서 잘못된 갈래·공사 중 경로·카트/피트 구간을 포함한 경우
- OSM/타일 제공자 attribution 또는 ODbL 데이터 고지가 표시되지 않는 경우
- 선택한 타일 서버 약관이 수익화·요청량·오프라인 요구에 맞지 않거나 서비스 가용성 계획이 없는 경우
- OSM에서 받은 경로로 바꿨다는 이유만으로 FIA 지도 추적본/로고/상표 권리 문제까지 해결됐다고 판단한 경우
- 현재 버전과 시즌 표시가 실제 검수 데이터 기준과 다르거나, 2025/2026 구성을 혼동시키는 경우

## 출처

- [OpenStreetMap Copyright and License](https://www.openstreetmap.org/copyright) — ODbL 라이선스 안내
- [OSMF Licence/Attribution Guidelines](https://osmfoundation.org/wiki/Licence/Attribution_Guidelines) — 지도 attribution 요건
- [OSM Foundation Tile Usage Policy](https://operations.osmfoundation.org/policies/tiles/) — 기본 타일 서버의 상호작용, 캐시, 대량/오프라인 제한
- [OSM Foundation Licence FAQ](https://osmfoundation.org/wiki/Licence_and_Legal_FAQ) — Produced Work와 파생 데이터베이스 일반 설명
- [MapLibre Native](https://maplibre.org/projects/native/) — Android/iOS 지도 렌더링 엔진
- [MapLibre Compose](https://maplibre.org/maplibre-compose/) — Android/iOS/Desktop Compose 기반 대안. 현재 앱의 iOS SwiftUI 화면 구조와 호환 여부는 확인 필요
- 로컬 데이터 조사: [`output/osm-audit/2026-11-15/registration-results.json`](../output/osm-audit/2026-11-15/registration-results.json), [`summary.json`](../output/osm-audit/2026-11-15/summary.json), [`manifest.json`](../output/osm-audit/2026-11-15/manifest.json)

이 문서는 기술·제품 검토를 위한 계획이며 법률 자문이나 OSM/권리자의 사용 승인이 아니다.
