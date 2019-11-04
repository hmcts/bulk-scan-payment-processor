package uk.gov.hmcts.reform.bulkscan.payment.processor;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.bulkscan.payment.processor.ccd.CcdAuthenticator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.ccd.CcdAuthenticatorFactory;
import uk.gov.hmcts.reform.bulkscan.payment.processor.helper.CaseSearcher;
import uk.gov.hmcts.reform.bulkscan.payment.processor.helper.ExceptionRecordCreator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.helper.PaymentsMessageSender;
import uk.gov.hmcts.reform.bulkscan.payment.processor.model.CreatePaymentsCommand;
import uk.gov.hmcts.reform.bulkscan.payment.processor.model.PaymentData;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("functional")
class PaymentForExistingCaseTest {

    private static final String EXCEPTION_RECORD_AWAITING_PAYMENT_DCN_PROCESSING = "awaitingPaymentDCNProcessing";
    private static final String YES = "Yes";
    private static final String NO = "No";
    private static final String JURISDICTION = "BULKSCAN";
    private static final String BULKSCAN_PO_BOX = "12625";

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private ExceptionRecordCreator exceptionRecordCreator;

    @Autowired
    private CaseSearcher caseSearcher;

    @Autowired
    private PaymentsMessageSender paymentsMessageSender;

    @Autowired
    private CcdAuthenticatorFactory ccdAuthenticatorFactory;

    @Test
    public void should_set_awaiting_payment_false_after_payment_sent() throws Exception {
        // given
        CaseDetails caseDetails = exceptionRecordCreator.createExceptionRecord(
            ImmutableMap.of(EXCEPTION_RECORD_AWAITING_PAYMENT_DCN_PROCESSING, YES)
        );

        assertThat(caseDetails.getData().get(EXCEPTION_RECORD_AWAITING_PAYMENT_DCN_PROCESSING)).isEqualTo(YES);

        // when
        // payment sent to payments queue
        paymentsMessageSender.send(
            new CreatePaymentsCommand(
                "some_envelope_id",
                Long.toString(caseDetails.getId()),
                caseDetails.getJurisdiction(),
                "bulkscan",
                BULKSCAN_PO_BOX,
                true,
                asList(new PaymentData("154565768"))
            )
        );

        //then
        CcdAuthenticator authenticator = ccdAuthenticatorFactory.createForJurisdiction(JURISDICTION);
        await("Case is updated")
            .atMost(120, TimeUnit.SECONDS)
            .pollDelay(1, TimeUnit.SECONDS)
            .until(() -> casePaymentStatusUpdated(authenticator, caseDetails));
    }

    private Boolean casePaymentStatusUpdated(
        CcdAuthenticator authenticator,
        CaseDetails caseDetails
    ) {
        CaseDetails caseDetailsUpdated =
            coreCaseDataApi.getCase(
                authenticator.getUserToken(),
                authenticator.getServiceToken(),
                Long.toString(caseDetails.getId())
            );
        return caseDetailsUpdated.getData().get(EXCEPTION_RECORD_AWAITING_PAYMENT_DCN_PROCESSING).equals(NO);
    }
}
