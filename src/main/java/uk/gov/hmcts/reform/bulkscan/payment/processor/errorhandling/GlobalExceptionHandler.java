package uk.gov.hmcts.reform.bulkscan.payment.processor.errorhandling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.hmcts.reform.bulkscan.payment.processor.errorhandling.exception.CcdCallException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.errorhandling.exception.PayHubCallException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handle(MethodArgumentNotValidException ex) {
        Map<String, String> errorMap = new ConcurrentHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errorMap.put(error.getField(),
                                                                             error.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
    }

    @ExceptionHandler(PayHubCallException.class)
    public ResponseEntity<ExceptionResponse> handle(PayHubCallException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(generateExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(CcdCallException.class)
    public ResponseEntity<ExceptionResponse> handle(CcdCallException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(generateExceptionResponse(ex.getMessage()));
    }

    private ExceptionResponse generateExceptionResponse(String message) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(message);
        exceptionResponse.setTimestamp(LocalDateTime.now());
        return exceptionResponse;
    }
}
