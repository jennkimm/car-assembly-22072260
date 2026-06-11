# car-assembly 리팩토링 프로젝트

### 차량 제조 순서
1) 자동차 타입을 선택한다.
   세단, SUV(에스-유-브이), 트럭
   총 세가지 타입을 제작할 수 있으며,
   향후에 타입이 더 추가될 수 있다.

2) 자동차에 들어갈 부품을 선택한다.
   엔진, 제동장치, 조향장치를 각각 선택한다.

3) 완성된 차량을 테스트 한다.
   선택한 부품이 자동차 타입에 사용 가능한지 검사한다.
- 제한조건1
   제동장치에 Bosch 제품을 사용했다면, 조향장치도 Bosch 제품을 사용해야한다.
   (타사제품과호환되지않는다.)
- 제한조건2
   • Continental은 Sedan용 제동장치를만들지않는다.
   (-> 세단에 Continental 제품 사용 불가)
   • 도요타는 SUV용 엔진을 만들지 않는다.
   • WIA는 Truck용 엔진을 만들지 않는다.
   • Mando는 Truck용 제동장치(brake System)을 만들지 않는다.


## 프로젝트 문서

| 문서 | 설명 |
|------|------|
| [docs/PLAN.md](docs/PLAN.md) | 리팩토링 전체 계획 — Explore / Plan / Action / Commit 단계별 체크리스트 |
| [docs/EXPLORE.md](docs/EXPLORE.md) | Explore 단계 결과 — Code Smell 7건, 버그 2건, 안전성 문제 3건 상세 분석 |

> 각 단계를 시작하기 전에 해당 문서의 체크리스트를 먼저 확인한다.

---

## 리팩토링 방향

### 기존에 사용중인 시스템의 아쉬운 점
• 절차지향식 코드로, 유지보수가 어려운 구조
• 안전하지않은 문법들이 사용
• 확장성이 고려되지 않음
• 유닛 테스트가 없음

### 리팩토링 시 고려해야될 사항
- Agentic Engineering 을 지킨다.
- Explore 단계에서 Code Smell 을 탐색한다.
- Plan 을 검토, Refactoring 결과 검토
- 좋은 Unit Text = regression(핵심 로직에 대한 regression) * 리펙토링 내성
- 리팩토링 준비물 + coverage 특정
