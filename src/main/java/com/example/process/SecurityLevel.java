package com.example.process;


public enum SecurityLevel {
    TOP_SECRET("Top Secret", 4), SECRET("Secret", 3), CONFIDENTIAL("Confidential", 2), UNCLASSIFIED("Unclassified", 1);

    private String value;
    private int securityOrdinal;

    private SecurityLevel(String value, int securityOrdinal) {
        this.securityOrdinal = securityOrdinal;
        this.value = value;
    }

    public int getSecurityOrdinal() {
        return securityOrdinal;
    }

    public String getValue() {
        return value;
    }

    public SecurityLevel fromValue(String value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].value.equalsIgnoreCase(value)) {
                return values()[i];
            }
        }
        return null;
    }

    public static int fromString(String s) {
        for (SecurityLevel sl : values()) {
            if (sl.toString().equalsIgnoreCase(s)) {
                return sl.securityOrdinal;
            }
        }
        throw new IllegalArgumentException("unidentified security level");
    }
}
