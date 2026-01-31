package com.mss.backOffice.request;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.mss.unified.entities.User;

public class UserPrincipal implements UserDetails {

	private static final long serialVersionUID = 1L;

	private String id;

	private String firstname;

	private String lastname;

	private String email;

	private String password;
	
	private Date lastPasswordResetDate;

	private Collection<? extends GrantedAuthority> authorities;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

		return authorities;
	}

	@Override
	public String getPassword() {

		return password;
	}

	@Override
	public String getUsername() {

		return email;
	}

	@Override
	public boolean isAccountNonExpired() {

		return true;
	}

	@Override
	public boolean isAccountNonLocked() {

		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {

		return true;
	}

	@Override
	public boolean isEnabled() {

		return true;
	}

	public Date getLastPasswordResetDate() {
		return lastPasswordResetDate;
	}

	public void setLastPasswordResetDate(Date lastPasswordResetDate) {
		this.lastPasswordResetDate = lastPasswordResetDate;
	}

	public UserPrincipal(String id, String firstname, String lastname, String usermail, String password,
                         Collection<? extends GrantedAuthority> authorities) {
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = usermail;
		this.password = password;
		this.authorities = authorities;
	}

	public static UserPrincipal create(User user) {

		List<GrantedAuthority> authorities = user.getRoles().stream().map(role ->

		new SimpleGrantedAuthority(role.toString())

		).collect(Collectors.toList());

		return new UserPrincipal(String.valueOf(user.getUserCode()), user.getFirstName(), user.getLastName(), user.getUserEmail(),
				user.getPassword(), authorities);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String usermail) {
		this.email = usermail;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}

