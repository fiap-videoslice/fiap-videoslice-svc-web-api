package com.example.fiap.archburgers.adapters.auth;

import com.example.fiap.archburgers.adapters.externalsystem.AwsConfig;
import com.example.fiap.archburgers.adapters.testUtils.StaticEnvironment;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CognitoJwksApiIT {

    @Test
    public void getKnownSignaturesJson() throws Exception {
        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setBody(SAMPLE_DATA));

            server.start();
            HttpUrl baseUrl = server.url("/00000000/.well-known/jwks.json");

            CognitoJwksApi cognitoJwksApi = getCognitoJwksApi(baseUrl);

            String json = cognitoJwksApi.getKnownSignaturesJson();

            assertThat(json).isEqualTo(SAMPLE_DATA);
        }
    }

    @Test
    public void getKnownSignaturesJson_errorResponse() throws Exception {
        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setResponseCode(404));

            server.start();
            HttpUrl baseUrl = server.url("/00000000/.well-known/jwks.json");

            CognitoJwksApi cognitoJwksApi = getCognitoJwksApi(baseUrl);

            assertThatThrownBy(cognitoJwksApi::getKnownSignaturesJson)
                    .hasMessageContaining("Error in identity signature info request");
        }
    }

    private static @NotNull CognitoJwksApi getCognitoJwksApi(HttpUrl baseUrl) {
        Environment env = new StaticEnvironment(Map.of(
                "archburgers.integration.aws.region", "us-east-1",
                "archburgers.integration.cognito.userPoolId", "111111111",
                "archburgers.integration.cognito.clientId", "1234567",
                "archburgers.integration.cognito.clientSecret", "00secret99999",

                "archburgers.integration.cognito.overrideJwksUrl", baseUrl.toString()
        ));

        return new CognitoJwksApi(env);
    }

    private static final String SAMPLE_DATA = """
            {
              "keys": [
                {
                  "alg": "RS256",
                  "e": "AQAB",
                  "kid": "3/DBNUA8Xeh3u4EFsz/I6RNu9Y6I2PfjOaEQ0d2TWEg=",
                  "kty": "RSA",
                  "n": "tuz4IHRCUfCYkRC3h9fxGXkufLwKm14KAYtu-tvQ0B1ifsGxIpXWXEGhvXxN65gxc2KENgwqK8BZ9zKRKxRLUly9O-6-eX9mHTgtKiB94wMCkEc83fYSlZ6IEQ7fdjJZJP8lTHx3Q4dOkytrKZ804nvlT_69L46Y_387zAdKs8bOT_3LcK7gN3E5pTMlKNJAUaPyptu8hoGZfk1KmxigrLGNn5OZ3MwHncScFrhxzy4Nd-bpGsSSwZdL6MWxykdsLKqVRWqgpyQQoWEEIVRzdzGyli7951tBaptmu95jdXh-4LRH_z0_mdRe2allzfD-e5rR6FnQrHyA8LvOKkcYaQ",
                  "use": "sig"
                }
              ]
            }
            """;
}
