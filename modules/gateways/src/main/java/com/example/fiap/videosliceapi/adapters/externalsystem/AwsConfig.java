package com.example.fiap.videosliceapi.adapters.externalsystem;

import com.example.fiap.videosliceapi.domain.utils.StringUtils;
import org.springframework.core.env.Environment;

public class AwsConfig {
    private final String awsRegion;
    private final String cognitoUserPoolId;
    private final String cognitoClientId;
    private final String cognitoClientSecret;

    private AwsConfig(String awsRegion, String cognitoUserPoolId, String cognitoClientId, String cognitoClientSecret) {
        this.awsRegion = awsRegion;
        this.cognitoUserPoolId = cognitoUserPoolId;
        this.cognitoClientId = cognitoClientId;
        this.cognitoClientSecret = cognitoClientSecret;
    }

    public static AwsConfig loadFromEnv(Environment environment) {
        String awsRegion = environment.getProperty("videosliceapi.integration.aws.region");
        String userPoolId = environment.getProperty("videosliceapi.integration.cognito.userPoolId");
        String cognitoClientId = environment.getProperty("videosliceapi.integration.cognito.clientId");
        String cognitoClientSecret = environment.getProperty("videosliceapi.integration.cognito.clientSecret");

        if (StringUtils.isEmpty(awsRegion))
            throw new IllegalStateException("videosliceapi.integration.aws.region not set");
        if (com.example.fiap.videosliceapi.domain.utils.StringUtils.isEmpty(userPoolId))
            throw new IllegalStateException("videosliceapi.integration.cognito.userPoolId not set");
        if (StringUtils.isEmpty(cognitoClientId))
            throw new IllegalStateException("videosliceapi.integration.cognito.clientId not set");
        if (StringUtils.isEmpty(cognitoClientSecret))
            throw new IllegalStateException("videosliceapi.integration.cognito.clientSecret not set");

        return new AwsConfig(awsRegion, userPoolId, cognitoClientId, cognitoClientSecret);
    }

    public String getAwsRegion() {
        return awsRegion;
    }

    public String getCognitoUserPoolId() {
        return cognitoUserPoolId;
    }

    public String getCognitoClientId() {
        return cognitoClientId;
    }

    public String getCognitoClientSecret() {
        return cognitoClientSecret;
    }
}
