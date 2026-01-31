package com.mss.backOffice;



import com.mss.backOffice.config.*;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableWebSecurity
@EnableScheduling
@SpringBootApplication
@Configuration
@ComponentScan(basePackages={"com.mss.unified","com.mss.backOffice"})
@EnableJpaRepositories(basePackages="com.mss.unified.repositories")
@EnableTransactionManagement
@EnableEncryptableProperties
@EntityScan(basePackages={"com.mss.unified","com.mss.backOffice"})
@Import({ AuditingConfig.class, DatasourceConfig.class , IFA_TTTTLLLNUM.class, InterceptorAppConfig.class,
		LoggingFilter.class, PayloadLogFilter.class,PBKDF2Hasher.class,
SchedulerConfig.class,  ServiceInterceptor.class,
TTTTDecimalTagMapper.class, WebConfig.class}) //les classes de configurations
public class BackOfficeApplication extends SpringBootServletInitializer {
	public static boolean occupied=false;
	public static boolean occupiedEv=false;
	public static boolean occupiedEd=false;
	public static boolean occupiedEi=false;
	public static boolean occupiedEr=false;
	public static boolean occupiedEm=false;
	public static boolean occupiedBe=false;
	public static boolean occupiedEPAN=false;
	public static boolean occupiedEm_A2C=false;
	public static boolean occupiedEv_A2C=false;
	public static boolean occupiedEd_A2C=false;
	public static boolean occupiedEi_A2C=false;
	public static boolean occupiedEr_A2C=false;
	public static boolean occupiedAcs=false;
	
	public static void main(String[] args) {
		
		SpringApplication.run(BackOfficeApplication.class, args);
	    
	        
	}

}
