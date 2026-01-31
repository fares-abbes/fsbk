package com.mss.backOffice.message.response;


import java.util.Date;
import java.util.List;
import java.util.Map;

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String user;
    private List< String > authorities;
    private Date expiration;
    private Map<String,Integer> permissions;
    private Integer userId;
    private String firstLogin;
    public
    JwtResponse(String firstLogin,String token, String username , List < String > authorities,Date expiration,Map<String,Integer> permissions,Integer userId) {
        this.token = token;
        this.user = username;
        this.authorities = authorities;
        this.expiration=expiration;
        this.permissions=permissions;
        this.userId=userId;
        this.firstLogin=firstLogin;
    }

    public JwtResponse(String token, String user, Date expiration,
        Map<String, Integer> permissions) {
        this.token = token;
        this.user = user;
        this.expiration = expiration;
        this.permissions = permissions;
    }

    public
    Date getExpiration() {
        return expiration;
    }

    public
    void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Map<String, Integer> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, Integer> permissions) {
        this.permissions = permissions;
    }

    public
    List < String > getAuthorities() {
        return authorities;
    }

    public
    void setAuthorities(List < String > authorities) {
        this.authorities = authorities;
    }

    public
    String getToken() {
        return token;
    }

    public
    void setToken(String token) {
        this.token = token;
    }

    public
    String getType() {
        return type;
    }

    public
    void setType(String type) {
        this.type = type;
    }

//	public Integer getUserId() {
//		return userId;
//	}
//
//	public void setUserId(Integer userId) {
//		this.userId = userId;
//	}

	public String getFirstLogin() {
		return firstLogin;
	}

	public void setFirstLogin(String firstLogin) {
		this.firstLogin = firstLogin;
	}
    
}
