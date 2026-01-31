package com.mss.backOffice.config;


import com.mss.unified.repositories.ApplicationLogRepository;
import com.mss.unified.repositories.UserRepository;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;


@Component
public class ServiceInterceptor implements HandlerInterceptor {

  @Autowired
  ApplicationLogRepository applicationLogRepository;
  @Autowired
  UserRepository userRepository;

  @Override
  public boolean preHandle
      (HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    //System.out.println("Pre Handle method is Calling");
    return true;
  }



  @Override
  public void postHandle(HttpServletRequest request,
      HttpServletResponse response, Object handler,
      ModelAndView modelAndView) throws Exception {
   // System.out.println("SiteInterceptor postHandle");
    

  }


}
