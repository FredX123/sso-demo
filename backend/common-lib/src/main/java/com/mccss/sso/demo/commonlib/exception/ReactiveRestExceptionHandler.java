package com.mccss.sso.demo.commonlib.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
@ConditionalOnWebApplication(type = Type.REACTIVE)  // ensures only the correct handler is activated per service type.
public class ReactiveRestExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException ex) {
        log.error("ApplicationException:", ex);
        HttpStatus status = ex.getStatusCode() != 0 ? HttpStatus.valueOf(ex.getStatusCode())
                : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), ex.getMessage(), Instant.now()));
    }

    /** Map downstream WebClient errors to the same JSON shape */
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClient(WebClientResponseException ex,
                                                         ServerWebExchange exchange) {
        log.warn("Downstream error {} {} -> {} {}",
                exchange.getRequest().getMethod(), exchange.getRequest().getURI(),
                ex.getRawStatusCode(), ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ErrorResponse(ex.getRawStatusCode(), ex.getResponseBodyAsString(), Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, ServerWebExchange exchange) {
        log.error("Unhandled exception:", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ex.getMessage(), Instant.now()));
    }
}
