package com.example.fiap.videosliceapi.adapters.auth;

import io.jsonwebtoken.Clock;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DefaultUserTokenParserTest {
    private CognitoJwksApi cognitoJwksApi;
    private Clock expirationCheckClock;

    private DefaultUserTokenParser parser;

    @BeforeEach
    void setUp() {
        cognitoJwksApi = mock(CognitoJwksApi.class);

        expirationCheckClock = mock(Clock.class);

        parser = new DefaultUserTokenParser(cognitoJwksApi, expirationCheckClock);
    }

    @Test
    void verifyLoggedUser_headerNotPresent() {
        var user = parser.verifyLoggedUser(new HttpHeaders());

        assertThat(user.authError()).isEqualTo("Authorization header is missing");
        assertThat(user.authenticated()).isFalse();

        assertThrows(IllegalStateException.class, user::getName);
        assertThrows(IllegalStateException.class, user::getEmail);
        assertThrows(IllegalStateException.class, user::getGroup);
        assertThrows(IllegalStateException.class, user::idToken);
    }

    @Test
    void verifyLoggedUser_headerInvalidFormat() {
        HttpHeaders headers = new HttpHeaders();
        headers.put("Authorization", List.of("Not a valid Bearer token"));

        var user = parser.verifyLoggedUser(headers);

        assertThat(user.authError()).isEqualTo("Authorization header is invalid");
        assertThat(user.authenticated()).isFalse();

        assertThrows(IllegalStateException.class, user::getUserId);
        assertThrows(IllegalStateException.class, user::getName);
        assertThrows(IllegalStateException.class, user::getEmail);
        assertThrows(IllegalStateException.class, user::getGroup);
        assertThrows(IllegalStateException.class, user::idToken);
    }

    @Test
    void verifyLoggedUser_ok() {
        when(cognitoJwksApi.getKnownSignaturesJson()).thenReturn(EXAMPLE_SIGNATURES_JSON);

        when(expirationCheckClock.now()).thenReturn(new Date(1737321360000L)); // Fixed time before the expiration of the example token

        String token = "eyJraWQiOiJsWndMV01CQ0tNbTFKYTR1VVFSZXJHZ05CdkNWaVpITkplbnZJNVlydVljPSIsImFsZyI6IlJTMjU2In0" +
                       ".eyJzdWIiOiJiNGU4YjRjOC1iMGUxLTcwOTctZDgwZi00NjI1NmU5ZjgxODkiLCJjb2duaXRvOmdyb3VwcyI6WyJVc2VyIl0sImlzcyI6Imh0dHBzOlwvXC9jb2duaXRvLWlkcC51cy1lYXN0LTEuYW1hem9uYXdzLmNvbVwvdXMtZWFzdC0xX1dPenNQZjlIOSIsImNvZ25pdG86dXNlcm5hbWUiOiJiN" +
                       "GU4YjRjOC1iMGUxLTcwOTctZDgwZi00NjI1NmU5ZjgxODkiLCJvcmlnaW5fanRpIjoiYTQ2ODIyZDItYzU0MC00MDkxLWJjYzItMTIwNDNlOTdlYjA2IiwiYXVkIjoiNjN1dXVlcWphODNudTFrc3QzcTQxaW5wcGkiLCJldmVudF9pZCI6IjllMjJkZDExLWVkOTctNDZhOS04ZTg1LTk2OTc3NGU1YWU1" +
                       "ZSIsInRva2VuX3VzZSI6ImlkIiwiYXV0aF90aW1lIjoxNzM3MzIxMDgxLCJuYW1lIjoiUmVndWxhciBVc2VyIiwiZXhwIjoxNzM3MzI0NjgxLCJpYXQiOjE3MzczMjEwODEsImp0aSI6ImI2MTBjNjI1LTUyNWEtNDA0Ny1iMTZmLTBjYTU3MjIzOTQ2OSIsImVtYWlsIjoidmlkZW9zbGljZXVzZXJAZmlhcC5leGFtcGxlLmNvbSJ9" +
                       ".S3M-8P4GCbx-eiSi1taP3-oW4d6qEYxVeHCHyWT3dhoMhAl26zPQ7eedH4khUvjNeTPC5yqb3WyiKmqkAFfmLsnY22_onJJ6doSkCSakPI8_pRmr9Bu3yos1PthPlVLYCiIhn3k4JjqUWQuj-7u5qZlKtTT5jjB9M2fNHI7rhqDAgTBPFCCRG7H5-FZ3ZTFtb6XxRNXu1RbY-0119eaSxwvtyq1JEB5qKxoyr3wsOzYI8Ucy1gvhFYFFzDscrnYgn2IF3X-g2TDGLsNN8jHzRNQp4gq40CLsWk4iFQqeqa7I-in2SmGtSyDSwPPNpJefST0tbfwzAzaBPbso3OHLVw";

        HttpHeaders headers = new HttpHeaders();
        headers.put("Authorization", List.of("Bearer " + token));

        var user = parser.verifyLoggedUser(headers);

        assertThat(user.authError()).isNull();
        assertThat(user.authenticated()).isTrue();
        assertThat(user.getUserId()).isEqualTo("b4e8b4c8-b0e1-7097-d80f-46256e9f8189");
        assertThat(user.getName()).isEqualTo("Regular User");
        assertThat(user.getEmail()).isEqualTo("videosliceuser@fiap.example.com");
        assertThat(user.getGroup()).isEqualTo(UserGroup.User);
        assertThat(user.idToken()).isEqualTo(token);

        verify(cognitoJwksApi, times(1)).getKnownSignaturesJson();
    }

    @Test
    void verifyLoggedUser_ok_keyCacheUsed() {
        when(cognitoJwksApi.getKnownSignaturesJson()).thenReturn(EXAMPLE_SIGNATURES_JSON);

        when(expirationCheckClock.now()).thenReturn(new Date(1737321360000L)); // Fixed time before the expiration of the example token

        String token = "eyJraWQiOiJsWndMV01CQ0tNbTFKYTR1VVFSZXJHZ05CdkNWaVpITkplbnZJNVlydVljPSIsImFsZyI6IlJTMjU2In0" +
                       ".eyJzdWIiOiJiNGU4YjRjOC1iMGUxLTcwOTctZDgwZi00NjI1NmU5ZjgxODkiLCJjb2duaXRvOmdyb3VwcyI6WyJVc2VyIl0sImlzcyI6Imh0dHBzOlwvXC9jb2duaXRvLWlkcC51cy1lYXN0LTEuYW1hem9uYXdzLmNvbVwvdXMtZWFzdC0xX1dPenNQZjlIOSIsImNvZ25pdG86dXNlcm5hbWUiOiJiN" +
                       "GU4YjRjOC1iMGUxLTcwOTctZDgwZi00NjI1NmU5ZjgxODkiLCJvcmlnaW5fanRpIjoiYTQ2ODIyZDItYzU0MC00MDkxLWJjYzItMTIwNDNlOTdlYjA2IiwiYXVkIjoiNjN1dXVlcWphODNudTFrc3QzcTQxaW5wcGkiLCJldmVudF9pZCI6IjllMjJkZDExLWVkOTctNDZhOS04ZTg1LTk2OTc3NGU1YWU1" +
                       "ZSIsInRva2VuX3VzZSI6ImlkIiwiYXV0aF90aW1lIjoxNzM3MzIxMDgxLCJuYW1lIjoiUmVndWxhciBVc2VyIiwiZXhwIjoxNzM3MzI0NjgxLCJpYXQiOjE3MzczMjEwODEsImp0aSI6ImI2MTBjNjI1LTUyNWEtNDA0Ny1iMTZmLTBjYTU3MjIzOTQ2OSIsImVtYWlsIjoidmlkZW9zbGljZXVzZXJAZmlhcC5leGFtcGxlLmNvbSJ9" +
                       ".S3M-8P4GCbx-eiSi1taP3-oW4d6qEYxVeHCHyWT3dhoMhAl26zPQ7eedH4khUvjNeTPC5yqb3WyiKmqkAFfmLsnY22_onJJ6doSkCSakPI8_pRmr9Bu3yos1PthPlVLYCiIhn3k4JjqUWQuj-7u5qZlKtTT5jjB9M2fNHI7rhqDAgTBPFCCRG7H5-FZ3ZTFtb6XxRNXu1RbY-0119eaSxwvtyq1JEB5qKxoyr3wsOzYI8Ucy1gvhFYFFzDscrnYgn2IF3X-g2TDGLsNN8jHzRNQp4gq40CLsWk4iFQqeqa7I-in2SmGtSyDSwPPNpJefST0tbfwzAzaBPbso3OHLVw";

        HttpHeaders headers = new HttpHeaders();
        headers.put("Authorization", List.of("Bearer " + token));

        var user = parser.verifyLoggedUser(headers);
        assertThat(user.getUserId()).isEqualTo("b4e8b4c8-b0e1-7097-d80f-46256e9f8189");

        user = parser.verifyLoggedUser(headers);
        assertThat(user.getUserId()).isEqualTo("b4e8b4c8-b0e1-7097-d80f-46256e9f8189");

        verify(cognitoJwksApi, times(1)).getKnownSignaturesJson();
    }

    @Test
    void verifyLoggedUser_invalidHeader() {
        when(cognitoJwksApi.getKnownSignaturesJson()).thenReturn(EXAMPLE_SIGNATURES_JSON);

        when(expirationCheckClock.now()).thenReturn(new Date(1737321360000L)); // Fixed time before the expiration of the example token

        // Token alterado com uma assinatura incorreta
        String token = "eyJhbGciOiJSUzI1NiJ9" +
                       ".eyJzdWIiOiJiNGU4Yj" +
                       ".NpJefST0tbfwzAzaBPbso3OHLVw";

        HttpHeaders headers = new HttpHeaders();
        headers.put("authorization", List.of("bearer " + token));

        assertThatThrownBy(() -> parser.verifyLoggedUser(headers))
                .hasMessageContaining("Unexpected JWT header format. Missing kid");
    }

    @Test
    void verifyLoggedUser_assinaturaTokenInvalida() {
        when(cognitoJwksApi.getKnownSignaturesJson()).thenReturn(EXAMPLE_SIGNATURES_JSON);

        when(expirationCheckClock.now()).thenReturn(new Date(1737321360000L)); // Fixed time before the expiration of the example token

        // Token alterado com uma assinatura incorreta
        String token = "eyJraWQiOiJsWndMV01CQ0tNbTFKYTR1VVFSZXJHZ05CdkNWaVpITkplbnZJNVlydVljPSIsImFsZyI6IlJTMjU2In0" +
                       ".eyJzdWIiOiJiNGU4YjRjOC1iMGUxLTcwOTctZDgwZi00NjI1NmU5ZjgxODkiLCJjb2duaXRvOmdyb3VwcyI6WyJVc2VyIl0sImlzcyI6Imh0dHBzOlwvXC9jb2duaXRvLWlkcC51cy1lYXN0LTEuYW1hem9uYXdzLmNvbVwvdXMtZWFzdC0xX1dPenNQZjlIOSIsImNvZ25pdG86dXNlcm5hbWUiOiJiN" +
                       "GU4YjRjOC1iMGUxLTcwOTctZDgwZi00NjI1NmU5ZjgxODkiLCJvcmlnaW5fanRpIjoiYTQ2ODIyZDItYzU0MC00MDkxLWJjYzItMTIwNDNlOTdlYjA2IiwiYXVkIjoiNjN1dXVlcWphODNudTFrc3QzcTQxaW5wcGkiLCJldmVudF9pZCI6IjllMjJkZDExLWVkOTctNDZhOS04ZTg1LTk2OTc3NGU1YWU1" +
                       "ZSIsInRva2VuX3VzZSI6ImlkIiwiYXV0aF90aW1lIjoxNzM3MzIxMDgxLCJuYW1lIjoiUmVndWxhciBVc2VyIiwiZXhwIjoxNzM3MzI0NjgxLCJpYXQiOjE3MzczMjEwODEsImp0aSI6ImI2MTBjNjI1LTUyNWEtNDA0Ny1iMTZmLTBjYTU3MjIzOTQ2OSIsImVtYWlsIjoidmlkZW9zbGljZXVzZXJAZmlhcC5leGFtcGxlLmNvbSJ9" +
                       ".ASSINATURAinvalidaASSINATURAinvalidaASSINATURAinvalidaASSINATURAinvalidaASSINATURAinvalida_onJJ6doSkCSakPI8_pRmr9Bu3yos1PthPlVLYCiIhn3k4JjqUWQuj-7u5qZlKtTT5jjB9M2fNHI7rhqDAgTBPFCCRG7H5-FZ3ZTFtb6XxRNXu1RbY-0119eaSxwvtyq1JEB5qKxoyr3wsOzYI8Ucy1gvhFYFFzDscrnYgn2IF3X-g2TDGLsNN8jHzRNQp4gq40CLsWk4iFQqeqa7I-in2SmGtSyDSwPPNpJefST0tbfwzAzaBPbso3OHLVw";

        HttpHeaders headers = new HttpHeaders();
        headers.put("authorization", List.of("bearer " + token));

        var user = parser.verifyLoggedUser(headers);

        assertThat(user.authError()).startsWith("Erro ao validar IdToken: JWT signature does not match locally computed signature");
        assertThat(user.authenticated()).isFalse();
    }

    @Test
    void verifyLoggedUser_invalidServerSignaturesFile() {
        when(cognitoJwksApi.getKnownSignaturesJson()).thenReturn("NOT_A_VALID_JSON");

        when(expirationCheckClock.now()).thenReturn(new Date(1737321360000L)); // Fixed time before the expiration of the example token

        // Token alterado com uma assinatura incorreta
        String token = "eyJraWQiOiJsWndMV01CQ0tNbTFKYTR1VVFSZXJHZ05CdkNWaVpITkplbnZJNVlydVljPSIsImFsZyI6IlJTMjU2In0" +
                       ".eyJzdWIiOiJiNGU4YjRjOC1iMGUxLTcwOTctZDgwZi00NjI1NmU5ZjgxODkiLCJjb2duaXRvOmdyb3VwcyI6WyJVc2VyIl0sImlzcyI6Imh0dHBzOlwvXC9jb2duaXRvLWlkcC51cy1lYXN0LTEuYW1hem9uYXdzLmNvbVwvdXMtZWFzdC0xX1dPenNQZjlIOSIsImNvZ25pdG86dXNlcm5hbWUiOiJiN" +
                       "GU4YjRjOC1iMGUxLTcwOTctZDgwZi00NjI1NmU5ZjgxODkiLCJvcmlnaW5fanRpIjoiYTQ2ODIyZDItYzU0MC00MDkxLWJjYzItMTIwNDNlOTdlYjA2IiwiYXVkIjoiNjN1dXVlcWphODNudTFrc3QzcTQxaW5wcGkiLCJldmVudF9pZCI6IjllMjJkZDExLWVkOTctNDZhOS04ZTg1LTk2OTc3NGU1YWU1" +
                       "ZSIsInRva2VuX3VzZSI6ImlkIiwiYXV0aF90aW1lIjoxNzM3MzIxMDgxLCJuYW1lIjoiUmVndWxhciBVc2VyIiwiZXhwIjoxNzM3MzI0NjgxLCJpYXQiOjE3MzczMjEwODEsImp0aSI6ImI2MTBjNjI1LTUyNWEtNDA0Ny1iMTZmLTBjYTU3MjIzOTQ2OSIsImVtYWlsIjoidmlkZW9zbGljZXVzZXJAZmlhcC5leGFtcGxlLmNvbSJ9" +
                       ".S3M-8P4GCbx-eiSi1taP3-oW4d6qEYxVeHCHyWT3dhoMhAl26zPQ7eedH4khUvjNeTPC5yqb3WyiKmqkAFfmLsnY22_onJJ6doSkCSakPI8_pRmr9Bu3yos1PthPlVLYCiIhn3k4JjqUWQuj-7u5qZlKtTT5jjB9M2fNHI7rhqDAgTBPFCCRG7H5-FZ3ZTFtb6XxRNXu1RbY-0119eaSxwvtyq1JEB5qKxoyr3wsOzYI8Ucy1gvhFYFFzDscrnYgn2IF3X-g2TDGLsNN8jHzRNQp4gq40CLsWk4iFQqeqa7I-in2SmGtSyDSwPPNpJefST0tbfwzAzaBPbso3OHLVw";

        HttpHeaders headers = new HttpHeaders();
        headers.put("authorization", List.of("bearer " + token));

        assertThatThrownBy(() -> parser.verifyLoggedUser(headers))
                .hasMessageContaining("Error parsing response from Jwks endpoint");
    }

    @Test
    void invokeDefaultConstructor() {
        // The default constructor is not using on the tests because it does not get the controlled clock
        // for predictable tests
        // Invoking it here just for coverage
        new DefaultUserTokenParser(cognitoJwksApi);
    }

    @Language("JSON")
    private static final String EXAMPLE_SIGNATURES_JSON = """
            {
              "keys": [
                {
                  "alg": "RS256",
                  "e": "AQAB",
                  "kid": "lZwLWMBCKMm1Ja4uUQRerGgNBvCViZHNJenvI5YruYc=",
                  "kty": "RSA",
                  "n": "n6xMDre03muqInire2cIcjre4B1YQFKIscKUcdVWGJqFmjpjl4OTSQ3O9Veyx3hE7dzD0iBgs78TnL1iXufnNFywo_Ci79SCqyv108u7Drb9i8pFMyHhKpFsai6YpRRlcXIU02HobW9p6EOeEjZkQCivKpfdYaReLw2vadG9MYk4OsJl9Nj7JBs9uDfbTP3deGJRVpZbv6my7MzHVhNYwml-RgJaSe0qjP4kfUkCNQCH3xUXupHao7tzkNLO0tY9ecPB4GkLB9EYhmqOKojIeLHSfvhRpkLZDxf8HpiLI1YaKzm8FxVFgPmyv2CGrdB9yEpRQCH_vBWeJ8ZAGrjV8Q",
                  "use": "sig"
                },
                {
                  "alg": "RS256",
                  "e": "AQAB",
                  "kid": "9/3QmJRENNVmnsgHSLY8KzeGv/N14uMNo6Q40NQ4BuQ=",
                  "kty": "RSA",
                  "n": "wE_Ko75PU1iWKI54K8IvtuFa_RGkvcsXdOxTUZ7rwLa0z4QM_axbT8a3LnATBSVQZgVAqa1b73RugvwTagUptKirbxorSgL2r0gNaJyd7_cR408mV9bh0IdcH2L1_uRENXYH1ourBuQFSteIrKFDZgbDsS3g3yEn5sMEwtyM_Qsm--5i0-YWL4SyhLUAyfgAjcZhNepWMUQIpQcxtNpzlINNM-R_-qn9MzZqWzwtVFN6Dth2UtVBJR-ealHkwgXmxB65NFCfjHvM6zGibURxDacRn9xMQKgWdC91y-2qhS_LYz9NepPFx5H7SBOll80PX4G6y3IxhYo2x_keo8qPTQ",
                  "use": "sig"
                }
              ]
            }""";
}