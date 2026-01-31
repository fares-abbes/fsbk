package com.mss.backOffice.message.request;

import javax.persistence.Column;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

public class SignUpForm {


    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(max = 60)
    @Email
    private String email;
    
    private Set<String> role;



    @NotBlank
    @Size(min = 3, max = 50)
    private String firstName;
    @NotBlank
    @Size(min = 3, max = 50)
    private String lasName;

@NotNull
    private Integer bank;
@NotNull
    private Integer type;

private Integer idAgence;
private Integer idRegion;
private Integer idZone;

    public Integer getIdAgence() {
	return idAgence;
}

public void setIdAgence(Integer idAgence) {
	this.idAgence = idAgence;
}

public Integer getIdRegion() {
	return idRegion;
}

public void setIdRegion(Integer idRegion) {
	this.idRegion = idRegion;
}

public Integer getIdZone() {
	return idZone;
}

public void setIdZone(Integer idZone) {
	this.idZone = idZone;
}

	public Integer getType() {
	return type;
}

public void setType(Integer type) {
	this.type = type;
}

	public Integer getBank() {
        return bank;
    }

    public void setBank(Integer bank) {
        this.bank = bank;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLasName() {
        return lasName;
    }

    public void setLasName(String lasName) {
        this.lasName = lasName;
    }



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "SignUpForm{" +
            "username='" + username + '\'' +
            ", email='" + email + '\'' +
            ", role=" + role +
            ", firstName='" + firstName + '\'' +
            ", lasName='" + lasName + '\'' +
            ", bank=" + bank +
            '}';
    }

    public Set<String> getRole() {
    	return this.role;
    }
    
    public void setRole(Set<String> role) {
    	this.role = role;
    }
}