package com.mss.backOffice.enumType;
public enum ChargebackStatus {
    None(0),
    PENDING(1),
    REJECT(2),
    DONE(3);

    public final int code;
    ChargebackStatus(int code) { this.code = code; }
    public int getCode() { return code; }
    public String toString() {
        return value;
    }

    public String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}