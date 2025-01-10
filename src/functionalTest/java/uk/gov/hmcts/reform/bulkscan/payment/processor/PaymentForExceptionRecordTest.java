package uk.gov.hmcts.reform.bulkscan.payment.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.bulkscan.payment.processor.ccd.CcdAuthenticator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.ccd.CcdAuthenticatorFactory;
import uk.gov.hmcts.reform.bulkscan.payment.processor.helper.ExceptionRecordCreator;
import uk.gov.hmcts.reform.bulkscan.payment.processor.service.PaymentService;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@SpringBootTest
@ActiveProfiles("functional")
class PaymentForExceptionRecordTest {

    private static final String AWAITING_DNC_PROCESSING_FLAG_NAME = "awaitingPaymentDCNProcessing";
    private static final String YES = "Yes";
    private static final String NO = "No";
    private static final String JURISDICTION = "BULKSCAN";
    private static final String BULKSCAN_PO_BOX = "BULKSCANPO1";

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private ExceptionRecordCreator exceptionRecordCreator;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private CcdAuthenticatorFactory ccdAuthenticatorFactory;

    // Note: if running locally, refer to JmsPaymentForExceptionRecordTest, not this one
    // Remove @Disabled on the other if you want to run it locally
    //    @Test
    //    public void should_set_awaiting_payment_false_after_payment_sent() {
    //        // given
    //        CaseDetails caseDetails = exceptionRecordCreator.createExceptionRecord(
    //            ImmutableMap.of(AWAITING_DNC_PROCESSING_FLAG_NAME, YES)
    //        );
    //
    //        assertThat(caseDetails.getData().get(AWAITING_DNC_PROCESSING_FLAG_NAME)).isEqualTo(YES);
    //
    //        // when
    //        // payment sent to payments queue
    //        paymentService.createPayment(
    //            new CreatePayment(
    //                "some_envelope_id",
    //                Long.toString(caseDetails.getId()),
    //                true,
    //                BULKSCAN_PO_BOX,
    //                caseDetails.getJurisdiction(),
    //                "bulkscan",
    //                singletonList(new PaymentInfo("154565768345123456789"))
    //            )
    //        );
    //
    //        //then
    //        CcdAuthenticator authenticator = ccdAuthenticatorFactory.createForJurisdiction(JURISDICTION);
    //        await("Case is updated")
    //            .atMost(120, TimeUnit.SECONDS)
    //            .pollDelay(1, TimeUnit.SECONDS)
    //            .until(() -> casePaymentStatusUpdated(authenticator, caseDetails));
    //    }

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
        return caseDetailsUpdated.getData().get(AWAITING_DNC_PROCESSING_FLAG_NAME).equals(NO);
    }
}
