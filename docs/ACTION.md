# Action — 리팩토링 실행 기록

> 기준 문서: [docs/PLAN.md](PLAN.md)
> 원칙: 테스트 먼저 → 리팩토링 → 테스트 통과 확인 (각 Step마다 반복)

---

## 빌드 환경

| 항목 | 값 |
|------|-----|
| Java | OpenJDK 21 (Temurin-21.0.11) |
| 빌드 도구 | Gradle 9.3.0 |
| 테스트 프레임워크 | JUnit 5 (Jupiter 5.10.0) |
| 소스 경로 | `src/main/java/` |
| 테스트 경로 | `src/test/java/` |

---

## Step A. 테스트 작성 ✅

**대상**: `CarValidatorTest.java` — R1~R7 + BUG-01 수정 케이스

| 테스트 | 검증 규칙 | 결과 |
|--------|-----------|------|
| `sedan_continental_불가` | R1 | ✅ |
| `suv_toyota_불가` | R2 | ✅ |
| `truck_wia_불가` | R3 | ✅ |
| `truck_mando_불가` | R4 | ✅ |
| `bosch_brake_비bosch_steering_불가` | R5 | ✅ |
| `broken_engine_불가` | R6 + R7 (BUG-01 수정) | ✅ |
| `bosch_brake_bosch_steering_가능` | R5 경계 | ✅ |
| `유효한_조합_통과` | 정상 케이스 | ✅ |

---

## Step B. enum 추출 ✅

| 파일 | 멤버 | 해결 |
|------|------|------|
| `CarType.java` | SEDAN, SUV, TRUCK | CS-05, CS-06 |
| `Engine.java` | GM, TOYOTA, WIA, BROKEN | CS-05, CS-06 |
| `Brake.java` | MANDO, CONTINENTAL, BOSCH | CS-05, CS-06 |
| `Steering.java` | BOSCH, MOBIS | CS-05, CS-06 |
| `Step.java` | CAR_TYPE, ENGINE, BRAKE, STEERING, RUN_TEST | CS-07 |

- 각 enum에 `displayName` 필드 추가 → 이름 매핑 분산 제거 (CS-06)
- `BROKEN = 4` 미선언 상수 제거 (CS-05)
- `Step.previous()` 메서드로 뒤로가기 캡슐화

---

## Step C. Car VO 추출 ✅

`Car.java` — `CarType`, `Engine`, `Brake`, `Steering` 불변 필드

- `static int[] stack` 전역 상태 제거 (SI-02)
- 1-based dummy 배열 관례 제거 (SI-03)

---

## Step D. CarValidator 추출 ✅

`CarValidator.java` — `isValid(Car)`, `getFailReason(Car)`

- `isValidCheck()` / `testProducedCar()` 중복 제거 (CS-03)
- BUG-01 수정: 고장난 엔진 → `getFailReason()` non-null 반환

---

## Step E. ConsoleMenu / AssemblyWizard 분리 ✅

| 파일 | 책임 | 해결 |
|------|------|------|
| `ConsoleMenu.java` | 메뉴 출력 전담 | SI-01 |
| `AssemblyWizard.java` | Step enum 기반 입력 루프 | CS-02, CS-07 |
| `Assemble.java` | `new AssemblyWizard().run()` 한 줄 | CS-02 |

---

## 체크리스트

- [x] Step A — CarValidatorTest 8개 테스트 작성
- [x] Step B — enum 5개 추출 (`CarType`, `Engine`, `Brake`, `Steering`, `Step`)
- [x] Step C — `Car` VO 추출, `stack[]` 제거
- [x] Step D — `CarValidator` 추출, BUG-01 수정
- [x] Step E — `ConsoleMenu`, `AssemblyWizard` 분리, `Assemble` 정리
- [x] 전체 유닛 테스트 통과 (`./gradlew test`)
- [ ] **[사용자 확인]** 각 Step 완료 후 기존 동작과 동일하게 작동하는지 수동 확인
- [ ] **[사용자 확인]** `static int[] stack` 전역 상태 완전히 제거 확인
- [ ] **[사용자 확인]** BUG-01 수정: 고장난 엔진 선택 시 Test가 FAIL 반환 확인
- [ ] **[사용자 확인]** 출력 메시지(한국어)가 리팩토링 전과 동일한지 확인
- [ ] **[사용자 확인]** Commit 단계 진행 승인
