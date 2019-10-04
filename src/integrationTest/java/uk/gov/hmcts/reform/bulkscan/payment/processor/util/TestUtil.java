package uk.gov.hmcts.reform.bulkscan.payment.processor.util;

import com.google.common.io.Resources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class TestUtil {

    private  TestUtil() {}

    public static String fileContentAsString(String file) {
        return new String(fileContentAsBytes(file), StandardCharsets.UTF_8);
    }

    public static byte[] fileContentAsBytes(String file) {
        try {
            return Resources.toByteArray(Resources.getResource(file));
        } catch (IOException e) {
            throw new RuntimeException("Could not load file" + file, e);
        }
    }
}
