package com.mss.backOffice.request;

public class GetTokenAmplitudeResponse {
	private int id;
	private String firstName;
	private String lastName;
	private String username;
	private String password;
	private String role;
	private String token;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	@Override
	public String toString() {
		return "GetTokenAmplitudeResponse [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName
				+ ", username=" + username + ", password=" + password + ", role=" + role + ", token=" + token + "]";
	}
	
	
	
}
