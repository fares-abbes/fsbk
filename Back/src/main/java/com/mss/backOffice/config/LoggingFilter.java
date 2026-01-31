package com.mss.backOffice.config;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import static java.nio.charset.StandardCharsets.UTF_8;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component 
@Order(1)
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) {
        ContentCachingRequestWrapper reqWrapper = new ContentCachingRequestWrapper((HttpServletRequest) req);
        ContentCachingResponseWrapper resWrapper = new ContentCachingResponseWrapper((HttpServletResponse) res);
        try {
            chain.doFilter(reqWrapper, resWrapper);
            resWrapper.copyBodyToResponse();  // Necessary (see answer by StasKolodyuk above)
        } catch (IOException | ServletException e) {
        	e.printStackTrace();
           // log.error("Error extracting body", e);
        }
    }

}