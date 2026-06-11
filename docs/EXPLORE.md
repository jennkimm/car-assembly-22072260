# Explore — Code Smell 탐색 결과

> 대상 파일: `Assemble.java` (271줄, 단일 클래스)
> 탐색 기준: PLAN.md 1단계 체크리스트 + 직접 코드 분석

---

## 전체 요약

| 구분 | 건수 |
|------|------|
| Code Smell | 7건 |
| 버그 (동작 불일치) | 2건 |
| 네이밍 / 안전성 문제 | 3건 |

---

## Code Smell 상세

---

### CS-01. Primitive Obsession — `static int[] stack`

**위치**: `Assemble.java:17`, `:192`, `:196`, `:200`, `:205`

```java
private static int[] stack = new int[5];   // 크기 5, 실제 사용은 인덱스 0~3
```

부품 선택값 전체를 `int[]` 하나로 관리한다.
인덱스와 값 모두 int이기 때문에 잘못된 인덱스 접근이나 범위 외 값 대입을 컴파일 타임에 잡을 수 없다.

```java
// 이 코드는 컴파일 통과 — 런타임에도 예외 없이 조용히 오염됨
stack[Engine_Q] = 99;
stack[5] = 1;   // ArrayIndexOutOfBoundsException — 런타임에야 발견
```

- `stack[4]` (인덱스 = `Run_Test`)는 배열에 할당되어 있으나 **단 한 번도 값이 쓰이지 않는다.**
  → 배열 크기를 5로 잡은 이유가 불명확하며, 미래 독자에게 혼란을 준다.

---

### CS-02. God Method — `main()`

**위치**: `Assemble.java:19~107` (89줄)

단일 `main()` 안에 다음 책임이 모두 뒤섞여 있다.

| 책임 | 코드 위치 |
|------|-----------|
| 애플리케이션 루프 제어 | `while(true)` |
| 화면 지우기 | `:24~25` |
| 단계별 메뉴 디스패치 | `:27~38` |
| 입력 파싱 및 예외 처리 | `:41~55` |
| 범위 유효성 검사 호출 | `:57~60` |
| 뒤로가기 로직 | `:62~69` |
| 단계 전이 및 선택값 저장 | `:71~103` |
| 종료 처리 | `:43~46`, `:106` |

메서드 하나가 이 많은 일을 하면 단위 테스트가 불가능하다.
`step` 전이 로직을 검증하려면 실제 `Scanner`와 `System.in`을 연결해야 한다.

---

### CS-03. Duplicated Code — `isValidCheck()` vs `testProducedCar()`

**위치**: `Assemble.java:212~219` vs `:244~258`

동일한 5개 호환성 규칙이 **두 메서드에 각각 따로 구현**되어 있다.

```java
// isValidCheck() — 212~219
if (stack[CarType_Q] == SEDAN && stack[BrakeSystem_Q] == CONTINENTAL) return false;
if (stack[CarType_Q] == SUV   && stack[Engine_Q] == TOYOTA)           return false;
if (stack[CarType_Q] == TRUCK && stack[Engine_Q] == WIA)              return false;
if (stack[CarType_Q] == TRUCK && stack[BrakeSystem_Q] == MANDO)       return false;
if (stack[BrakeSystem_Q] == BOSCH_B && stack[SteeringSystem_Q] != BOSCH_S) return false;

// testProducedCar() — 244~254 (동일 규칙을 if-else 체인으로 재구현)
if      (stack[CarType_Q] == SEDAN && stack[BrakeSystem_Q] == CONTINENTAL) fail("...");
else if (stack[CarType_Q] == SUV   && stack[Engine_Q] == TOYOTA)            fail("...");
else if (stack[CarType_Q] == TRUCK && stack[Engine_Q] == WIA)               fail("...");
else if (stack[CarType_Q] == TRUCK && stack[BrakeSystem_Q] == MANDO)        fail("...");
else if (stack[BrakeSystem_Q] == BOSCH_B && stack[SteeringSystem_Q] != BOSCH_S) fail("...");
```

규칙이 하나 추가되거나 변경될 때 **두 곳을 동시에 수정해야** 한다.
한쪽만 수정하면 Run과 Test의 판정이 달라지는 버그가 발생한다.

---

### CS-04. Divergent Change — 부품 추가 시 수정 지점 분산

**위치**: 코드 전체

새 부품 종류(예: 엔진 브랜드 추가)를 넣으려면 아래 위치를 모두 수정해야 한다.

| 수정 위치 | 해당 라인 |
|-----------|-----------|
| `int` 상수 선언 추가 | `:12~15` |
| `showXxxMenu()` 출력 문자열 | `:122~153` |
| `isValidRange()` switch 범위 숫자 | `:155~188` |
| `selectXxx()` 삼항 연산자 체인 | `:191~209` |
| `runProducedCar()` 이름 배열 또는 삼항 | `:232~240` |
| `isValidCheck()` 조건식 | `:212~219` |
| `testProducedCar()` 조건식 + 메시지 | `:244~258` |

7개 지점 중 하나라도 누락하면 런타임 오류 또는 잘못된 출력이 발생한다.

---

### CS-05. Magic Number — 미선언 상수 `4` (고장난 엔진)

**위치**: `Assemble.java:226`

```java
if (stack[Engine_Q] == 4) {   // "4"가 고장난 엔진임을 코드에서 알 수 없음
```

엔진 상수는 `:13`에 `GM=1, TOYOTA=2, WIA=3` 까지만 선언되어 있다.
`BROKEN = 4`에 해당하는 상수가 없어서 `4`가 무엇인지 코드만 보고 알 수 없다.

```java
private static final int GM = 1, TOYOTA = 2, WIA = 3;  // BROKEN(4) 누락
```

---

### CS-06. Data Clumps — 부품 이름 매핑이 세 가지 방식으로 분산

**위치**: `:193`, `:197`, `:202`, `:207`, `:232~240`

부품 번호 → 이름 변환이 일관된 방식 없이 세 군데서 제각각 구현되어 있다.

**방식 A. 삼항 연산자 체인 (select 메서드)**
```java
// :193
a == 1 ? "Sedan" : a == 2 ? "SUV" : "Truck"

// :197
a == 1 ? "GM" : a == 2 ? "TOYOTA" : a == 3 ? "WIA" : "고장난 엔진"
```

**방식 B. 1-based dummy 배열 (`runProducedCar`)**
```java
// :232~233
String[] carNames = {"", "Sedan", "SUV", "Truck"};   // 인덱스 0은 빈 문자열
String[] engNames = {"", "GM", "TOYOTA", "WIA"};     // BROKEN(4)는 없음
```

**방식 C. 삼항 연산자 (`runProducedCar` 내 Brake/Steering)**
```java
// :237~240
stack[BrakeSystem_Q]==1 ? "Mando" : stack[BrakeSystem_Q]==2 ? "Continental" : "Bosch"
stack[SteeringSystem_Q]==1 ? "Bosch" : "Mobis"
```

같은 데이터를 세 가지 방법으로 다루기 때문에 부품 추가 시 변환 로직 누락이 발생하기 쉽다.

---

### CS-07. 네이밍 컨벤션 위반 — 상수가 UPPER_SNAKE_CASE가 아님

**위치**: `Assemble.java:6~10`

```java
private static final int CarType_Q       = 0;   // PascalCase + underscore 혼용
private static final int Engine_Q        = 1;
private static final int BrakeSystem_Q   = 2;
private static final int SteeringSystem_Q = 3;
private static final int Run_Test        = 4;
```

Java 상수 컨벤션은 `CAR_TYPE_Q`, `ENGINE_Q` 등 `UPPER_SNAKE_CASE`다.
`SEDAN`, `GM`, `MANDO` 등 다른 상수들은 올바른 컨벤션을 따르고 있어 일관성이 없다.

---

## 버그 (동작 불일치)

---

### BUG-01. 고장난 엔진이 `testProducedCar()`에서 검사되지 않음

**위치**: `Assemble.java:244~258`

`testProducedCar()`는 5개 호환성 규칙만 검사하고, **고장난 엔진(Engine=4) 케이스를 검사하지 않는다.**

```
시나리오: Sedan + 고장난 엔진 + Mando + Bosch 선택 후

Run  → "엔진이 고장나있습니다. 자동차가 움직이지 않습니다."  (정상)
Test → "자동차 부품 조합 테스트 결과 : PASS"               ← 버그
```

Run은 동작 불가로 판정하지만 Test는 PASS를 반환한다.
사용자 관점에서 Test가 PASS라면 차가 동작해야 하는데 Run은 실패하는 모순이 발생한다.

---

### BUG-02. `runProducedCar()`의 `engNames` 배열이 고장난 엔진 케이스를 포함하지 않음

**위치**: `Assemble.java:233`

```java
String[] engNames = {"", "GM", "TOYOTA", "WIA"};  // 인덱스 4 없음
```

`isValidCheck()` 통과 + `stack[Engine_Q] == 4` 체크 사이에 논리적으로
`engNames[stack[Engine_Q]]`가 `engNames[4]`에 접근하는 경로는 없지만,
두 검사의 순서가 바뀌거나 조건이 바뀌면 즉시 `ArrayIndexOutOfBoundsException`이 발생하는
**취약한 구조**다. 방어 코드나 설계적 보호가 없다.

---

## 안전하지 않은 문법 / 설계 문제

---

### SI-01. `isValidRange()`에 UI 출력 책임이 섞임

**위치**: `Assemble.java:155~188`

`isValidRange()`는 이름상 "범위 검사" 메서드이지만, 내부에서 직접 `System.out.println()`으로 오류 메시지를 출력한다.
검사와 출력이 결합되어 있어 **오류 메시지를 바꾸지 않고 검사 로직만 재사용하는 것이 불가능**하다.
단위 테스트에서 호출하면 원치 않는 출력이 발생한다.

---

### SI-02. `static` 전역 상태 — `stack[]`

**위치**: `Assemble.java:17`

```java
private static int[] stack = new int[5];
```

선택값 상태가 클래스 레벨 `static` 배열에 저장된다.
- 테스트 간 상태 오염: 한 테스트가 `stack`을 수정하면 다음 테스트에 영향
- 프로그램을 처음부터 다시 시작하지 않으면 이전 선택값이 남아있음
- "처음 화면으로 돌아가기"(step = CarType_Q) 시 `stack[]`은 초기화되지 않아
  이전 선택값이 그대로 유지됨 (현재 코드에서는 어차피 덮어쓰이지만 의도가 불명확)

---

### SI-03. 1-based 배열 인덱싱 관례 — `{"", "Sedan", "SUV", "Truck"}`

**위치**: `Assemble.java:232~233`

부품 번호가 1부터 시작하기 때문에 `runProducedCar()` 내부에서
인덱스 0 위치에 빈 문자열을 집어넣은 dummy 배열을 사용한다.

```java
String[] carNames = {"", "Sedan", "SUV", "Truck"};  // [0]은 쓰레기 값
```

이 관례는 enum의 `ordinal()` 또는 별도 `displayName` 필드로 완전히 제거할 수 있다.

---

## 호환성 규칙 전체 목록 (regression 기준)

리팩토링 후 반드시 동일하게 동작해야 하는 규칙:

| # | 규칙 | 소스 라인 |
|---|------|-----------|
| R1 | Sedan + Continental 제동장치 → 불가 | `:213`, `:245` |
| R2 | SUV + TOYOTA 엔진 → 불가 | `:214`, `:247` |
| R3 | Truck + WIA 엔진 → 불가 | `:215`, `:249` |
| R4 | Truck + MANDO 제동장치 → 불가 | `:216`, `:251` |
| R5 | BOSCH 제동장치 → BOSCH 조향장치 필수 | `:217`, `:253` |
| R6 | 고장난 엔진(4) → Run 불가 | `:226~230` |
| R7 | 고장난 엔진(4) → Test 결과 미검사 (현재 PASS 반환, BUG-01) | `:244~258` |

> **결정 (2026-06-11)**: R7은 Action 단계에서 함께 수정한다.
> 고장난 엔진 선택 시 Test도 FAIL을 반환하도록 변경. `CarValidator`에서 단일 처리.

---

## Explore 단계 체크리스트

- [x] Code Smell 7건 코드에서 직접 확인
- [x] `isValidCheck()`와 `testProducedCar()`의 중복 규칙이 정확히 일치하는지 비교 (5개 규칙 동일, if-return vs if-else 구조 차이만 있음)
- [x] 고장난 엔진이 `testProducedCar()`에서 검사되지 않는 버그(BUG-01) 확인
- [x] 부품 추가 시 수정 필요 위치 7곳 파악 (CS-04)
- [x] `stack[4]` 미사용 확인 (CS-01)
- [x] `BROKEN = 4` 상수 미선언 확인 (CS-05)
- [x] regression 기준 규칙 목록 R1~R7 작성
- [x] **[사용자 확인]** BUG-01(고장난 엔진 Test PASS) → Action 단계에서 FAIL로 수정하기로 결정
- [x] **[사용자 확인]** Plan 단계 진행 승인
