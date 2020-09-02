package uk.gov.hmcts.reform.bulkscan.payment.processor.ccd;

import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import static java.lang.String.format;

@Service
public class CcdClient {

    private static final Logger log = LoggerFactory.getLogger(CcdClient.class);

    private static final String AWAITING_DCN_PROCESSING_FIELD_NAME = "awaitingPaymentDCNProcessing";
    private static final String COMPLETE_AWAITING_DCN_PROCESSING_EVENT_ID = "completeAwaitingPaymentDCNProcessing";

    private final CoreCaseDataApi ccdApi;
    private final CcdAuthenticatorFactory authenticatorFactory;

    public CcdClient(
        CoreCaseDataApi ccdApi,
        CcdAuthenticatorFactory authenticatorFactory
    ) {
        this.ccdApi = ccdApi;
        this.authenticatorFactory = authenticatorFactory;
    }

    public void completeAwaitingDcnProcessing(
        String exceptionRecordCcdId,
        String service,
        String jurisdiction
    ) {
        log.info(
            "Completing awaiting payment DCN processing. Exception record ID: {}, service: {}",
            exceptionRecordCcdId,
            service
        );

        CcdAuthenticator authenticator = authenticatorFactory.createForJurisdiction(jurisdiction);
        String caseTypeId = service.toUpperCase() + "_ExceptionRecord";

        StartEventResponse startEventResponse =
            startCompleteAwaitingDcnProcessing(authenticator, jurisdiction, caseTypeId, exceptionRecordCcdId);

        Event event = Event
            .builder()
            .summary("Complete payment DCN processing")
            .id(startEventResponse.getEventId())
            .build();

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .data(ImmutableMap.of(AWAITING_DCN_PROCESSING_FIELD_NAME, "No"))
            .event(event)
            .ignoreWarning(true)
            .caseReference(exceptionRecordCcdId)
            .eventToken(startEventResponse.getToken())
            .build();

        submitCompleteAwaitingDcnProcessing(
            authenticator,
            jurisdiction,
            caseTypeId,
            exceptionRecordCcdId,
            caseDataContent
        );

        log.info(
            "Completed awaiting payment DCN processing. Exception record ID: {}, service {}",
            exceptionRecordCcdId,
            service
        );
    }

    private StartEventResponse startCompleteAwaitingDcnProcessing(
        CcdAuthenticator authenticator,
        String jurisdiction,
        String caseTypeId,
        String caseRef
    ) {
        try {
            StartEventResponse response = ccdApi.startEventForCaseWorker(
                authenticator.getUserToken(),
                authenticator.getServiceToken(),
                authenticator.getUserDetails().getId(),
                jurisdiction,
                caseTypeId,
                caseRef,
                COMPLETE_AWAITING_DCN_PROCESSING_EVENT_ID
            );

            log.info(
                "Started event {} for case {}. Jurisdiction: {}. Case type ID: {}",
                COMPLETE_AWAITING_DCN_PROCESSING_EVENT_ID,
                caseRef,
                jurisdiction,
                caseTypeId
            );

            return response;
        } catch (FeignException ex) {
            debugCcdException(ex, "Failed to call 'startCompleteAwaitingDcnProcessing'");
            throw new CcdCallException(
                format("Internal Error: start event call failed case: %s Error: %s", caseRef, ex.status()), ex
            );
        }
    }

    private void submitCompleteAwaitingDcnProcessing(
        CcdAuthenticator authenticator,
        String jurisdiction,
        String caseTypeId,
        String caseRef,
        CaseDataContent caseDataContent
    ) {
        try {
            ccdApi.submitEventForCaseWorker(
                authenticator.getUserToken(),
                authenticator.getServiceToken(),
                authenticator.getUserDetails().getId(),
                jurisdiction,
                caseTypeId,
                caseRef,
                true,
                caseDataContent
            );

            log.info(
                "Submitted event {} for case {}. Jurisdiction: {}. Case type ID: {}",
                caseDataContent.getEvent().getId(),
                caseRef,
                jurisdiction,
                caseTypeId
            );
        } catch (FeignException ex) {
            debugCcdException(ex, "Failed to call 'submitCompleteAwaitingDcnProcessing'");
            throw new CcdCallException(
                format("Internal Error: submit event call failed case: %s Error: %s", caseRef, ex.status()), ex
            );
        }
    }

    private void debugCcdException(FeignException exception, String introMessage) {
        log.debug(
            "{}. CCD response: {}",
            introMessage,
            exception.responseBody().map(b -> new String(b.array())).orElseGet(exception::getMessage)
        );
    }
}
