# Circuit Info XR 확장 기획

## 개요
- 목표: 3D 서킷 모델을 중심에 두고, 서킷 스펙 / 실시간 날씨 / 스타팅 그리드를 공간 패널로 제공하는 F1 서킷 정보 앱.
- 전제: 자체 서버 없음. 인증 키 불필요한 Open API + 앱 내 정적 자원(Assets)만으로 구성.
- 기반: Android XR, Jetpack Compose(`androidx.xr.compose`), Material 3 XR(`androidx.xr.compose.material3`).
- 기존 문서와의 관계: `F1 Live Circuit.md`가 "실시간 레이스 중계" 방향이라면, 본 문서는 "서킷 정보 탐색(Circuit Explorer)" 방향의 확장 기획.

## 현재 코드베이스 기준선
- 모듈: `:app` / `:domain` / `:data` / `:presentation` (Hilt 골격만 존재, 실사용 없음).
- 구현됨: `CircuitMainScreen`에서 `Subspace` + `SpatialGltfModel`로 Silverstone 트랙 로드, 웨이포인트 기반 차량 마커 랩 애니메이션.
- 정적 자원: `presentation/src/main/assets/SilverstoneTrack.glb`, `CarDotMat.glb`, `silverstone_waypoints.json`.
- 네트워크: `data/network/OpenF1Retrofit.kt`(BASE_URL만 설정, 호출부 없음).
- 미사용: `SpatialPanel`, `Orbiter`, `Volume`, Material 3 XR API, Repository/UseCase/ViewModel 전체.
- 결론: 본 기획은 "신규 개발"이 아니라 기존 XR 데모 위에 정보 계층을 얹는 확장 작업으로 정의한다.

---

## 1. 콘텐츠 아이디어 (XR 공간감 활용)
3D 서킷이 공중에 떠 있는 특성을 활용해 2D 화면 대비 몰입감 있는 정보를 제공한다.

### 1.1 섹터 / DRS 오버레이
- Sector 1·2·3 구간을 색상별로 하이라이트(트랙 상단에 얇은 발광 밴드 형태).
- DRS Zone은 별도 컬러의 레이저 라인 또는 점멸 띠로 시각화.
- 구현 방식: 웨이포인트 인덱스 범위(`startIndex`~`endIndex`)로 구간을 정의하고, 해당 구간에 얇은 세그먼트 메시(또는 소형 GLB 반복 배치)를 생성.

### 1.2 주요 코너 3D 핀 마커
- 예: 스파-프랑코샹 `Eau Rouge`, 몬자 `Parabolica`, 실버스톤 `Maggotts-Becketts`.
- 코너 앵커 좌표에 3D 핀을 띄우고, 포인팅/터치 시 코너 설명 팝업(Spatial Panel 또는 Orbiter 툴팁) 표시.
- 팝업 내용: 코너 번호, 이름, 진입 속도대, 기어, 특징 설명.

### 1.3 고도 변화(Elevation Profile) 및 서킷 특성
- 서킷 최고/최저 해발 고도차(m), 최대 경사도.
- 추월 난이도 지수(1~5), 타이어 마모도 지수(1~5), 풀스로틀 비율(%).
- 표현: 좌측 패널의 미니 라인 차트 + 3D 모델 상 고도 강조 토글.

### 1.4 역대 랩 레코드 3D 핫스팟
- 최단 랩타임 기록 지점(또는 스타트/피니시 라인)에 핫스팟 배치.
- 정보: 랩타임, 드라이버, 팀, 차량, 연도.

### 1.5 피트레인 및 속도 제한 구간
- 피트레인 경로를 별도 색상으로 표시.
- 피트 진입/이탈 지점 마커 + 속도 제한(80km/h 등) 라벨.

---

## 2. 이용 가능한 오픈 API (무료 / 인증 키 불필요)
자체 서버가 없으므로 클라이언트에서 직접 호출 가능한 공개 API를 조합한다.

### 2.1 OpenF1 API
- Base: `https://api.openf1.org/v1/`
- 특징: 최신 F1 라이브/세션 데이터. 드라이버 위치, 스타팅 그리드, 랩타임, 세션 결과 제공.
- 활용: 해당 GP의 스타팅 그리드 및 세션 결과 조회.
- 주요 endpoint: `meetings`, `sessions`, `drivers`, `starting_grid`, `laps`, `weather`.

### 2.2 Jolpica F1 API
- Base: `https://api.jolpi.ca/ergast/f1/`
- 특징: 종료된 Ergast API를 승계한 오픈소스 F1 데이터 API.
- 활용: 역대 레이스 결과, 시즌 일정, 드라이버/컨스트럭터 순위, 그리드 정보.
- 주요 endpoint: `{season}/{round}/results`, `{season}/{round}/qualifying`, `{season}/driverStandings`, `circuits`.
- 주의: Rate limit(비인증 기준 시간당 제한) 존재 → 캐싱 필수.

### 2.3 Open-Meteo Weather API
- Base: `https://api.open-meteo.com/v1/forecast`
- 특징: API 키 없이 위도/경도만으로 실시간 날씨, 기온, 습도, 풍향/풍속, 강수 확률을 JSON 제공.
- 활용: 각 서킷 좌표 기반 실시간 트랙 기상 상태 조회.
- 파라미터 예: `latitude`, `longitude`, `current=temperature_2m,precipitation,weather_code,wind_speed_10m,wind_direction_10m`, `hourly=precipitation_probability`.

### 2.4 API 우선순위 정책
- 스타팅 그리드: OpenF1 우선 → 실패/미제공 시 Jolpica 폴백.
- 드라이버/팀 메타: Jolpica 우선(안정적) → OpenF1 보강(팀 컬러, 차량 번호).
- 날씨: Open-Meteo 단독. 실패 시 OpenF1 `weather`(세션 중일 때만) 폴백.

---

## 3. 정적 / 동적 데이터 분류
서버가 없으므로 변경 주기와 API 의존성에 따라 명확히 분리한다.

### 3.1 정적 데이터 (앱 내 Local Assets / JSON)
| 항목 | 내용 | 보관 위치 |
|---|---|---|
| 3D 모델 | 서킷 GLB 파일 | `presentation/src/main/assets/*.glb` |
| 서킷 기본 정보 | 이름, 국가, 총 길이(km), 코너 개수, DRS 존 개수 | `assets/circuits/{id}.json` |
| 서킷 좌표 | 위도/경도 (날씨 API 호출용) | 위 JSON 내 `location` |
| 코너 상세 | 코너 번호/명칭 + 3D Anchor 좌표 | 위 JSON 내 `corners` |
| 섹터/DRS 구간 | 웨이포인트 인덱스 범위 | 위 JSON 내 `sectors`, `drsZones` |
| 역대 랩 레코드 | 랩타임, 드라이버, 팀, 연도 | 위 JSON 내 `lapRecord` |
| 웨이포인트 | 경로 좌표(기존 포맷 유지) | `assets/{circuit}_waypoints.json` |

### 3.2 동적 데이터 (오픈 API 실시간 호출)
| 항목 | 내용 | 소스 | 갱신 시점 |
|---|---|---|---|
| 실시간 날씨 | 기온, 풍속/풍향, 강수 확률, 날씨 코드 | Open-Meteo | 서킷 선택 시 + 10분 주기 |
| 스타팅 그리드 | 최근/다음 GP 드라이버 출전 순서 | OpenF1 / Jolpica | 서킷 선택 시 |
| 드라이버/팀 정보 | 이름, 팀 컬러, 차량 번호 | Jolpica / OpenF1 | 앱 실행 시 1회(세션 캐시) |

### 3.3 캐싱 정책
- 메모리 캐시(ViewModel 보유) + OkHttp 디스크 캐시 병행.
- TTL: 날씨 10분, 그리드 6시간, 드라이버 메타 24시간.
- 오프라인 시 정적 데이터만으로 화면 구성 가능해야 함(동적 영역은 "정보 없음" 상태 표시).

### 3.4 정적 JSON 스키마 초안
```json
{
  "id": "silverstone",
  "name": "Silverstone Circuit",
  "country": "United Kingdom",
  "lengthKm": 5.891,
  "turns": 18,
  "drsZoneCount": 2,
  "location": { "latitude": 52.0786, "longitude": -1.0169 },
  "characteristics": {
    "elevationMinM": 148,
    "elevationMaxM": 172,
    "overtakingDifficulty": 2,
    "tyreWearIndex": 5,
    "fullThrottlePercent": 71
  },
  "lapRecord": {
    "time": "1:27.097",
    "driver": "Max Verstappen",
    "team": "Red Bull Racing",
    "year": 2020
  },
  "sectors": [
    { "index": 1, "startWaypoint": 0, "endWaypoint": 35, "colorHex": "#E10600" }
  ],
  "drsZones": [
    { "index": 1, "startWaypoint": 88, "endWaypoint": 102 }
  ],
  "corners": [
    {
      "number": 9,
      "name": "Copse",
      "waypointIndex": 30,
      "anchor": { "x": 0.42, "y": 0.02, "z": -0.31 },
      "description": "고속 우코너. 진입 290km/h 부근."
    }
  ],
  "pitLane": {
    "speedLimitKmh": 80,
    "entryWaypoint": 100,
    "exitWaypoint": 6
  }
}
```

---

## 4. 공간 레이아웃 및 Spatial UI 구성
Material 3 XR과 Android XR 패러다임을 활용한 공간 배치.

### 4.1 중앙 3D 공간 (Main Spatial Volume)
- `Subspace` + `SpatialGltfModel`로 GLB 서킷 모델을 공중에 플로팅(기존 구현 재사용).
- 손짓/컨트롤러로 회전, 확대/축소, 틸트 가능.
- 섹터 밴드, DRS 라인, 코너 핀, 랩 레코드 핫스팟을 자식 엔티티로 배치.

### 4.2 좌측 패널 (Left Spatial Panel — Circuit Info)
- 서킷 스펙: 길이, 코너 수, DRS 존 수, 고도차.
- 역대 랩 레코드 카드.
- 코너 정보 리스트(선택 시 중앙 3D 핀 하이라이트 연동).

### 4.3 우측 패널 (Right Spatial Panel — Grid & Weather)
- 상단 카드: 실시간 날씨(아이콘, 기온, 강수 확률, 풍향/풍속).
- 하단 리스트: 스타팅 그리드(순위, 드라이버, 팀 컬러 인디케이터, 차량 번호, 타이어 컴파운드).

### 4.4 하단 툴바 (Bottom Orbiter / Control Dock)
- 서킷 선택 드롭다운.
- 3D 시점 리셋 버튼.
- 섹터 하이라이트 토글, DRS 토글, 코너 핀 토글.

### 4.5 프로토타입 와이어프레임
```text
+-----------------------------------------------------------------------------------+
|                                  [ Spatial Volume ]                               |
|                                                                                   |
|   +-----------------------+      +-----------------------+     +----------------+ |
|   |  [Left Spatial Panel] |      |    [Center Volume]    |     | [Right Panel]  | |
|   |                       |      |                       |     |                | |
|   |  Circuit Specs        |      |      (  3D GLB  )     |     |  Track Weather | |
|   |  - Length: 7.004 km   |      |     / Circuit   \     |     |  24 C / 10%    | |
|   |  - Turns: 20          |      |    (   Model     )    |     +----------------+ |
|   |  - DRS Zones: 2       |      |     \           /     |     | Starting Grid  | |
|   |                       |      |      `---------`      |     |  1. VER        | |
|   |  Lap Record           |      |                       |     |  2. NOR        | |
|   |  - 1:41.252 (LEC)     |      |  *Interactive 3D Pins |     |  3. LEC        | |
|   +-----------------------+      +-----------------------+     +----------------+ |
|                                                                                   |
|                                +-------------------+                              |
|                                |   [Bottom Dock]   |                              |
|                                | [Select Circuit]  |                              |
|                                +-------------------+                              |
+-----------------------------------------------------------------------------------+
```

### 4.6 비XR(Home Space) 폴백
- `LocalSpatialCapabilities.isSpatialUiEnabled == false`인 경우 2D 스크롤 화면으로 대체.
- 구성: 상단 서킷 이미지 → 스펙 → 날씨 → 그리드 순 단일 컬럼.
- 현재 코드는 비XR에서 빈 화면이므로 이 폴백 구현이 필수.

---

## 5. 기술 스택
- UI: Jetpack Compose + `androidx.xr.compose` + Material 3 XR(`androidx.xr.compose.material3` 1.0.0-alpha17, 의존성 추가 완료).
- 3D 렌더링: `androidx.xr.scenecore` 기반 `SpatialGltfModel`(현재 구현 방식 유지). SceneView/Filament는 커스텀 셰이더가 필요해질 경우에만 검토.
- 네트워크: Retrofit2 + OkHttp(기존 `OpenF1Retrofit` 확장) + Kotlinx Serialization으로 컨버터 통일(현재 Gson → 교체 검토).
- DI: Hilt(현재 의존성만 존재 → 실제 모듈 작성 필요).
- 상태 관리: ViewModel + `StateFlow` 기반 UiState.

---

## 6. 계층별 설계 방향
- `:domain`
  - 모델: `Circuit`, `CircuitCorner`, `Sector`, `DrsZone`, `LapRecord`, `TrackWeather`, `GridEntry`, `Driver`.
  - UseCase: `GetCircuitDetailUseCase`, `GetTrackWeatherUseCase`, `GetStartingGridUseCase`.
  - Repository 인터페이스 정의(구현은 `:data`).
- `:data`
  - `CircuitLocalDataSource`(assets JSON 파싱), `OpenF1ApiService`, `JolpicaApiService`, `OpenMeteoApiService`.
  - DTO → Domain 매퍼, 폴백/캐시 정책 포함 Repository 구현.
  - Hilt 모듈: `NetworkModule`, `RepositoryModule`.
- `:presentation`
  - `CircuitViewModel` + `CircuitUiState`(정적/동적 섹션별 로딩·에러 상태 분리).
  - `CircuitMainScreen`(중앙 볼륨) / `CircuitInfoScreen`(좌측 패널, 현재 빈 스텁) / `GridWeatherScreen`(우측 패널) / `CircuitControlDock`(하단).
  - `:presentation`이 현재 `:domain`만 참조하므로 `:data` 주입 경로를 `:app`에서 연결.

---

## 7. 구현 Phase

### Phase 1. 정적 데이터 스키마 및 로더 (1~1.5일)
- 작업:
  - `CircuitDetail` JSON 스키마 확정 및 Silverstone 데이터 작성
  - assets 로더 + Kotlinx Serialization 파싱 구현
  - Domain 모델/매퍼 정의
- 완료 기준:
  - 앱 실행 시 Silverstone 정적 정보가 모델 객체로 로드됨
  - 스키마 누락 필드에 대한 기본값 처리 검증

### Phase 2. Spatial 패널 골격 (1.5~2일)
- 작업:
  - `SpatialRow` 기반 좌/중앙/우 3분할 배치
  - `SpatialPanel`로 좌측 서킷 정보, 우측 그리드/날씨 패널 뼈대 구현
  - `Orbiter`로 하단 컨트롤 독 배치
  - 비XR 2D 폴백 화면 구현
- 완료 기준:
  - 헤드셋/에뮬레이터에서 3패널 + 독이 정상 표시
  - 비XR 환경에서 빈 화면 없이 2D 화면 표시

### Phase 3. Open-Meteo 날씨 연동 (1일)
- 작업:
  - `OpenMeteoApiService` + Repository + UseCase 구현
  - 서킷 좌표 기반 호출, 10분 TTL 캐시
  - 우측 상단 날씨 카드 바인딩(아이콘 매핑 포함)
- 완료 기준:
  - 서킷 선택 시 실제 기상 데이터 표시
  - 네트워크 오프라인 시 에러 상태 카드로 대체

### Phase 4. 스타팅 그리드 연동 (1.5~2일)
- 작업:
  - OpenF1 `sessions` / `starting_grid` / `drivers` 연동
  - Jolpica 폴백 경로 구현
  - 팀 컬러/차량 번호 매핑 및 그리드 리스트 UI 바인딩
- 완료 기준:
  - 최근 GP 그리드가 순위대로 표시
  - OpenF1 실패 시 Jolpica 결과로 자동 대체

### Phase 5. 3D 오버레이 (섹터 / DRS / 코너 핀) (2~3일)
- 작업:
  - 웨이포인트 인덱스 범위 → 3D 세그먼트 변환 로직
  - 섹터 밴드, DRS 라인 렌더링
  - 코너 3D 핀 + 선택 시 설명 팝업
  - 하단 독 토글과 연동
- 완료 기준:
  - 토글 시 오버레이가 즉시 반영되고 트랙 선형과 정렬됨
  - 코너 핀 선택 → 좌측 리스트 항목 하이라이트 양방향 연동

### Phase 6. 랩 레코드 / 피트레인 / 고도 프로파일 (1.5~2일)
- 작업:
  - 랩 레코드 핫스팟 배치
  - 피트레인 경로 및 속도 제한 라벨
  - 고도 프로파일 미니 차트(좌측 패널)
- 완료 기준:
  - 3개 요소가 정적 데이터만으로 동작
  - 데이터 미보유 서킷에서 해당 섹션 자동 숨김

### Phase 7. 다중 서킷 확장 및 안정화 (2~3일)
- 작업:
  - 서킷 선택 드롭다운 + GLB/JSON 동적 스위칭
  - 2~3개 서킷 데이터 추가
  - 메모리/프레임 프로파일링, GLB 언로드 처리
  - 계층별 테스트 작성
- 완료 기준:
  - 서킷 전환 시 누수 없이 모델 교체
  - 주요 유스케이스 회귀 테스트 통과

---

## 8. 리스크 및 대응
- Open API rate limit / 서비스 중단:
  - 대응: TTL 캐시, 폴백 소스, 정적 데이터 단독 동작 보장
- 좌표계 불일치(Blender Z-up ↔ glTF Y-up):
  - 대응: 기존 `waypoints_gltf_yup` 규약 유지, 코너 앵커도 동일 좌표계로 작성
- XR 라이브러리 alpha 버전 API 변경:
  - 대응: 버전 고정, 업그레이드 시 스파이크 브랜치에서 검증
- 오버레이 다수 렌더 시 성능 저하:
  - 대응: 세그먼트 인스턴싱, 토글 기본값 최소화, LOD 적용
- 서킷별 정적 데이터 제작 공수:
  - 대응: 스키마 필드 대부분 옵셔널 처리, 서킷 단위 점진 추가

---

## 9. 완료 정의(Definition of Done)
- Silverstone 기준 좌/중앙/우 3패널 + 하단 독 공간 레이아웃 동작
- 정적 서킷 정보(스펙, 코너, 랩 레코드) 전량 표시
- Open-Meteo 실시간 날씨, OpenF1/Jolpica 스타팅 그리드 표시
- 섹터/DRS/코너 핀 오버레이 토글 동작
- 비XR 환경 2D 폴백 화면 제공
- 오프라인 상태에서 정적 데이터만으로 크래시 없이 동작
- 서킷 2개 이상 전환 가능
