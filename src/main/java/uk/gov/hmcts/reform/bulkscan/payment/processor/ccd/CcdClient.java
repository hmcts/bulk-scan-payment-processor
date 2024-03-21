package uk.gov.hmcts.reform.bulkscan.payment.processor.ccd;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.util.Map;

import static java.lang.String.format;

/**
 * Client for interacting with CCD.
 */
@Service
public class CcdClient {

    private static final Logger log = LoggerFactory.getLogger(CcdClient.class);

    private static final String AWAITING_DCN_PROCESSING_FIELD_NAME = "awaitingPaymentDCNProcessing";
    private static final String COMPLETE_AWAITING_DCN_PROCESSING_EVENT_ID = "completeAwaitingPaymentDCNProcessing";

    private final CoreCaseDataApi ccdApi;
    private final CcdAuthenticatorFactory authenticatorFactory;

    /**
     * Constructor for the CcdClient.
     * @param ccdApi The CoreCaseDataApi
     * @param authenticatorFactory The CcdAuthenticatorFactory
     */
    public CcdClient(
        CoreCaseDataApi ccdApi,
        CcdAuthenticatorFactory authenticatorFactory
    ) {
        this.ccdApi = ccdApi;
        this.authenticatorFactory = authenticatorFactory;
    }

    /**
     * Completes the awaiting payment DCN processing for the given exception record.
     * @param exceptionRecordCcdId The ID of the exception record in CCD
     * @param service The service name
     * @param jurisdiction The jurisdiction name
     */
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
            .data(Map.of(AWAITING_DCN_PROCESSING_FIELD_NAME, "No"))
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

    /**
     * Starts the event for completing payment DCN processing.
     * @param authenticator The authenticator
     * @param jurisdiction The jurisdiction name
     * @param caseTypeId The case type ID
     * @param caseRef The case reference
     * @return The start event response
     */
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
                format(
                    "Failed starting event in CCD for completing payment DCN processing "
                        + "case: %s Error: %s", caseRef,
                    ex.status()
                ),
                ex
            );
        }
    }

    /**
     * Submits the event for completing payment DCN processing.
     * @param authenticator The authenticator
     * @param jurisdiction The jurisdiction name
     * @param caseTypeId The case type ID
     * @param caseRef The case reference
     * @param caseDataContent The case data content
     */
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
                format(
                    "Failed submitting event in CCD for completing payment DCN processing "
                        + "case: %s Error: %s", caseRef,
                    ex.status()
                ),
                ex
            );
        }
    }

    /**
     * Logs the response from CCD.
     * @param exception The exception
     * @param introMessage The intro message
     */
    private void debugCcdException(FeignException exception, String introMessage) {
        log.debug(
            "{}. CCD response: {}",
            introMessage,
            exception.responseBody().map(b -> new String(b.array())).orElseGet(exception::getMessage)
        );
    }
}
