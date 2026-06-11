# Assemble.java 리팩토링 계획

> 진행 원칙: **Agentic Engineering** — 각 단계를 완료하고 검토한 뒤 다음 단계로 넘어간다.
> 리팩토링은 동작을 바꾸지 않는다. 테스트가 통과하면 코드를 바꿀 수 있다.

---

## 1단계 — Explore (코드 스멜 탐색)

> 목표: 현재 코드에서 문제점을 빠짐없이 식별한다. 아직 아무것도 바꾸지 않는다.

### Code Smell 목록 (탐색 대상)

| # | 위치 | 스멜 유형 | 설명 |
|---|------|-----------|------|
| 1 | `static int[] stack` | Primitive Obsession | 부품 선택값을 int 배열로 관리 — 타입 안전성 없음 |
| 2 | `main()` 전체 | God Method | 흐름 제어, 입력 처리, 출력, 검증이 한 메서드에 혼재 |
| 3 | `isValidCheck()` / `testProducedCar()` | Duplicated Code | 동일한 유효성 규칙이 두 메서드에 중복 구현 |
| 4 | `selectCarType` 등 4개 메서드 | Divergent Change | 부품 추가 시 상수, 메뉴 출력, select, 이름 배열을 모두 수정해야 함 |
| 5 | `CarType_Q = 0` 등 int 상수 | Magic Number | 단계와 부품 종류가 모두 int로 표현 — enum 부재 |
| 6 | `runProducedCar()` 내 이름 배열 | Data Clumps | 부품 이름 매핑이 배열/삼항연산자/상수로 분산 |
| 7 | 전체 | 테스트 없음 | 핵심 규칙(호환성 검사)에 대한 자동화 테스트 전무 |

### 체크리스트

- [ ] 위 7개 Code Smell을 코드에서 직접 확인했다
- [ ] `isValidCheck()`와 `testProducedCar()`의 중복 규칙이 정확히 일치하는지 비교했다
- [ ] "고장난 엔진(4번)"이 `testProducedCar()`에서 검사되지 않는 버그를 확인했다
- [ ] 부품이 추가될 경우 수정이 필요한 위치를 모두 파악했다 (확장성 취약 지점)
- [ ] coverage 측정 기준점을 정했다 — 리팩토링 전 동작 기준 시나리오 목록 작성

---

## 2단계 — Plan (설계)

> 목표: 리팩토링 후 구조를 확정한다. 코드를 작성하기 전에 반드시 검토한다.

### 클래스 설계

```
src/
├── Assemble.java          ← main() 진입점만 남김
├── AssemblyWizard.java    ← 단계 흐름 제어 (현재 main() 루프)
├── Car.java               ← 선택된 부품 조합 (Value Object)
├── CarValidator.java      ← 호환성 검사 규칙 단일화 (현재 중복 로직)
├── ConsoleMenu.java       ← 메뉴 출력 전담
└── enums/
    ├── CarType.java       ← SEDAN, SUV, TRUCK
    ├── Engine.java        ← GM, TOYOTA, WIA, BROKEN
    ├── Brake.java         ← MANDO, CONTINENTAL, BOSCH
    └── Steering.java      ← BOSCH, MOBIS
```

### 핵심 설계 결정

| 결정 | 이유 |
|------|------|
| 부품을 enum으로 표현 | int → 타입 안전, 이름 매핑 내재화, 확장 용이 |
| `CarValidator`를 단일 책임 클래스로 분리 | 중복 제거 + 단위 테스트 대상 명확화 |
| `Car`를 불변 VO로 설계 | static 전역 상태 제거 |
| 메뉴 출력을 `ConsoleMenu`로 분리 | UI 변경이 로직에 영향을 주지 않도록 |

### 유닛 테스트 설계 (리팩토링 내성 확보)

테스트 대상: `CarValidator` (핵심 비즈니스 규칙 regression)

```
CarValidatorTest
├── sedan_continental_brake_불가
├── suv_toyota_engine_불가
├── truck_wia_engine_불가
├── truck_mando_brake_불가
├── bosch_brake_비_bosch_steering_불가
├── bosch_brake_bosch_steering_가능
└── 모든_유효_조합_통과
```

### 체크리스트

- [ ] 클래스 구조가 CLAUDE.md의 리팩토링 방향과 일치하는지 확인했다
- [ ] enum 각각의 멤버가 현재 int 상수와 1:1 대응되는지 검증했다
- [ ] `CarValidator`가 `isValidCheck()`와 `testProducedCar()` 두 곳의 규칙을 모두 포함하는지 확인했다
- [ ] "고장난 엔진" 처리를 어느 클래스 책임으로 둘지 결정했다 (`Engine.BROKEN` vs. `CarValidator`)
- [ ] 테스트 케이스 목록이 제한조건 1·2를 빠짐없이 커버하는지 확인했다
- [ ] 기존 출력 메시지(한국어)가 변경 없이 유지되는지 확인했다

---

## 3단계 — Action (리팩토링 실행)

> 목표: 설계대로 코드를 작성한다. 동작은 바꾸지 않는다.
> 순서: **테스트 먼저 → 리팩토링 → 테스트 통과 확인** (각 소단계마다 반복)

### 실행 순서

#### Step A. 테스트 작성 (리팩토링 전 안전망 확보)
1. `CarValidatorTest.java` 작성 — 현재 규칙 기준으로 실패 케이스 먼저 작성
2. 기존 `isValidCheck()` 로직으로 테스트를 통과시켜 baseline 확인

#### Step B. enum 추출
1. `CarType`, `Engine`, `Brake`, `Steering` enum 생성
2. `stack[]` 인덱스와 int 상수를 enum으로 교체
3. 테스트 통과 확인

#### Step C. Car VO 추출
1. `Car.java` 생성 — `carType`, `engine`, `brake`, `steering` 필드
2. `stack[]` 전역 상태를 `Car` 인스턴스로 교체
3. 테스트 통과 확인

#### Step D. CarValidator 추출
1. `CarValidator.java` 생성 — `isValid(Car)`, `getFailReason(Car)` 메서드
2. 중복된 `isValidCheck()` / `testProducedCar()` 로직을 `CarValidator`로 통합
3. 테스트 통과 확인

#### Step E. ConsoleMenu / AssemblyWizard 분리
1. `ConsoleMenu.java` — 각 단계 메뉴 출력
2. `AssemblyWizard.java` — 입력 루프 및 단계 흐름
3. `Assemble.java` — `new AssemblyWizard().run()` 만 남김

### 체크리스트

- [ ] 각 Step 완료 후 기존 동작과 동일하게 작동하는지 수동 확인했다
- [ ] 모든 유닛 테스트가 통과한다
- [ ] `static` 전역 상태(`stack[]`)가 완전히 제거되었다
- [ ] int 상수(`SEDAN = 1` 등)가 모두 enum으로 교체되었다
- [ ] `isValidCheck()`와 `testProducedCar()`의 중복이 제거되었다
- [ ] 부품을 하나 추가할 때 수정 파일이 enum 1개 + 메뉴 1줄로 최소화되었는지 확인했다
- [ ] 출력 메시지가 리팩토링 전과 동일하다

---

## 4단계 — Commit (검토 및 마무리)

> 목표: 리팩토링 결과를 검토하고, 의도한 변경만 반영되었음을 확정한다.

### 최종 검토 항목

#### 동작 동등성 확인
- [ ] 5가지 제한조건 시나리오를 모두 실행해서 기존과 동일한 결과가 나온다
- [ ] "고장난 엔진" 선택 후 Run 시 기존 메시지가 그대로 출력된다
- [ ] 뒤로가기(0번 입력) 흐름이 모든 단계에서 정상 동작한다
- [ ] `exit` 입력 시 정상 종료된다

#### 코드 품질 확인
- [ ] 각 클래스가 단일 책임(SRP)을 가진다
- [ ] `CarValidator`가 유일한 유효성 검사 진입점이다
- [ ] 테스트 코드가 구현 세부사항(private 메서드, 내부 상태)이 아닌 public 인터페이스를 테스트한다
- [ ] 매직 넘버가 코드에 남아 있지 않다

#### 확장성 확인
- [ ] 새 차량 타입(예: 버스)을 추가한다면 `CarType` enum + `CarValidator` 규칙 추가만으로 가능한 구조인지 확인했다

#### 커밋 구성
- [ ] 커밋 단위가 논리적으로 분리되어 있다 (enum 추출 / VO 추출 / Validator 추출 / UI 분리)
- [ ] 커밋 메시지가 "무엇을"이 아닌 "왜"를 설명한다

---

## 참고: 시나리오 기반 regression 목록

리팩토링 전후 동일한 결과가 나와야 하는 케이스:

| # | 조합 | Run 결과 | Test 결과 |
|---|------|----------|-----------|
| 1 | Sedan + GM + Continental + Bosch | 동작 안 함 | FAIL (Continental Sedan 불가) |
| 2 | SUV + Toyota + Mando + Bosch | 동작 안 함 | FAIL (Toyota SUV 불가) |
| 3 | Truck + WIA + Bosch + Bosch | 동작 안 함 | FAIL (WIA Truck 불가) |
| 4 | Truck + GM + Mando + Bosch | 동작 안 함 | FAIL (Mando Truck 불가) |
| 5 | Sedan + GM + Bosch + Mobis | 동작 안 함 | FAIL (Bosch brake → Bosch steering 필수) |
| 6 | Sedan + 고장난엔진 + Mando + Bosch | 동작 안 함 | FAIL (고장난 엔진 — BUG-01 수정으로 변경) |
| 7 | Sedan + GM + Mando + Bosch | 정상 동작 | PASS |
| 8 | SUV + GM + Bosch + Bosch | 정상 동작 | PASS |
