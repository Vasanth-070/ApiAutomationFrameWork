package com.automation.tests.services.payment;

import com.automation.framework.core.base.BaseApiTest;
import com.automation.framework.services.payment.endpoints.FlightEndpoints;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class PaymentPageApiTests extends BaseApiTest {

    @BeforeClass
    @Override
    public void baseSetup() {
        super.baseSetup();
        
        reportManager.initializeReport("Flight Booking API Tests", apiConfig.getEnvironment());
        testLogger.logTestSuiteStart("Flight Booking API Test Suite");
    }

    @Override
    public void setupTestData() {
        testLogger.logInfo("Setting up test data for Flight Booking API tests");
    }

    @Override
    public void cleanupTestData() {
        testLogger.logInfo("Cleaning up test data for Flight Booking API tests");
    }

    private Map<String, String> getIxigoHeaders() {
        Map<String, String> headers = new HashMap<>();
        
        // Authentication headers - read from properties
        headers.put("apikey", apiConfig.getProperty("ixigo.apikey"));
        headers.put("authorization", "Bearer " + apiConfig.getProperty("ixigo.auth.token"));
        headers.put("clientid", apiConfig.getProperty("ixigo.clientid"));
        headers.put("deviceid", apiConfig.getProperty("ixigo.deviceid"));
        
        // Standard headers - read from properties
        headers.put("accept", apiConfig.getProperty("ixigo.accept"));
        headers.put("accept-language", apiConfig.getProperty("ixigo.accept.language"));
        headers.put("timezone", apiConfig.getProperty("ixigo.timezone"));
        headers.put("x-request-webappversion", apiConfig.getProperty("ixigo.webapp.version"));
        headers.put("psdkuiversion", apiConfig.getProperty("ixigo.psdk.ui.version"));
        headers.put("ixisrc", apiConfig.getProperty("ixigo.ixisrc"));
        headers.put("priority", apiConfig.getProperty("ixigo.priority"));
        headers.put("sec-fetch-mode", apiConfig.getProperty("ixigo.sec.fetch.mode"));
        headers.put("sec-fetch-site", apiConfig.getProperty("ixigo.sec.fetch.site"));
        headers.put("user-agent", apiConfig.getProperty("ixigo.user.agent"));
        headers.put("x-requested-with", apiConfig.getProperty("ixigo.x.requested.with"));
        
        // Sentry headers - read from properties
        headers.put("baggage", apiConfig.getProperty("ixigo.baggage"));
        headers.put("sentry-trace", apiConfig.getProperty("ixigo.sentry.trace"));
        
        // Referer header - read from properties
        headers.put("referer", apiConfig.getProperty("ixigo.referer"));
        
        // Cookie header - read from properties (update in dev.properties when needed)
        headers.put("Cookie", apiConfig.getProperty("ixigo.cookies"));
        
        return headers;
    }

    @Test(description = "Test Ixigo Flight Trip Details API call with exact curl headers")
    public void testGetFlightTripDetails() {
        reportManager.startTest("Ixigo Flight Trip Details", "Test Ixigo Flight Trip Details API call with authentication");
        testLogger.logTestStart("testGetFlightTripDetails", "FlightBookingApiTests");
        
        long startTime = System.currentTimeMillis();
        
        try {
            String tripId = "IF25061094872927";
            String endpoint = FlightEndpoints.IXIGO_FLIGHT_TRIP_DETAILS.replace("{tripId}", tripId) + "?tripFetchFlow=PAYMENT_CONFIRMATION";
            
            testLogger.logApiRequest("GET", endpoint, null);
            reportManager.logApiRequest("GET", endpoint, null, getIxigoHeaders().toString());
            
            String fullUrl = apiConfig.getBaseUrl() + FlightEndpoints.IXIGO_FLIGHT_TRIP_DETAILS.replace("{tripId}", tripId) + "?tripFetchFlow=PAYMENT_CONFIRMATION";
            testLogger.logInfo("Full Request URL: " + fullUrl);
            testLogger.logInfo("Request Headers: " + getIxigoHeaders().toString());
            
            Response response = RestAssured.given()
                    .headers(getIxigoHeaders())
                    .when()
                    .get(FlightEndpoints.IXIGO_FLIGHT_TRIP_DETAILS.replace("{tripId}", tripId) + "?tripFetchFlow=PAYMENT_CONFIRMATION")
                    .then()
                    .extract()
                    .response();
            
            testLogger.logApiResponse(response.getStatusCode(), response.getBody().asString(), response.getTime());
            reportManager.logApiResponse(response, response.getBody().asString());
            
            testLogger.logInfo("Response Status Code: " + response.getStatusCode());
            testLogger.logInfo("Response Time: " + response.getTime() + " ms");
            testLogger.logInfo("Response Headers: " + response.getHeaders().toString());
            testLogger.logInfo("Response Body: " + response.asString());
            
            long endTime = System.currentTimeMillis();
            testLogger.logTestEnd("testGetFlightTripDetails", "COMPLETED", endTime - startTime);
            reportManager.markTestPassed("testGetFlightTripDetails", "API call completed successfully");
            
        } catch (Exception e) {
            handleTestException("testGetFlightTripDetails", e, startTime);
        }
    }
    
    @AfterClass
    public void tearDown() {
        baseTearDown("Flight Booking API Test Suite", 1, 1, 0, 0);
    }
}