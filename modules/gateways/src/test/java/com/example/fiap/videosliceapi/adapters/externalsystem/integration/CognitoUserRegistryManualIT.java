package com.example.fiap.videosliceapi.adapters.externalsystem.integration;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.videosliceapi.adapters.auth.UserGroup;
import com.example.fiap.videosliceapi.adapters.externalsystem.CognitoUserRegistry;
import com.example.fiap.videosliceapi.testUtils.StaticEnvironment;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CognitoUserRegistryManualIT {
    private CognitoUserRegistry cognitoUserRegistry;

    @BeforeEach
    void setUp() {
        System.setProperty("aws.accessKeyId", "ASIAQ3GU3....");
        System.setProperty("aws.secretAccessKey", "Tgk5w5hqZ/GL5.....");
        System.setProperty("aws.sessionToken", "IQoJb3JpZ2luX2VjEM7//////////wEaCXVzLXdl.......");

        cognitoUserRegistry = new CognitoUserRegistry(new StaticEnvironment(Map.of(
                "videosliceapi.integration.aws.region", "us-east-1",
                "videosliceapi.integration.cognito.userPoolId", "us-east-1_sRGHlV5xO",
                "videosliceapi.integration.cognito.clientId", "7hcrtevul8dljf000000000000",
                "videosliceapi.integration.cognito.clientSecret", "pvf4ulup3eimkc71i9nv2vn0000000000000000000000"
        )));
    }

    @Test
    @Disabled("This is not an automated test. It depends on a real Cognito instance and is intended to be invoked " +
              "manually for tests during development")
    public void registerUser() {
        String newUserId = cognitoUserRegistry.registerUser("John Doe",
                "john@fiap.example.com", UserGroup.Admin, "123456789");
    
        System.out.println("Created user with ID: " + newUserId);
    }

    @Disabled("This is not an automated test. It depends on a real Cognito instance and is intended to be invoked " +
              "manually for tests during development")
    @Test
    public void getUserEmail() {
        String email = cognitoUserRegistry.getUserEmail("f4881488-1091-70b1-21fe-6dc5cce9c313");

        System.out.println("Found email: " + email);
    }
}