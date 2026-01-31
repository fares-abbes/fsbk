package com.mss.backOffice.Response;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.servlet.http.HttpServletResponse;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse extends Response {

 
    public ErrorResponse(HttpServletResponse res, int code, String o) {
       super(res, false,code,null,o);

    }
    
    public ErrorResponse(int code,String o) {
        super(code ,false,o);
     }
    
    public ErrorResponse(int code,boolean statue , String message) {
    	super(code,statue,message)  ;
     }

	 
}

