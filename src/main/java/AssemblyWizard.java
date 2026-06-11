import java.util.Scanner;

public class AssemblyWizard {

    private static final String CLEAR_SCREEN = "\033[H\033[2J";

    private final ConsoleMenu menu = new ConsoleMenu();
    private final CarValidator validator = new CarValidator();

    public void run() {
        Scanner sc = new Scanner(System.in);
        Step step = Step.CAR_TYPE;

        CarType carType = null;
        Engine engine = null;
        Brake brake = null;
        Steering steering = null;

        while (true) {
            System.out.print(CLEAR_SCREEN);
            System.out.flush();

            switch (step) {
                case CAR_TYPE:  menu.showCarTypeMenu();  break;
                case ENGINE:    menu.showEngineMenu();   break;
                case BRAKE:     menu.showBrakeMenu();    break;
                case STEERING:  menu.showSteeringMenu(); break;
                case RUN_TEST:  menu.showRunTestMenu();  break;
            }

            System.out.print("INPUT > ");
            String buf = sc.nextLine().trim();

            if (buf.equalsIgnoreCase("exit")) {
                System.out.println("바이바이");
                break;
            }

            int answer;
            try {
                answer = Integer.parseInt(buf);
            } catch (NumberFormatException e) {
                System.out.println("ERROR :: 숫자만 입력 가능");
                delay(800);
                continue;
            }

            if (!isValidRange(step, answer)) {
                delay(800);
                continue;
            }

            if (answer == 0) {
                step = (step == Step.RUN_TEST) ? Step.CAR_TYPE : step.previous();
                continue;
            }

            switch (step) {
                case CAR_TYPE:
                    carType = CarType.values()[answer - 1];
                    System.out.printf("차량 타입으로 %s을 선택하셨습니다.\n", carType.displayName);
                    delay(800);
                    step = Step.ENGINE;
                    break;
                case ENGINE:
                    engine = Engine.values()[answer - 1];
                    System.out.printf("%s 엔진을 선택하셨습니다.\n", engine.displayName);
                    delay(800);
                    step = Step.BRAKE;
                    break;
                case BRAKE:
                    brake = Brake.values()[answer - 1];
                    System.out.printf("%s 제동장치를 선택하셨습니다.\n", brake.displayName);
                    delay(800);
                    step = Step.STEERING;
                    break;
                case STEERING:
                    steering = Steering.values()[answer - 1];
                    System.out.printf("%s 조향장치를 선택하셨습니다.\n", steering.displayName);
                    delay(800);
                    step = Step.RUN_TEST;
                    break;
                case RUN_TEST:
                    Car car = new Car(carType, engine, brake, steering);
                    if (answer == 1) {
                        runCar(car);
                        delay(2000);
                    } else if (answer == 2) {
                        System.out.println("Test...");
                        delay(1500);
                        testCar(car);
                        delay(2000);
                    }
                    break;
            }
        }

        sc.close();
    }

    private void runCar(Car car) {
        String reason = validator.getFailReason(car);
        if (reason != null) {
            if (car.engine == Engine.BROKEN) {
                System.out.println("엔진이 고장나있습니다.");
                System.out.println("자동차가 움직이지 않습니다.");
            } else {
                System.out.println("자동차가 동작되지 않습니다");
            }
            return;
        }
        System.out.printf("Car Type : %s\n", car.carType.displayName);
        System.out.printf("Engine   : %s\n", car.engine.displayName);
        System.out.printf("Brake    : %s\n", car.brake.displayName);
        System.out.printf("Steering : %s\n", car.steering.displayName);
        System.out.println("자동차가 동작됩니다.");
    }

    private void testCar(Car car) {
        String reason = validator.getFailReason(car);
        if (reason != null) {
            System.out.println("자동차 부품 조합 테스트 결과 : FAIL");
            System.out.println(reason);
        } else {
            System.out.println("자동차 부품 조합 테스트 결과 : PASS");
        }
    }

    private boolean isValidRange(Step step, int ans) {
        switch (step) {
            case CAR_TYPE:
                if (ans < 1 || ans > 3) {
                    System.out.println("ERROR :: 차량 타입은 1 ~ 3 범위만 선택 가능");
                    return false;
                }
                break;
            case ENGINE:
                if (ans < 0 || ans > 4) {
                    System.out.println("ERROR :: 엔진은 1 ~ 4 범위만 선택 가능");
                    return false;
                }
                break;
            case BRAKE:
                if (ans < 0 || ans > 3) {
                    System.out.println("ERROR :: 제동장치는 1 ~ 3 범위만 선택 가능");
                    return false;
                }
                break;
            case STEERING:
                if (ans < 0 || ans > 2) {
                    System.out.println("ERROR :: 조향장치는 1 ~ 2 범위만 선택 가능");
                    return false;
                }
                break;
            case RUN_TEST:
                if (ans < 0 || ans > 2) {
                    System.out.println("ERROR :: Run 또는 Test 중 하나를 선택 필요");
                    return false;
                }
                break;
        }
        return true;
    }

    private void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}
