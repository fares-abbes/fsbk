package com.mss.backOffice.exception;


public class UserNotFoundException extends RuntimeException {
 
			private static final long serialVersionUID = 1L;
 
		    public
            UserNotFoundException(String id ) {
		        super("User: " +id +" not found.");
		    }
 
			public
            UserNotFoundException() {
			        super();
			   }
}

