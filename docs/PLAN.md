# Assemble.java 리팩토링 계획

> 진행 원칙: **Agentic Engineering** — 각 단계를 완료하고 검토한 뒤 다음 단계로 넘어간다.
> 리팩토링은 동작을 바꾸지 않는다. 테스트가 통과하면 코드를 바꿀 수 있다.

---

## 1단계 — Explore (코드 스멜 탐색) ✅

> 결과: [docs/EXPLORE.md](EXPLORE.md) 참고

### 체크리스트

- [x] Code Smell 7건 코드에서 직접 확인
- [x] `isValidCheck()`와 `testProducedCar()`의 중복 규칙이 정확히 일치하는지 비교
- [x] 고장난 엔진이 `testProducedCar()`에서 검사되지 않는 버그(BUG-01) 확인
- [x] 부품 추가 시 수정 필요 위치 7곳 파악 (CS-04)
- [x] `stack[4]` 미사용 확인 (CS-01)
- [x] `BROKEN = 4` 상수 미선언 확인 (CS-05)
- [x] regression 기준 규칙 목록 R1~R7 작성
- [x] BUG-01 → Action 단계에서 FAIL로 수정하기로 결정
- [x] Plan 단계 진행 승인

---

## 2단계 — Plan (설계) ✅

> 목표: 리팩토링 후 구조를 확정한다. 코드를 작성하기 전에 반드시 검토한다.

### 클래스 구조

```
Assemble.java          ← main() 한 줄: new AssemblyWizard().run()
AssemblyWizard.java    ← 단계 흐름 제어, 입력 루프
Car.java               ← 선택된 부품 조합 Value Object (불변)
CarValidator.java      ← 호환성 규칙 단일 진입점 (CS-03, BUG-01 해결)
ConsoleMenu.java       ← 메뉴 출력 전담 (SI-01 해결)

enum CarType   ← SEDAN, SUV, TRUCK
enum Engine    ← GM, TOYOTA, WIA, BROKEN  (CS-05 해결)
enum Brake     ← MANDO, CONTINENTAL, BOSCH
enum Steering  ← BOSCH, MOBIS
```

### 핵심 설계 결정

| 결정 | 해결하는 문제 |
|------|--------------|
| 부품을 enum + `displayName` 필드로 표현 | CS-05 (Magic Number), CS-06 (Data Clumps) |
| `CarValidator` 단일 책임 클래스 분리 | CS-03 (Duplicated Code), BUG-01 |
| `Car`를 불변 VO로 설계 | SI-02 (static 전역 상태 제거) |
| `Step` enum으로 단계 흐름 표현 | CS-02 (God Method), CS-07 (네이밍) |
| `ConsoleMenu`로 메뉴 출력 분리 | SI-01 (검사+출력 혼재 제거) |

### enum 설계 — `displayName` 필드로 이름 매핑 내재화

```java
public enum Engine {
    GM("GM"), TOYOTA("TOYOTA"), WIA("WIA"), BROKEN("고장난 엔진");

    public final String displayName;
    Engine(String displayName) { this.displayName = displayName; }
}
// CarType, Brake, Steering도 동일한 패턴
```

### CarValidator 설계

```java
public class CarValidator {
    // Run / Test 모두 이 메서드 하나를 사용
    public boolean isValid(Car car) { ... }

    // Test 출력용 — 실패 이유 반환, 없으면 null
    public String getFailReason(Car car) { ... }
}
```

- `isValid()` + `getFailReason()` 모두 **고장난 엔진 검사 포함** (BUG-01 수정)
- `isValidCheck()` / `testProducedCar()` 중복 제거 (CS-03 해결)

### Step enum 설계

```java
enum Step { CAR_TYPE, ENGINE, BRAKE, STEERING, RUN_TEST }
```

- 현재 `int step` + `CarType_Q = 0` 등 int 상수를 대체
- `step--` 뒤로가기 로직을 `Step.previous()` 메서드로 캡슐화

### 유닛 테스트 설계 — `CarValidatorTest`

| 테스트 메서드 | 검증 규칙 |
|---|---|
| `sedan_continental_불가` | R1 |
| `suv_toyota_불가` | R2 |
| `truck_wia_불가` | R3 |
| `truck_mando_불가` | R4 |
| `bosch_brake_비bosch_steering_불가` | R5 |
| `broken_engine_불가` | R6 + R7 (BUG-01 수정) |
| `bosch_brake_bosch_steering_가능` | R5 경계 |
| `유효한_조합_통과` | 정상 케이스 |

### 체크리스트

- [x] 8개 클래스/enum 구조가 납득된다
- [x] `CarValidator.getFailReason()`이 실패 이유를 문자열로 반환하는 설계가 적절하다
- [x] `Step` enum으로 단계 흐름을 표현하는 방식이 적절하다
- [x] BUG-01 수정 방향 (고장난 엔진 → `CarValidator.isValid()` false + `getFailReason()` 반환)이 맞다
- [x] 테스트 8개가 R1~R7 regression을 충분히 커버한다
- [x] **[사용자 확인]** Action 단계 진행 승인

---

## 3단계 — Action (리팩토링 실행) ✅

> 결과: [docs/ACTION.md](ACTION.md) 참고

### 체크리스트

- [x] Step A — `CarValidatorTest` 8개 테스트 작성 및 통과
- [x] Step B — enum 5개 추출 (`CarType`, `Engine`, `Brake`, `Steering`, `Step`)
- [x] Step C — `Car` VO 추출, `static int[] stack` 제거
- [x] Step D — `CarValidator` 추출, BUG-01 수정
- [x] Step E — `ConsoleMenu`, `AssemblyWizard` 분리, `Assemble` 정리
- [x] 전체 유닛 테스트 통과 (`./gradlew test`)
- [x] 기존 동작과 동일하게 작동하는지 수동 확인
- [x] `static int[] stack` 전역 상태 완전히 제거 확인
- [x] BUG-01 수정: 고장난 엔진 선택 시 Test가 FAIL 반환 확인
- [x] 출력 메시지(한국어)가 리팩토링 전과 동일한지 확인
- [x] **[사용자 확인]** Commit 단계 진행 승인

---

## 4단계 — Commit (검토 및 마무리) ✅

### 동작 동등성 확인

- [x] Regression 시나리오 8개 직접 실행 결과 일치
- [x] 뒤로가기(0번 입력) 흐름이 모든 단계에서 정상 동작
- [x] `exit` 입력 시 정상 종료

### 코드 품질 확인

- [x] 각 클래스가 단일 책임(SRP)을 가진다
- [x] `CarValidator`가 유일한 유효성 검사 진입점이다
- [x] 테스트가 public 인터페이스를 검증한다
- [x] 매직 넘버가 코드에 남아 있지 않다

### 확장성 확인

- [x] 새 차량 타입 추가 시 `CarType` enum + `CarValidator` 규칙 추가만으로 가능한 구조

### 커밋 구성

- [x] 커밋이 논리적 단위로 분리되어 있다
- [x] **[사용자 확인]** 전체 리팩토링 완료 승인