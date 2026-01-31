package com.mss.backOffice.Response;
 /**
  * 
		400 Bad Request — Client sent an invalid request — such as lacking required request body or parameter
		401 Unauthorized — Client failed to authenticate with the server
		403 Forbidden — Client authenticated but does not have permission to access the requested resource
		404 Not Found — The requested resource does not exist
		500 Internal Server Error — A generic error occurred on the server
		503 Service Unavailable — The requested service is not available
  * 
  */



import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.net.SocketTimeoutException;
import java.util.NoSuchElementException;


@ControllerAdvice
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	/**
	 * Exception for BAD_REQUEST
	 * @param ex
	 * @param request
	 * @return
	 * @throws Exception 400
	 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> Exception400(final RuntimeException ex, final WebRequest request) {    	 
    	 ErrorResponse er  = new ErrorResponse(HttpStatus.BAD_REQUEST.value() ,false, "Some think Wrong in your part please check Swagger documentation ");
 		return new  ResponseEntity(er , HttpStatus.BAD_REQUEST);
    }
    
    
	
    /**
	 * Exception for AuthenticationException
	 * @param ex
	 * @param request
	 * @return
	 * @throws Exception 401
	 */
    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<Object> Exception401(final RuntimeException ex, final WebRequest request) {
    	 ErrorResponse er  = new ErrorResponse(HttpStatus.UNAUTHORIZED.value() ,false, "Need Logged in first dude :) ");
 		return new  ResponseEntity(er , HttpStatus.UNAUTHORIZED);
    }
    
    
	
    /**
	 * Exception for NoHandlerFoundException
	 * @param ex
	 * @param request
	 * @return
	 * @throws Exception 404
	 */
//    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Object> Exception404(final RuntimeException ex, final WebRequest request) {
    	 ErrorResponse er  = new ErrorResponse(HttpStatus.NOT_FOUND.value() ,false, "Some think Wrong in your part please check swager documentation");
 		return new  ResponseEntity(er , HttpStatus.NOT_FOUND);
    }

    
	/**
	 * Exception for Access Denied 
	 * @param ex
	 * @param request
	 * @return
	 * @throws Exception 403
	 */
	@ExceptionHandler(AccessDeniedException.class)
 	public final ResponseEntity<Object> Exception403(Exception ex, WebRequest request) throws Exception {		
		 ErrorResponse er  = new ErrorResponse(HttpStatus.FORBIDDEN.value() ,false, "Access is denied");
		return new  ResponseEntity(er , HttpStatus.FORBIDDEN);
 	}
	

	/**
	 * Exception for NullPointerException && IllegalArgumentException && IllegalStateException
	 * @param ex
	 * @param request
	 * @return
	 * @throws Exception 500 NullPointerException.class, 
	 */
//    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
//    public ResponseEntity<Object> Exception500(final RuntimeException ex, final WebRequest request) {
//    	 ErrorResponse er  = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value() ,false, "Some think wrong with us please contact us as soon as possible ");
// 		return new  ResponseEntity(er , HttpStatus.INTERNAL_SERVER_ERROR);
//    }
 
	
	
	/**
	 * Exception for Expired or invalid JWT token
	 * @param ex
	 * @param request
	 * @return
	 * @throws Exception 402
	 */
	@ExceptionHandler(JwtException.class)
 	public final ResponseEntity<Object> Exception402(Exception ex, WebRequest request) throws Exception {		
		 ErrorResponse er  = new ErrorResponse(HttpStatus.FORBIDDEN.value() ,false, "Expired or invalid JWT token");
		return new  ResponseEntity(er , HttpStatus.FORBIDDEN);
 	}
	
	
 
	
	/**
	 * Exception for NoSuchElementException
	 * @param ex
	 * @param request
	 * @return
	 * @throws Exception 402
	 */
	@ExceptionHandler(NoSuchElementException.class)
 	public final ResponseEntity<Object> ExceptionNoValuePresent(Exception ex, WebRequest request) throws Exception {		
		 ErrorResponse er  = new ErrorResponse(HttpStatus.BAD_REQUEST.value() ,false, "No Value Present");
		return new  ResponseEntity(er , HttpStatus.BAD_REQUEST);
 	}
	
 

    
 

}