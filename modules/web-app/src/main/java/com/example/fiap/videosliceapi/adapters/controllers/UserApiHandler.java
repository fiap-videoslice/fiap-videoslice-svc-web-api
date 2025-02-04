package com.example.fiap.videosliceapi.adapters.controllers;

import com.example.fiap.videosliceapi.adapters.auth.LoggedUser;
import com.example.fiap.videosliceapi.adapters.auth.LoggedUserTokenParser;
import com.example.fiap.videosliceapi.adapters.auth.UserGroup;
import com.example.fiap.videosliceapi.adapters.dto.CreateUserRequest;
import com.example.fiap.videosliceapi.adapters.dto.CreateUserResponse;
import com.example.fiap.videosliceapi.adapters.externalsystem.CognitoUserRegistry;
import com.example.fiap.videosliceapi.adapters.presenters.LoggedUserPresenter;
import com.example.fiap.videosliceapi.apiutils.LoggedUserCheck;
import com.example.fiap.videosliceapi.apiutils.WebUtils;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/*
 * The APIs on this controller are technical services that are not tied to the domain of this Microservice.
 * They are just convenience methods for common user management options; a possible evolution is that they
 * eventually grow to an independent User Management microservice.
 */
@RestController
public class UserApiHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserApiHandler.class);

    private final CognitoUserRegistry cognitoUserRegistry;
    private final LoggedUserTokenParser loggedUserTokenParser;

    @Autowired
    public UserApiHandler(CognitoUserRegistry cognitoUserRegistry, LoggedUserTokenParser loggedUserTokenParser) {
        this.cognitoUserRegistry = cognitoUserRegistry;
        this.loggedUserTokenParser = loggedUserTokenParser;
    }

    @Operation(description = "Retorna os dados do usuário logado")
    @GetMapping(path = "/users/me")
    public ResponseEntity<Map<String, Object>> getLoggedUser(@RequestHeader HttpHeaders headers) {
        LoggedUser loggedUser = loggedUserTokenParser.verifyLoggedUser(headers);

        if (loggedUser.authenticated()) {
            return WebUtils.okResponse(LoggedUserPresenter.toMap(loggedUser));
        } else {
            return WebUtils.errorResponse(HttpStatus.UNAUTHORIZED, loggedUser.authError());
        }
    }

    @Operation(summary = "Add a new user", description = "Create a new user (directly on the external user manager service). Requester must be in the Admin group")
    @PostMapping(path = "/users", produces = "application/json")
    public ResponseEntity<CreateUserResponse> createUser(@RequestBody CreateUserRequest request,
                                                         @RequestHeader HttpHeaders headers) {
        try {
            LoggedUser loggedUser = LoggedUserCheck.ensureLoggedUser(loggedUserTokenParser, headers);

            if (loggedUser.getGroup() != UserGroup.Admin) {
                return WebUtils.errorResponse(HttpStatus.FORBIDDEN, "This operation can only be performed by an Admin");
            }

            request.validate();

            String newUserId = cognitoUserRegistry.registerUser(request.name(), request.email(),
                    UserGroup.valueOf(request.group()), request.password());

            return WebUtils.okResponse(new CreateUserResponse(newUserId));

        } catch (LoggedUserCheck.NotAuthenticatedException nae) {
            return WebUtils.errorResponse(HttpStatus.UNAUTHORIZED, nae.getMessage());
        } catch (IllegalArgumentException iae) {
            return WebUtils.errorResponse(HttpStatus.BAD_REQUEST, iae.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error while creating user: {}", e, e);
            return WebUtils.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error while creating user");
        }
    }
}
