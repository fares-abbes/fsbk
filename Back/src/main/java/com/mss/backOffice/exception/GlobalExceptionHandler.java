package com.mss.backOffice.exception;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Throwables;
import com.mss.unified.repositories.BatchPorteurRepository;

import org.apache.commons.lang3.StringUtils;
import static com.mss.backOffice.BackOfficeApplication.occupiedEv;
import static com.mss.backOffice.BackOfficeApplication.occupiedEd;
import static com.mss.backOffice.BackOfficeApplication.occupiedEi;
import static com.mss.backOffice.BackOfficeApplication.occupiedEm;
import static com.mss.backOffice.BackOfficeApplication.occupiedEr;
import static com.mss.backOffice.BackOfficeApplication.occupiedBe;
import static com.mss.backOffice.BackOfficeApplication.occupiedEPAN;
import static com.mss.backOffice.BackOfficeApplication.occupiedEm_A2C;
import static com.mss.backOffice.BackOfficeApplication.occupiedEv_A2C;
import static com.mss.backOffice.BackOfficeApplication.occupiedEd_A2C;
import static com.mss.backOffice.BackOfficeApplication.occupiedEi_A2C;
import static com.mss.backOffice.BackOfficeApplication.occupiedEr_A2C;
import static com.mss.backOffice.BackOfficeApplication.occupiedAcs;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
	
	@Autowired
	private BatchPorteurRepository batchPorteurRepository;
	
	
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<?> resourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
		logger.error(ExceptionUtils.getStackTrace(ex)); 
		ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getDescription(false));
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> globleExcpetionHandler(Exception ex, HttpServletRequest request) {
		logger.info(request.getRequestURI());
		int index =StringUtils.ordinalIndexOf(request.getRequestURI(), "/", 2);

		
		String stackTrace = Throwables.getStackTraceAsString(ex);
		logger.error("Exception is");
		logger.error(stackTrace);
		
		
		String error = ex.getMessage() == null ? ex.toString() : ex.getMessage();
		if (error.length() > 255)
			error = error.substring(0, 250);
		
		
		if (stackTrace.length() > 4000)
			stackTrace = stackTrace.substring(0, 3990);
		
		
		String requestUri=request.getRequestURI().substring(index+1);
		
		if (requestUri.equals("generateFileEmbossingReplacementWithPin")) {
			batchPorteurRepository.updateStatusAndErrorBatch("EV", 2,error, new Date(),stackTrace);
			occupiedEv=false;
		}
		
		
		if (requestUri.equals("generateFileEmbossingReplacementWithountPin")) {
			batchPorteurRepository.updateStatusAndErrorBatch("ED", 2, error, new Date(), stackTrace);
			occupiedEd=false;
		}
		
		if (requestUri.equals("generateFileEmbossingRecalculPin")) {
			batchPorteurRepository.updateStatusAndErrorBatch("EI", 2, error, new Date(), stackTrace);
			occupiedEi=false;
		}
		
		if (requestUri.equals("generateFileEmbossingRenewel")) {
			batchPorteurRepository.updateStatusAndErrorBatch("ER", 2, error, new Date(), stackTrace);
			occupiedEr=false;
		}
		
		
		if (requestUri.equals("generateFileEmbossingCreation")) {
			batchPorteurRepository.updateStatusAndErrorBatch("EM", 2, error, new Date(), stackTrace);
			occupiedEm=false;
		}
		
		if (requestUri.equals("generateFileBe")) {
			batchPorteurRepository.updateStatusAndErrorBatch("BE", 2, error, new Date(), stackTrace);
			occupiedBe=false;
		}
		
		if (requestUri.equals("readFilePorteur")) {
			batchPorteurRepository.updateStatusAndErrorBatch("readPorteur", 2, error, new Date(), stackTrace);
			occupiedEPAN=false;
		}
		
		if (requestUri.equals("generateFileEmbossingVisaCreation")) {
			batchPorteurRepository.updateStatusAndErrorBatch("EM_A2C", 2, error, new Date(), stackTrace);
			occupiedEm_A2C=false;
		}
		if (requestUri.equals("generateFileEmbossingVisaRenewel")) {
			batchPorteurRepository.updateStatusAndErrorBatch("ER_A2C", 2, error, new Date(), stackTrace);
			occupiedEr_A2C=false;
		}
		
		if (requestUri.equals("generateFileEmbossingVisaReplacementWithPin")) {
			batchPorteurRepository.updateStatusAndErrorBatch("EV_A2C", 2,error, new Date(),stackTrace);
			occupiedEv_A2C=false;
		}
		
		
		if (requestUri.equals("generateFileEmbossingVisaReplacementWithountPin")) {
			batchPorteurRepository.updateStatusAndErrorBatch("ED_A2C", 2, error, new Date(), stackTrace);
			occupiedEd_A2C=false;
		}
		
		if (requestUri.equals("generateFileEmbossingVisaRecalculPin")) {
			batchPorteurRepository.updateStatusAndErrorBatch("EI_A2C", 2, error, new Date(), stackTrace);
			occupiedEi_A2C=false;
		}
		
		if (requestUri.equals("generateFileACS")) {
			batchPorteurRepository.updateStatusAndErrorBatch("ACS", 2,error, new Date(),stackTrace);
			occupiedAcs=false;
		}
	
		ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), "");
		return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
		logger.error(ExceptionUtils.getStackTrace(ex)); 
		ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getDescription(false));
		return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
	}
	
	
}
