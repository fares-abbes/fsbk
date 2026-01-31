package com.mss.backOffice.Response;


import javax.servlet.http.HttpServletResponse;

public class Response {
    private  boolean status = true;
    private int code;   
    private String message;
    private Object value;

    public Response(HttpServletResponse res, int code , boolean status, Object value) {
        res.setStatus(code);
        this.status = status;
        this.code = code;
        this.value = value;
    }
    
    public Response(HttpServletResponse res,boolean status, int code,Object value,String message) {

        res.setStatus(code);
        this.status = status;
        this.code = code;
        this.value = value;
        this.message=message;

    }

    public Response(int code , boolean status, Object value) {
        this.status = status;
        this.code = code;
        this.value = value;
    }
    public Response(int code , boolean status, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
    public Response() {
	 
	}

	public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}


}

