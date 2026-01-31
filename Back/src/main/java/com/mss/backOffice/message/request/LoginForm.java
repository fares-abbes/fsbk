package com.mss.backOffice.message.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

public class LoginForm {

    //public PasswordEncoder passwordEncoder;
    @NotBlank
    @Size(min=3, max = 60)
    private String userName;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginForm{" +
            "userName='" + userName + '\'' +
            ", password='" + password + '\'' +
            '}';
    }
}