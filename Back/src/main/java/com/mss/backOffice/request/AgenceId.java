package com.mss.backOffice.request;

import java.io.Serializable;


public class AgenceId implements Serializable {

    private Integer codeAgence;


    private String initial;

    public Integer getCodeAgence() {
        return codeAgence;
    }

    public void setCodeAgence(Integer codeAgence) {
        this.codeAgence = codeAgence;
    }

    public String getInitial() {
        return initial;
    }

    public void setInitial(String initial) {
        this.initial = initial;
    }
}
