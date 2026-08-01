package dev.aetlas.bootdoctor.rules;

public final class RuleIds {

    public static final String BUILD_SPRING_BOOT_MAVEN = "BUILD-001";
    public static final String PLAIN_SECRET = "SEC-001";
    public static final String CORS_WILDCARD = "SEC-002";
    public static final String ACTUATOR_MISSING = "OPS-001";
    public static final String DOCKERFILE_MISSING = "OPS-002";
    public static final String COMPOSE_MISSING = "OPS-003";
    public static final String TESTS_MISSING = "TEST-001";
    public static final String README_MISSING = "DOC-001";
    public static final String EXCEPTION_HANDLER_MISSING = "ARCH-001";
    public static final String VALIDATION_MISSING = "VAL-001";

    private RuleIds() {}
}
