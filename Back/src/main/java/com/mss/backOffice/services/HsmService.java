package com.mss.backOffice.services;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.mss.backOffice.connection.HsmConnection;
import com.mss.unified.references.CvvRequest;
import com.mss.unified.references.PvvRequest;
import com.mss.unified.references.RandomPinRequest;

import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Service
public class HsmService {
	@Autowired
	private HsmConnection conn;

	
	RestTemplate restTemplate = new RestTemplate();

    private static final Logger logger = LoggerFactory.getLogger(HsmService.class);
 

	public String generateCvv(String pan,String expiryDate,String serviceCode, String binCode,boolean isVisa)
			throws KeyManagementException, NoSuchAlgorithmException {

		CloseableHttpClient httpClient = conn.getConnection();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);
		RestTemplate restTemplate = new RestTemplate(requestFactory);
		HttpHeaders requestHeaders = new HttpHeaders();
      

		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		requestHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      
		CvvRequest cvvRequest=new CvvRequest();
		cvvRequest.setCardNumber(pan);
		cvvRequest.setBinCode(binCode);
		cvvRequest.setExpiryDate(expiryDate);
		cvvRequest.setServiceCode(serviceCode);
		cvvRequest.setVisa(isVisa);
		
		HttpEntity<CvvRequest> entity = new HttpEntity<CvvRequest>(cvvRequest, requestHeaders);

		return restTemplate.exchange(conn.path+"generateCvvFSBK", HttpMethod.POST, entity,  String.class)
				.getBody();


	}
	
	
	
	
	
	
	
	
	public String generateRandomPin(String pan)
			throws KeyManagementException, NoSuchAlgorithmException {

		CloseableHttpClient httpClient = conn.getConnection();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);
		RestTemplate restTemplate = new RestTemplate(requestFactory);
		HttpHeaders requestHeaders = new HttpHeaders();
      

		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		requestHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      
		RandomPinRequest request=new RandomPinRequest();
		request.setCardNumber(pan);
		

		
		HttpEntity<RandomPinRequest> entity = new HttpEntity<RandomPinRequest>(request, requestHeaders);

		return restTemplate.exchange(conn.path+"generateRandomPinFSBK", HttpMethod.POST, entity,  String.class)
				.getBody();


	}
	
	
	
	public String generatePvv(String pan,String randomPin,String binCode,boolean isVisa)
			throws KeyManagementException, NoSuchAlgorithmException {

		CloseableHttpClient httpClient = conn.getConnection();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);
		RestTemplate restTemplate = new RestTemplate(requestFactory);
		HttpHeaders requestHeaders = new HttpHeaders();
      

		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		requestHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      
		PvvRequest request=new PvvRequest();
		request.setCardNumber(pan);
		request.setBinCode(binCode);
		request.setRandomPin(randomPin);
		request.setVisa(isVisa);

		
		HttpEntity<PvvRequest> entity = new HttpEntity<PvvRequest>(request, requestHeaders);

		return restTemplate.exchange(conn.path+"generatePvvFSBK", HttpMethod.POST, entity,  String.class)
				.getBody();


	}
	
	
	public String generatePinBlock(String pan,String randomPin,String binCode,boolean isVisa)
			throws KeyManagementException, NoSuchAlgorithmException {

		CloseableHttpClient httpClient = conn.getConnection();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);
		RestTemplate restTemplate = new RestTemplate(requestFactory);
		HttpHeaders requestHeaders = new HttpHeaders();
      

		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		requestHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      
		PvvRequest request=new PvvRequest();
		request.setCardNumber(pan);
		request.setBinCode(binCode);
		request.setRandomPin(randomPin);
		request.setVisa(isVisa);

		
		HttpEntity<PvvRequest> entity = new HttpEntity<PvvRequest>(request, requestHeaders);

		return restTemplate.exchange(conn.path+"generatePinBlockFSBK", HttpMethod.POST, entity,  String.class)
				.getBody();


	}

}
