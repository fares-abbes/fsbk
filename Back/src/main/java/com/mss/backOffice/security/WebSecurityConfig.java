package com.mss.backOffice.security;

import com.mss.backOffice.security.jwt.JwtAuthEntryPoint;
import com.mss.backOffice.security.jwt.JwtAuthTokenFilter;
import com.mss.backOffice.services.CustomUserDetailsService;
import com.mss.backOffice.services.UserDetailsServiceImpl;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true

)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthEntryPoint unauthorizedHandler;

    @Bean
    public JwtAuthTokenFilter authenticationJwtTokenFilter() {
        return new JwtAuthTokenFilter();
    }


    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Pbkdf2PasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable().
                authorizeRequests()
                // .antMatchers("/user/**").permitAll()
                .antMatchers("/v2/api-docs").permitAll()//
                .antMatchers("/swagger-resources/**").permitAll()//
                .antMatchers("/swagger-ui.html").permitAll()//
                .antMatchers("/configuration/**").permitAll()//
                .antMatchers("/webjars/**").permitAll()//
                // Springdoc OpenAPI 3 endpoints
                .antMatchers("/v3/api-docs").permitAll()//
                .antMatchers("/v3/api-docs/**").permitAll()//
                .antMatchers("/swagger-ui/**").permitAll()//

                .antMatchers("/user/signin").permitAll()//
                .antMatchers("/user/forgetPassword").permitAll()//
                .antMatchers("/user/addVue").permitAll()//
                .antMatchers("/user/addAccess").permitAll()//
                .antMatchers("/user/registerFirstUser").permitAll()//
                .antMatchers("/user/changeEmailAndPasswordForFirstUser").permitAll()//
                .antMatchers("/user/addNav").permitAll()//


//                .antMatchers("/cardManagement/saveCustomer").permitAll()//
//                .antMatchers("/cardManagement/saveAccount").permitAll()//
//                .antMatchers("/cardManagement/saveCard").permitAll()//
//                .antMatchers("/cardManagement/saveGlobalAndDetailledRM/**").permitAll()//

                .antMatchers("/balance/update").permitAll()//
                //.antMatchers("/executorThreadUAP051INFileBC/buildControlTp").permitAll()//
             //   .antMatchers("/FileRequest/croControl").permitAll()//
                .anyRequest().authenticated().and().headers().addHeaderWriter(new StaticHeadersWriter("Server","server")).and().headers().frameOptions().sameOrigin()

            .httpStrictTransportSecurity().disable()
           /* .and().x509().subjectPrincipalRegex("CN=(.*?)(?:,|$)")*/.and()


            //    .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()

                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    }


    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }



    @Bean
    public static ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
    }


}
