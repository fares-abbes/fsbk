package com.mss.backOffice.enumType;


import org.springframework.security.core.GrantedAuthority;

public enum RoleName implements GrantedAuthority {

   AGENT, ADMIN,ROLE_USER;


   @Override
   public
   String getAuthority() {
      return name();
   }
}
