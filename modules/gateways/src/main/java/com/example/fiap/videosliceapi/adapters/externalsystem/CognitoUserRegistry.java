package com.example.fiap.videosliceapi.adapters.externalsystem;

import com.example.fiap.videosliceapi.adapters.auth.UserGroup;
import com.example.fiap.videosliceapi.domain.external.UserRegistry;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class CognitoUserRegistry implements UserRegistry, AutoCloseable {

    private final AwsConfig awsConfig;
    private final CognitoIdentityProviderClient cognitoClient;

    public CognitoUserRegistry(Environment environment) {
        this.awsConfig = AwsConfig.loadFromEnv(environment);

        this.cognitoClient = CognitoIdentityProviderClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    public String registerUser(String name, String email, UserGroup group, String password) {
        List<AttributeType> userAttrsList = new ArrayList<>();

        userAttrsList.add(AttributeType.builder()
                .name("name")
                .value(name)
                .build());

        userAttrsList.add(AttributeType.builder()
                .name("email")
                .value(email)
                .build());

        SignUpRequest signUpRequest = SignUpRequest.builder()
                .userAttributes(userAttrsList)
                .username(email)
                .clientId(awsConfig.getCognitoClientId())
                .password(password)
                .secretHash(secretHash(email, awsConfig.getCognitoClientId(), awsConfig.getCognitoClientSecret()))
                .build();

        SignUpResponse signUpResponse = cognitoClient.signUp(signUpRequest);

        if (!Boolean.TRUE.equals(signUpResponse.userConfirmed())) {
            AdminConfirmSignUpRequest confirmSignUpRequest = AdminConfirmSignUpRequest.builder()
                    .username(email)
                    .userPoolId(awsConfig.getCognitoUserPoolId())
                    .build();

            cognitoClient.adminConfirmSignUp(confirmSignUpRequest);
        }

        AdminAddUserToGroupRequest addUserToGroupRequest = AdminAddUserToGroupRequest.builder()
                .username(email)
                .userPoolId(awsConfig.getCognitoUserPoolId())
                .groupName(group.name())
                .build();

        cognitoClient.adminAddUserToGroup(addUserToGroupRequest);

        return signUpResponse.userSub();
    }

    public String getUserEmail(String sub) {
        AdminGetUserRequest getUserRequest = AdminGetUserRequest.builder()
                .username(sub)
                .userPoolId(awsConfig.getCognitoUserPoolId())
                .build();
    
        AdminGetUserResponse getUserResponse = cognitoClient.adminGetUser(getUserRequest);
    
        return getUserResponse.userAttributes().stream()
                .filter(attribute -> "email".equals(attribute.name()))
                .findFirst()
                .map(AttributeType::value)
                .orElseThrow(() -> new IllegalArgumentException("Email not found for user with sub: " + sub));
    }

    private static String secretHash(String userName, String clientId, String clientSecret) {
        Mac hmac = HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_SHA_256, clientSecret.getBytes(StandardCharsets.UTF_8));
        hmac.update((userName + clientId).getBytes(StandardCharsets.UTF_8));
        byte[] finalMac = hmac.doFinal();

        return Base64.getEncoder().encodeToString(finalMac);
    }

    @Override
    public void close() throws Exception {
        cognitoClient.close();
    }
}
