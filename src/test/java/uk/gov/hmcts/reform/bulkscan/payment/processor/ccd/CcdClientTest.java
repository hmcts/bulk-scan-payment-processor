package uk.gov.hmcts.reform.bulkscan.payment.processor.ccd;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class CcdClientTest {

    private static final String COMPLETE_AWAITING_DCN_PROCESSING_EVENT_ID = "completeAwaitingPaymentDCNProcessing";

    @Mock
    private CoreCaseDataApi ccdApi;

    @Mock
    private CcdAuthenticatorFactory authenticatorFactory;

    @Mock
    private CcdAuthenticator ccdAuthenticator;

    @Mock
    private UserDetails userDetails;

    @Mock
    private StartEventResponse startEventResponse;

    private CcdClient ccdClient;

    @BeforeEach
    void setUp() {
        ccdClient = new CcdClient(ccdApi, authenticatorFactory);

        given(authenticatorFactory.createForJurisdiction("jurisdiction")).willReturn(ccdAuthenticator);
        given(ccdAuthenticator.getUserToken()).willReturn("user_token");
        given(ccdAuthenticator.getServiceToken()).willReturn("service_token");
        given(ccdAuthenticator.getUserDetails()).willReturn(userDetails);
        given(userDetails.getId()).willReturn("user_id");
    }

    @Test
    void should_pass_successfully_if_no_feign_exceptions() {
        // given
        given(ccdApi.startEventForCaseWorker(
            "user_token",
            "service_token",
            "user_id",
            "jurisdiction",
            "SERVICE_ExceptionRecord",
            "exception_record_ccd_id",
            COMPLETE_AWAITING_DCN_PROCESSING_EVENT_ID
        )).willReturn(startEventResponse);

        // when
        assertThatCode(
            () -> ccdClient.completeAwaitingDcnProcessing("exception_record_ccd_id", "service", "jurisdiction")
        ).doesNotThrowAnyException();

        // then
        verify(ccdApi).submitEventForCaseWorker(
            eq("user_token"),
            eq("service_token"),
            eq("user_id"),
            eq("jurisdiction"),
            eq("SERVICE_ExceptionRecord"),
            eq("exception_record_ccd_id"),
            eq(true),
            any(CaseDataContent.class)
        );
    }

    @Test
    void should_throw_CcdCallException_if_start_event_thrown_feign_exception() {
        // given
        given(ccdApi.startEventForCaseWorker(
            "user_token",
            "service_token",
            "user_id",
            "jurisdiction",
            "SERVICE_ExceptionRecord",
            "exception_record_ccd_id",
            COMPLETE_AWAITING_DCN_PROCESSING_EVENT_ID
        )).willReturn(startEventResponse);

        Request request = Request.create(Request.HttpMethod.GET, "http://url", emptyMap(), "body".getBytes(), UTF_8, new RequestTemplate());
        final FeignException.BadRequest feignException = new FeignException.BadRequest(
            "message",
            request,
            "body".getBytes()
        );

        given(ccdApi.startEventForCaseWorker(
            "user_token",
            "service_token",
            "user_id",
            "jurisdiction",
            "SERVICE_ExceptionRecord",
            "exception_record_ccd_id",
            COMPLETE_AWAITING_DCN_PROCESSING_EVENT_ID
        )).willThrow(feignException);

        // when
        CcdCallException exception = catchThrowableOfType(
            () -> ccdClient.completeAwaitingDcnProcessing("exception_record_ccd_id", "service", "jurisdiction"),
            CcdCallException.class
        );

        // then
        assertThat(exception.getMessage())
            .isEqualTo("Internal Error: start event call failed case: exception_record_ccd_id Error: 400");
        assertThat(exception.getCause()).isEqualTo(feignException);

        verifyNoMoreInteractions(ccdApi);
    }

    @Test
    void should_throw_CcdCallException_if_submit_event_thrown_feign_exception() {
        // given
        given(ccdApi.startEventForCaseWorker(
            "user_token",
            "service_token",
            "user_id",
            "jurisdiction",
            "SERVICE_ExceptionRecord",
            "exception_record_ccd_id",
            COMPLETE_AWAITING_DCN_PROCESSING_EVENT_ID
        )).willReturn(startEventResponse);

        Request request = Request.create(Request.HttpMethod.GET, "http://url", emptyMap(), "body".getBytes(), UTF_8, new RequestTemplate());
        final FeignException.BadRequest feignException = new FeignException.BadRequest(
            "message",
            request,
            "body".getBytes()
        );

        given(ccdApi.submitEventForCaseWorker(
            eq("user_token"),
            eq("service_token"),
            eq("user_id"),
            eq("jurisdiction"),
            eq("SERVICE_ExceptionRecord"),
            eq("exception_record_ccd_id"),
            eq(true),
            any(CaseDataContent.class)
        )).willThrow(feignException);

        // when
        CcdCallException exception = catchThrowableOfType(
            () -> ccdClient.completeAwaitingDcnProcessing("exception_record_ccd_id", "service", "jurisdiction"),
            CcdCallException.class
        );

        // then
        assertThat(exception.getMessage())
            .isEqualTo("Internal Error: submit event call failed case: exception_record_ccd_id Error: 400");
        assertThat(exception.getCause()).isEqualTo(feignException);
    }
}
