package com.mss.backOffice.enumType;

public enum UserStatusName {

	GUEST
     ;
	
    private static final UserStatusName[] copyOfValues = values();

    public static UserStatusName forName(String name) {
        for (UserStatusName value : copyOfValues) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return null;
    }

    

}
