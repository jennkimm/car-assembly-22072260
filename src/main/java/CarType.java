public enum CarType {
    SEDAN("Sedan"), SUV("SUV"), TRUCK("Truck");

    public final String displayName;

    CarType(String displayName) {
        this.displayName = displayName;
    }
}
