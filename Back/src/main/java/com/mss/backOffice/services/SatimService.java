package com.mss.backOffice.services;

import com.mss.backOffice.connection.SatimConnection;
import com.mss.unified.entities.SwitchTransaction;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Service
public class SatimService {
	
	@Autowired
	private SatimConnection conn;

	RestTemplate restTemplate = new RestTemplate();

	public String sendMessageSignOn()
			throws KeyManagementException, NoSuchAlgorithmException {

		CloseableHttpClient httpClient = conn.getConnection();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);
		RestTemplate restTemplate = new RestTemplate(requestFactory);
//		HttpHeaders requestHeaders = new HttpHeaders();
//	
//
//		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
//		requestHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      
//
//		HttpEntity<SwitchTransaction> entity = new HttpEntity<SwitchTransaction>(switchTrans, requestHeaders);

		return restTemplate.exchange(conn.path+"sendSignOn", HttpMethod.GET, null,  String.class)
				.getBody();


	}
	
	
	public String sendMessageSignOff()
			throws KeyManagementException, NoSuchAlgorithmException {

		CloseableHttpClient httpClient = conn.getConnection();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);
		RestTemplate restTemplate = new RestTemplate(requestFactory);
//		HttpHeaders requestHeaders = new HttpHeaders();
//	
//
//		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
//		requestHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      

		//HttpEntity<SwitchTransaction> entity = new HttpEntity<SwitchTransaction>(switchTrans, requestHeaders);

		return restTemplate.exchange(conn.path+"sendSignOff", HttpMethod.GET, null,  String.class)
				.getBody();


	}
	
	


}

