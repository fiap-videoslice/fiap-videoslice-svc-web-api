package com.example.fiap.videosliceapi.apiutils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public class WebUtils {
    public static <T> ResponseEntity<T> errorResponse(HttpStatus httpStatus, String detail) {
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(httpStatus, detail)).build();
    }

    public static <T> ResponseEntity<T> okResponse(T responseValue) {
        return ResponseEntity.of(Optional.of(responseValue));
    }
}
