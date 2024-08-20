package uk.gov.hmcts.reform.bulkscan.payment.processor.errorhandling;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bulkscan.payment.processor.errorhandling.exception.CcdCallException;
import uk.gov.hmcts.reform.bulkscan.payment.processor.errorhandling.exception.PayHubCallException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private static final String TEST_MESSAGE = "This is a test message";
    private static final String ASSERTION_MESSAGE = "The message should match the message passed in";
    private static final String BAD_REQUEST_ASSERTION = "Status code should be of type: Bad Request";
    private static final String FAILED_DEPENDENCY_ASSERTION = "Status code should be of type: Failed Dependency";
    static final String ASSERTION_RESPONSE_BODY = "Response should contain a body";

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private PayHubCallException payHubCallException;

    @Mock
    private CcdCallException ccdCallException;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void testHandleMethodArgumentNotValidException() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "testField", TEST_MESSAGE);

        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, String>> exceptionResponse = globalExceptionHandler
            .handle(methodArgumentNotValidException);

        assertEquals(HttpStatus.BAD_REQUEST, exceptionResponse.getStatusCode(), BAD_REQUEST_ASSERTION);

        Map<String, String> errorMap = exceptionResponse.getBody();
        assertNotNull(errorMap, ASSERTION_RESPONSE_BODY);
        assertEquals(1, errorMap.size());
        assertEquals(TEST_MESSAGE, errorMap.get("testField"), ASSERTION_MESSAGE);
    }

    @Test
    void testHandlePayHubCallException() {
        when(payHubCallException.getMessage()).thenReturn(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> exceptionResponse = globalExceptionHandler.handle(payHubCallException);

        assertEquals(HttpStatus.FAILED_DEPENDENCY, exceptionResponse.getStatusCode(), FAILED_DEPENDENCY_ASSERTION);

        ExceptionResponse responseBody = exceptionResponse.getBody();
        assertNotNull(responseBody, ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseBody.getMessage(), ASSERTION_MESSAGE);
        assertNotNull(responseBody.getTimestamp());
    }

    @Test
    void testHandleCcdCallException() {
        when(ccdCallException.getMessage()).thenReturn(TEST_MESSAGE);

        ResponseEntity<ExceptionResponse> exceptionResponse = globalExceptionHandler.handle(ccdCallException);

        assertEquals(HttpStatus.FAILED_DEPENDENCY, exceptionResponse.getStatusCode(), FAILED_DEPENDENCY_ASSERTION);

        ExceptionResponse responseBody = exceptionResponse.getBody();
        assertNotNull(responseBody, ASSERTION_RESPONSE_BODY);
        assertEquals(TEST_MESSAGE, responseBody.getMessage(), ASSERTION_MESSAGE);
        assertNotNull(responseBody.getTimestamp());
    }
}
