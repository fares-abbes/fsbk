package com.mss.backOffice.enumType;

/**
 * Enum representing the different status values for file processing
 */
public enum FileStatusEnum {
    PENDING("Pending"),
    INTEGRATED("integrated"),
    LOT("Lot"),
    CRA("CRA"),
    CRO("CRO"),
    REG("REG"),
    ARCH("ARCH");

    private final String value;

    FileStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get enum from string value
     * @param value the string value
     * @return the corresponding enum
     */
    public static FileStatusEnum fromValue(String value) {
        for (FileStatusEnum status : FileStatusEnum.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown file status: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
