package com.knusdp.SmartLedger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "crypto")
public class CryptoProperties {
    private String secretKey;
    private String initVector;

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public String getInitVector() { return initVector; }
    public void setInitVector(String initVector) { this.initVector = initVector; }
}