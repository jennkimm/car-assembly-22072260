public enum Engine {
    GM("GM"), TOYOTA("TOYOTA"), WIA("WIA"), BROKEN("고장난 엔진");

    public final String displayName;

    Engine(String displayName) {
        this.displayName = displayName;
    }
}
