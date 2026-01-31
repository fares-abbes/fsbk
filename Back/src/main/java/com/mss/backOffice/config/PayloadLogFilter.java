package com.mss.backOffice.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import com.mss.unified.entities.ApplicationLog;
import com.mss.unified.repositories.ApplicationLogRepository;
import javax.servlet.*;
import java.io.IOException;
import java.util.Date;

@Component
@Order(2) // This needs to come after `CachingBodyFilter`
public class PayloadLogFilter implements Filter {
	@Autowired
	ApplicationLogRepository applicationLogRepository;

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		chain.doFilter(req, res);

		ContentCachingRequestWrapper reqWrapper = (ContentCachingRequestWrapper) req;
		String payloadReq = new String(reqWrapper.getContentAsByteArray(), "utf-8");

		//System.out.println(reqWrapper.getRequestURI() + "    request   reader    " + payloadReq);

		ContentCachingResponseWrapper resWrapper = (ContentCachingResponseWrapper) res;
		String payloadRes = new String(resWrapper.getContentAsByteArray(), "utf-8");

		//System.out.println("    response   reader    " + payloadRes);

		String email;
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		if (name.equals("anonymousUser")) {
			email = null;
		} else {
			email = name;
		}
		ApplicationLog ap = new ApplicationLog();
		ap.setExcutionDate(new Date());
		int i = reqWrapper.getRequestURI().indexOf("/", reqWrapper.getRequestURI().indexOf("/") + 1);
//		
		//int i =StringUtils.ordinalIndexOf(reqWrapper.getRequestURI(), "/", 3);
		ap.setMethodName(reqWrapper.getRequestURI().substring(i + 1));
		ap.setResponse(String.valueOf(resWrapper.getStatus()));

		ap.setIp(reqWrapper.getLocalAddr());
		ap.setUserEmail(email);

		if (ap.getMethodName().equals("signin") || ap.getMethodName().equals("changePassword")|| ap.getMethodName().equals("addNav")
				|| ap.getMethodName().equals("readFilePorteur")
				|| ap.getMethodName().equals("readFileCaf")
				|| ap.getMethodName().equals("readFilePF")
				|| ap.getMethodName().equals("forgetPassword")|| ap.getMethodName().equals("changeEmailAndPasswordForFirstUser")) {
			ap.setBody(null);
		} else {
			if (payloadReq.length()>4000)
		        ap.setBody(payloadReq.substring(0,3999));
			else
				ap.setBody(payloadReq);
		}
		if (ap.getResponse().equals("200")) {
			ap.setResponseBody(null);
		} else {
			ap.setResponseBody(payloadRes);
		}

		//System.out.println(" logggg" + ap.toString());
		//applicationLogRepository.save(ap);
	}

}
