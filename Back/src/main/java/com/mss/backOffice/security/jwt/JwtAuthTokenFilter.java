package com.mss.backOffice.security.jwt;

import com.mss.backOffice.services.CustomUserDetailsService;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


public class JwtAuthTokenFilter extends OncePerRequestFilter {

  @Autowired
  private JwtProvider tokenProvider;

  @Autowired
  private CustomUserDetailsService userDetailsService;

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthTokenFilter.class);

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {
    try {

      String jwt = getJwt(request);
      if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
        String username = tokenProvider.getUsernameFromToken(jwt);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication
            = new UsernamePasswordAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (Exception e) {
      logger.error("Can NOT set user authentication -> Message: {}", e);
    }

    filterChain.doFilter(request, response);
  }

  private String getJwt(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");

    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {

      return bearerToken.substring(7);

    }

    return null;

  }
}
