public class Car {
    public final CarType carType;
    public final Engine engine;
    public final Brake brake;
    public final Steering steering;

    public Car(CarType carType, Engine engine, Brake brake, Steering steering) {
        this.carType = carType;
        this.engine = engine;
        this.brake = brake;
        this.steering = steering;
    }
}
