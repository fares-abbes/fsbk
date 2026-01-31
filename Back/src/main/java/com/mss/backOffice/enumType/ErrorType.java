package com.mss.backOffice.enumType;

public enum ErrorType {
    MAIL_NOT_VERIFIED("MAIL.NOT.VERIFIED"),
    INVALID_PASSWORD("INVALID.PASSWORD"),
    INVALID_MAIL("INVALID.MAIL"),
    USER_NOT_FOUND("USER.NOT.FOUND"),
    MAIL_NOT_SENT("MAIL.NOT.SENT"),
    BLOCKED("BLOCKED"),
    PREVIOUS_PASSWORD("PREVIOUS.PASSWORD"),
    SYSTEM_ERROR("SYSTEM.ERROR"),
    USER_EXIST("USER.EXIST"),
    LOGIN_BLOCKED("LOGIN.BLOCKED");
	
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    ErrorType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
