# XR F1 Live Circuit 아키텍처 및 Hilt 의존 구조

## 목표
- Android XR 환경에서 **실시간 F1 트랙/차량 상태**를 안정적으로 시각화한다.
- 아키텍처는 `Data - Domain - Presentation` 3계층으로 구성한다.
- 의존성 주입은 Hilt로 통일하여 테스트 가능성과 확장성을 확보한다.

## 계층 구조

### 1) Presentation 계층
- 역할: XR 화면 렌더링, 상태 표시, 사용자 상호작용(시점 변경, 정보 토글) 처리.
- 구성:
  - `ViewModel`
  - `UiState`(불변 상태 모델)
  - XR Scene/Renderer 바인딩 레이어(트랙/차량 객체 업데이트)
- 원칙:
  - 데이터 가공 로직 최소화.
  - `UseCase` 호출 결과를 UI 친화 상태로 매핑.

### 2) Domain 계층
- 역할: 비즈니스 규칙과 유스케이스 오케스트레이션.
- 구성:
  - `UseCase` (예: `ObserveLiveCircuitUseCase`, `SelectSessionUseCase`)
  - `Repository` 인터페이스
  - 도메인 모델(예: `DriverState`, `CarTrackPosition`, `TrackMeta`)
- 원칙:
  - Android/XR 프레임워크에 의존하지 않는다.
  - 테스트에서 Data 소스 교체가 쉬워야 한다.

### 3) Data 계층
- 역할: OpenF1 API 통신, DTO 파싱, 캐시, 도메인 모델 변환.
- 구성:
  - `OpenF1ApiService` (REST 클라이언트)
  - Remote Data Source
  - Repository 구현체
  - Mapper (DTO -> Domain)
  - 로컬 캐시(선택: in-memory, 추후 Room 확장 가능)
- 원칙:
  - 외부 API 스키마 변경 영향을 Domain으로 직접 전달하지 않음.
  - 실패/재시도/폴링 전략을 Data에서 캡슐화.

## Hilt 의존 주입 설계

### 컴포넌트 범위
- `SingletonComponent`
  - API 클라이언트, Repository 구현체, 공통 Mapper, Polling Scheduler
- `ViewModelComponent`
  - UseCase, UI용 Mapper

### 바인딩 전략
- `@Module + @InstallIn(SingletonComponent::class)`
  - 네트워크 관련 주입:
    - HTTP 클라이언트
    - JSON 컨버터
    - `OpenF1ApiService`
  - 데이터 소스/저장소 주입:
    - `OpenF1RemoteDataSource`
    - `CircuitRepositoryImpl`
- `@Module + @InstallIn(ViewModelComponent::class)`
  - 유스케이스 주입:
    - `ObserveLiveCircuitUseCase`
    - `ObserveTrackEnvironmentUseCase`
    - `GetSessionContextUseCase`

### 의존 방향
- `Presentation -> Domain -> Data` 단방향 의존.
- Domain은 Data 구현을 모르게 `Repository interface`에만 의존.
- Data는 Domain 모델/인터페이스 구현만 담당.

## 실시간 데이터 흐름(권장)
1. 앱 시작 시 세션 결정:
   - `meetings`, `sessions`로 현재 레이스 세션 식별.
2. 드라이버 컨텍스트 로드:
   - `drivers` 조회로 차량 번호/팀 컬러/약어 확보.
3. 실시간 루프:
   - `location` + `car_data` + `position` 주기 폴링.
   - 필요 시 `intervals`, `weather` 병합.
4. Data 계층에서 도메인 이벤트로 변환 후 스트림 방출.
5. ViewModel이 `UiState`로 병합.
6. XR Renderer가 트랙 위 차량 오브젝트 위치/속도/색상 갱신.

## 오류 처리 및 복원 전략
- API 실패 시:
  - 지수 백오프 재시도.
  - 마지막 정상 상태 유지(stale 표시).
- 지연 증가 시:
  - 렌더 업데이트 주기와 폴링 주기를 분리.
- 데이터 누락 시:
  - `location` 우선, 없으면 `position` 기반 보간.

## 테스트 전략
- Domain:
  - UseCase 단위 테스트(정렬, 필터링, 병합 규칙).
- Data:
  - API 응답 파싱/매핑 테스트.
  - 폴링/재시도 정책 테스트.
- Presentation:
  - ViewModel 상태 전이 테스트.
  - XR 렌더 입력 모델 계산 테스트(좌표 변환/보간).

## 확장 포인트
- 다중 트랙 지원: 트랙 메타데이터 테이블화.
- 히트맵/속도 벡터 시각화: Domain 이벤트만 추가하고 UI에서 표현 확장.
- Replay 모드: 실시간 API 대신 저장된 타임라인 소스 주입.
