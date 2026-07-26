package dev.aetlas.bootdoctor.model;

public enum Severity {
    CRITICAL(0),
    HIGH(1),
    MEDIUM(2),
    LOW(3),
    INFO(4);

    private final int priority;

    Severity(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }
}

