package com.example.fiap.archburgers.adapters.externalsystem;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.archburgers.testUtils.StaticEnvironment;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AwsConfigTest {

    @Test
    void getAttributes() {
        AwsConfig awsConfig = AwsConfig.loadFromEnv(new StaticEnvironment(Map.of(
                "archburgers.integration.aws.region", "us-east-1",
                "archburgers.integration.cognito.userPoolId", "111111111",
                "archburgers.integration.cognito.clientId", "1234567",
                "archburgers.integration.cognito.clientSecret", "00secret99999"
        )));

        assertThat(awsConfig.getAwsRegion()).isEqualTo("us-east-1");
        assertThat(awsConfig.getCognitoUserPoolId()).isEqualTo("111111111");
        assertThat(awsConfig.getCognitoClientId()).isEqualTo("1234567");
        assertThat(awsConfig.getCognitoClientSecret()).isEqualTo("00secret99999");
    }

    @Test
    void loadFromEnv_error_regionNotSet() {
        StaticEnvironment env = new StaticEnvironment(Map.of());

        assertThatThrownBy(() -> AwsConfig.loadFromEnv(env))
                .hasMessageContaining("archburgers.integration.aws.region not set");
    }

    @Test
    void loadFromEnv_error_userPoolIdNotSet() {
        StaticEnvironment env = new StaticEnvironment(Map.of(
                "archburgers.integration.aws.region", "us-east-1"
        ));

        assertThatThrownBy(() -> AwsConfig.loadFromEnv(env))
                .hasMessageContaining("archburgers.integration.cognito.userPoolId not set");
    }

    @Test
    void loadFromEnv_error_clientIdNotSet() {
        StaticEnvironment env = new StaticEnvironment(Map.of(
                "archburgers.integration.aws.region", "us-east-1",
                "archburgers.integration.cognito.userPoolId", "111111111"
        ));

        assertThatThrownBy(() -> AwsConfig.loadFromEnv(env))
                .hasMessageContaining("archburgers.integration.cognito.clientId not set");
    }

    @Test
    void loadFromEnv_error_clientSecretNotSet() {
        StaticEnvironment env = new StaticEnvironment(Map.of(
                "archburgers.integration.aws.region", "us-east-1",
                "archburgers.integration.cognito.userPoolId", "111111111",
                "archburgers.integration.cognito.clientId", "1234567"
        ));

        assertThatThrownBy(() -> AwsConfig.loadFromEnv(env))
                .hasMessageContaining("archburgers.integration.cognito.clientSecret not set");
    }
}