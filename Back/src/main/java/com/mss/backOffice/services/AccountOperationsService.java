package com.mss.backOffice.services;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.mss.backOffice.connection.AccountConnection;
import com.mss.backOffice.request.AccountDetails;
import com.mss.backOffice.request.AccountOperation;

import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Service
public class AccountOperationsService {
	@Autowired
	private AccountConnection conn;

	
	RestTemplate restTemplate = new RestTemplate();

    private static final Logger logger = LoggerFactory.getLogger(AccountOperationsService.class);
 

	public List<AccountOperation> getAccountOperations(String rib,String token)
			throws KeyManagementException, NoSuchAlgorithmException {

		CloseableHttpClient httpClient = conn.getConnection();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);
		RestTemplate restTemplate = new RestTemplate(requestFactory);
		HttpHeaders requestHeaders = new HttpHeaders();
        MultiValueMap<String, String> parametersMap = new LinkedMultiValueMap <>();
        parametersMap.add("rib", rib);

		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		requestHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        requestHeaders.set("Authorization", "Bearer "+token);
       
		
		HttpEntity<Void> entity = new HttpEntity<>(requestHeaders);

		return restTemplate.exchange(conn.path+"Api/Account/Releve/{rib}", HttpMethod.GET, entity,  new ParameterizedTypeReference<List<AccountOperation>>(){},rib)
				.getBody();

	}

}
