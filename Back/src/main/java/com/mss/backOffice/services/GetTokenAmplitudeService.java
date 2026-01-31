package com.mss.backOffice.services;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.mss.backOffice.connection.AccountConnection;
import com.mss.backOffice.request.GetTokenAmplitudeResponse;
import com.mss.backOffice.request.UserAmplitude;


@Component
public class GetTokenAmplitudeService {
	@Autowired
	private AccountConnection conn;
	@Autowired
	private PropertyService propertyService;
	
	RestTemplate restTemplate = new RestTemplate();

	private final Logger logger = LoggerFactory.getLogger(GetTokenAmplitudeService.class);

	public GetTokenAmplitudeResponse getToken()
			throws KeyManagementException, NoSuchAlgorithmException {

		CloseableHttpClient httpClient = conn.getConnection();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);
		RestTemplate restTemplate = new RestTemplate(requestFactory);
		HttpHeaders requestHeaders = new HttpHeaders();

      
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		requestHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	//	requestHeaders.add("Authorization", "authorizationHeader");
       

		  UserAmplitude user =new UserAmplitude();

		  user.setUsername(propertyService.getAmplitudeAuthUser());
		  user.setPassword(propertyService.getAmplitudeAuthPassword()); 		  
		HttpEntity<UserAmplitude> entity = new HttpEntity<>(user, requestHeaders);
		logger.info("**********getting token************");
		return restTemplate.exchange(conn.path+"Oauth2/Users/Authenticate", HttpMethod.POST, entity, GetTokenAmplitudeResponse.class)
				.getBody();

	}
}
