package com.mss.backOffice.config;


import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.mss.backOffice.request.UserPrincipal;



@Configuration
public class AuditingConfig {
	@Bean
	public AuditorAware<String> auditorProvider() {
		return new SpringSecurityAuditAwareImpl();
	}

}

class SpringSecurityAuditAwareImpl implements AuditorAware<String> {

	@Override
	public Optional<String> getCurrentAuditor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			return Optional.empty();
		}
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		return Optional.of(userPrincipal.getId());
	}

}
