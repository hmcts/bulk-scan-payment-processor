package uk.gov.hmcts.reform.bulkscan.payment.processor.client.payhub;

import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class PayHubClientErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        return new PayHubClientException(FeignException.errorStatus(methodKey, response));
    }
}
