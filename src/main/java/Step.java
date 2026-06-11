public enum Step {
    CAR_TYPE, ENGINE, BRAKE, STEERING, RUN_TEST;

    private static final Step[] VALUES = values();

    public Step previous() {
        return this.ordinal() > 0 ? VALUES[this.ordinal() - 1] : this;
    }
}
