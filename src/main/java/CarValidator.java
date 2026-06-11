public class CarValidator {

    public boolean isValid(Car car) {
        return getFailReason(car) == null;
    }

    public String getFailReason(Car car) {
        if (car.carType == CarType.SEDAN && car.brake == Brake.CONTINENTAL)
            return "Sedan에는 Continental제동장치 사용 불가";
        if (car.carType == CarType.SUV && car.engine == Engine.TOYOTA)
            return "SUV에는 TOYOTA엔진 사용 불가";
        if (car.carType == CarType.TRUCK && car.engine == Engine.WIA)
            return "Truck에는 WIA엔진 사용 불가";
        if (car.carType == CarType.TRUCK && car.brake == Brake.MANDO)
            return "Truck에는 Mando제동장치 사용 불가";
        if (car.brake == Brake.BOSCH && car.steering != Steering.BOSCH)
            return "Bosch제동장치에는 Bosch조향장치 이외 사용 불가";
        if (car.engine == Engine.BROKEN)
            return "엔진이 고장나있습니다.";
        return null;
    }
}
