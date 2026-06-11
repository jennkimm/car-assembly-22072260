import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CarValidatorTest {

    private final CarValidator validator = new CarValidator();

    @Test
    void sedan_continental_불가() {
        Car car = new Car(CarType.SEDAN, Engine.GM, Brake.CONTINENTAL, Steering.BOSCH);
        assertFalse(validator.isValid(car));
        assertNotNull(validator.getFailReason(car));
    }

    @Test
    void suv_toyota_불가() {
        Car car = new Car(CarType.SUV, Engine.TOYOTA, Brake.MANDO, Steering.BOSCH);
        assertFalse(validator.isValid(car));
        assertNotNull(validator.getFailReason(car));
    }

    @Test
    void truck_wia_불가() {
        Car car = new Car(CarType.TRUCK, Engine.WIA, Brake.BOSCH, Steering.BOSCH);
        assertFalse(validator.isValid(car));
        assertNotNull(validator.getFailReason(car));
    }

    @Test
    void truck_mando_불가() {
        Car car = new Car(CarType.TRUCK, Engine.GM, Brake.MANDO, Steering.BOSCH);
        assertFalse(validator.isValid(car));
        assertNotNull(validator.getFailReason(car));
    }

    @Test
    void bosch_brake_비bosch_steering_불가() {
        Car car = new Car(CarType.SEDAN, Engine.GM, Brake.BOSCH, Steering.MOBIS);
        assertFalse(validator.isValid(car));
        assertNotNull(validator.getFailReason(car));
    }

    @Test
    void broken_engine_불가() {
        Car car = new Car(CarType.SEDAN, Engine.BROKEN, Brake.MANDO, Steering.BOSCH);
        assertFalse(validator.isValid(car));
        assertNotNull(validator.getFailReason(car));
    }

    @Test
    void bosch_brake_bosch_steering_가능() {
        Car car = new Car(CarType.SEDAN, Engine.GM, Brake.BOSCH, Steering.BOSCH);
        assertTrue(validator.isValid(car));
        assertNull(validator.getFailReason(car));
    }

    @Test
    void 유효한_조합_통과() {
        Car car = new Car(CarType.SEDAN, Engine.GM, Brake.MANDO, Steering.BOSCH);
        assertTrue(validator.isValid(car));
        assertNull(validator.getFailReason(car));
    }
}
