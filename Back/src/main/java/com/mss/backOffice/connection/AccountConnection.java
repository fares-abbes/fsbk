package com.mss.backOffice.connection;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;



@Component
@PropertySource("classpath:application.properties")
public class AccountConnection {
	
    @Value("${timeout}")
    private int timeout;
    //path Test
	public final String path =  "https://api_hom.fransabank.dz:8181/";
	//PathProd
//	public final String path =  "https://apip1.fransabank.dz/";
	public CloseableHttpClient getConnection() throws NoSuchAlgorithmException, KeyManagementException
	{
		SSLContext ctx = SSLContext.getInstance("TLS");
        X509TrustManager tm = new X509TrustManager() {

            public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        ctx.init(null, new TrustManager[]{tm}, null);
        
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        
        
        RequestConfig config = RequestConfig.custom()
          .setConnectTimeout(timeout)
          .setConnectionRequestTimeout(timeout )
          .setSocketTimeout(timeout).build();
        
        
//        CloseableHttpClient httpClient = HttpClients.custom()
//                .setSSLSocketFactory(csf)
//                
//                .build();
//        
        CloseableHttpClient client = 
        		  HttpClientBuilder.create().setDefaultRequestConfig(config).setSSLSocketFactory(csf).build();

        return client;
	}
}
