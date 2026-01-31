package com.mss.backOffice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Tc45Config {

    @Value("${tc45.expected.prefix}")
    private String expectedPrefix;

    public String getExpectedPrefix() {
        return expectedPrefix;
    }
}