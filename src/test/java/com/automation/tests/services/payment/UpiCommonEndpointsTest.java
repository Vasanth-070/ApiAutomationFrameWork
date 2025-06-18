package com.automation.tests.services.payment;

import com.automation.framework.core.base.BaseApiTest;
import com.automation.framework.services.payment.endpoints.UpiEndpoints;
import com.automation.framework.shared.utils.HttpMethod;
import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.automation.framework.core.auth.SessionAuthenticationManager;
import java.util.HashMap;
import java.util.Map;

public class UpiCommonEndpointsTest extends BaseApiTest {
    @Override
    public String getTestSuiteName() {
        return "UPI Common Endpoints Test Suite";
    }

    @Override
    public void defineTestCases() {
        // Test cases are defined as TestNG methods
    }

    @Override
    public void performTestSetup() {
        // No setup needed for this test
    }

    @Override
    public void performTestCleanup() {
        // No cleanup needed for this test
    }

    @Test
    public void testUpiValidation() throws Exception {
        executeTest("UPI Validation", "Validate UPI endpoint", () -> {
            String upiId = "apoorvavarshney0@okicici";
            String endpoint = UpiEndpoints.UPI_VALIDATION.replace("{upi}", upiId);

            // Get fresh token and device ID from the framework
            // String token = SessionAuthenticationManager.getInstance().getSessionAuthToken();
            // String deviceId = SessionAuthenticationManager.getInstance().getDeviceId();

            // Map<String, String> headers = new HashMap<>();
            // headers.put("accept", "*/*");
            // headers.put("authorization", "Bearer 8kg3nrrm1vc5w6o3ua4403gvts64m5erpv58xvj2wp2ch6mv5dcmf683k5jcsimf3wx29bpjyd6sp97l7akawejc1trxp32qkb5lkfqds2tjvjre3djxw2j6hsuwtp6lk1wbjhxjbd7vtv33fl9vk86068iacoj5rps3yyca5pbmvltona9lp334fuoybwae94a");
            // headers.put("clientid", "iximweb");
            // headers.put("deviceid", "fbb344d4254840a6abb9");
            // headers.put("x-requested-with", "XMLHttpRequest");

            Response response = makeApiCall(
                HttpMethod.GET,
                endpoint
            );

            JsonNode data = objectMapper.readTree(response.asString()).get("data");
            Assert.assertTrue(data.get("valid").asBoolean(), "UPI should be valid");
            Assert.assertEquals(data.get("vpa").asText(), upiId, "VPA should match");
            Assert.assertEquals(data.get("name").asText(), "APOORVA VARSHNEY", "Name should match");
        });
    }
} 