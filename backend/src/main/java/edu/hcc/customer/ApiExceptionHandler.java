package edu.hcc.customer;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException error) {
        String detail = error.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .sorted().collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Map.of("detail", detail));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    ResponseEntity<?> malformed(Exception error) {
        return ResponseEntity.badRequest().body(Map.of("detail", "Invalid request format"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<?> missing(ResponseStatusException error) {
        return ResponseEntity.status(error.getStatusCode())
                .body(Map.of("detail", error.getReason()));
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    ResponseEntity<?> unavailable(Exception error) {
        return ResponseEntity.status(503)
                .body(Map.of("detail", "Database unavailable. Check PostgreSQL configuration."));
    }
}
