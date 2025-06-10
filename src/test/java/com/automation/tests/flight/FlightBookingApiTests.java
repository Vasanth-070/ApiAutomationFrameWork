package com.automation.tests.flight;

import com.automation.framework.base.BaseApiTest;
import com.automation.framework.data.ApiEndPoints;
import com.automation.framework.logging.TestLogger;
import com.automation.framework.reporting.ExtentReportManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class FlightBookingApiTests extends BaseApiTest {

    private TestLogger testLogger;
    private ExtentReportManager reportManager;

    @BeforeClass
    @Override
    public void baseSetup() {
        testLogger = TestLogger.getInstance();
        reportManager = new ExtentReportManager();
        
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
        
        // Authentication headers
        headers.put("apikey", apiConfig.getProperty("ixigo.apikey"));
        headers.put("authorization", "Bearer " + apiConfig.getProperty("ixigo.auth.token"));
        headers.put("clientid", apiConfig.getProperty("ixigo.clientid"));
        headers.put("deviceid", apiConfig.getProperty("ixigo.deviceid"));
        
        // Standard headers
        headers.put("accept", "*/*");
        headers.put("accept-language", "en-GB,en-US;q=0.9,en;q=0.8");
        headers.put("timezone", apiConfig.getProperty("ixigo.timezone"));
        headers.put("x-request-webappversion", apiConfig.getProperty("ixigo.webapp.version"));
        headers.put("psdkuiversion", apiConfig.getProperty("ixigo.psdk.ui.version"));
        headers.put("ixisrc", "iximweb");
        headers.put("priority", "u=1, i");
        headers.put("sec-fetch-dest", "empty");
        headers.put("sec-fetch-mode", "cors");
        headers.put("sec-fetch-site", "same-origin");
        headers.put("user-agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1");
        headers.put("x-requested-with", "XMLHttpRequest");
        
        // Sentry headers (from baggage header)
        headers.put("baggage", "sentry-environment=production,sentry-public_key=9eabb2843ac1404cb88da58b04752d7d,sentry-trace_id=7a14375cff8746f295a595e51274de83,sentry-sample_rate=0.2,sentry-sampled=false");
        headers.put("sentry-trace", "7a14375cff8746f295a595e51274de83-a2daea8861e141eb-0");
        
        // Referer header
        headers.put("referer", "https://www.ixigo.com/payments/view/home/1/01JXDATMFEF29KMW5GW7665KD6JXT10MADF?showHeader=true&tripId=IF25061094872927&providerId=1044&productType=FLIGHT&isr=false&flowType=PAYMENT_SDK");
        
        // Complex Cookie header
        headers.put("Cookie", "session_id=2lzk3fr4vr6; WZRK_G=38c03065b2c441feb531bcd51e0b6838; ixiUID=d3c884ac7c1b414ca825; ixiSrc=gnBYrdxk1y1zNloga+lJbzPui0TV05KVCc8Wlll/zxpTAv0abvTM443Zg+Txm8hCKZRCduthaKnf+i2my0Tieg==; ixigoSrc=d3c884ac7c1b414ca825|DIR:10062025|DIR:10062025|DIR:10062025; _gcl_gs=2.1.k1$i1749572821$u33293798; _gcl_au=1.1.2032514045.1749572822; _gcl_awk=GCL.1749572847.CjwKCAjwr5_CBhBlEiwAzfwYuDaq--MGGVJOn4pr_OlwWcokCqbpJQPso25KHI4vaGn1VoDkTj7B8RoC6roQAvD_BwE; __cf_bm=3KxjMadWmNrk5Rq7LUyGP9O2V7msZ5SqTOKg.EM_CV8-1749572847-1.0.1.1-LQdo..D54wta52ZW.QKNz_Hz2_LBJzNZYkufKbZ2L92l84tZ8_rDxaEuy14VgyG2ch1pCQQGPm8W0bY5906z.Gdw6nCuB68nRKwILTO7ClE; _gid=GA1.2.258240042.1749572867; _gac_UA-949229-1=1.1749572867.CjwKCAjwr5_CBhBlEiwAzfwYuDaq--MGGVJOn4pr_OlwWcokCqbpJQPso25KHI4vaGn1VoDkTj7B8RoC6roQAvD_BwE; sapphire=sQV07hxSbDpEOAn/8UmPQXHdlNk5bmANbg2l2Dh4hZTkqjn56DoMzqxiIe4dgPkW1qOu8LvpS9ePbD5MyNIw7WsUlZI8dzOJKYwJlOTvem7Qe2XnukmP8LB5qkd2S2BGf3poBuAHUQWjI7o7w0T11fW5ob0b+/9aWUTs+mtvYtp8Q2Rm4QppLM23uf7D74Su/XEra/P+r0EgT5+09qCu11B3CMOFmp8eVRYMd6OE0m8CejTCZ4Yw/6ppY2sv+1NfUO5/Zm/H87I=; at=gfj28gr76at44d53pnvc17ec7addd2er1nxeuvfh2qrxnjri5m4du7e60fq766cdt4dbvb7t2kt1a7ygrc44nnk7blpgb0yjxbev2qaa6c6gqy54n7efgld056gntaiijbkhjagff1jh0jdbxtihrse5ac6g8hvbjk1r83rxb3agw7jik4uid0mxoe6tjx4bho1; lt=photp; _ga=GA1.2.592530000.1749572822; _gat_UA-949229-1=1; _ga_BBFTXKP5NY=GS2.1.s1749572882$o1$g1$t1749572962$j60$l0$h0; ixiUsrLocale=\"urgn=Delhi:ucnc=IN:ucty=New Delhi:uctz=Asia/Kolkata:cnc=IN:cc=INR:lng=en\"; WZRK_S_R5Z-849-WZ4Z=%7B%22p%22%3A8%2C%22s%22%3A1749572821%2C%22t%22%3A1749572998%7D; _ga_LJX9T6MDKX=GS2.1.s1749572822$o1$g1$t1749572998$j60$l0$h617299798; __cf_bm=8NuAChONSBfuEwVjPsW0jyAQbYMsWGGXgoHJLM4mRyk-1749575225-1.0.1.1-v1p_ljBv7Ex5MHI4IxpvMr0x44zHJbH1SvXwzLrEsGbYOVtxzdr5HJGJdEEHBNb.aoBszi1vkP8Pxyb1w_jpt5TQAGyuDyTyN.c2YMA5pgo");
        
        return headers;
    }

    @Test(description = "Test Ixigo Flight Trip Details API call with authentication headers")
    public void testGetFlightTripDetails() {
        reportManager.startTest("Ixigo Flight Trip Details", "Test Ixigo Flight Trip Details API call with authentication");
        testLogger.logTestStart("testGetFlightTripDetails", "FlightBookingApiTests");
        
        long startTime = System.currentTimeMillis();
        
        try {
            String tripId = "IF25061094872927";
            String endpoint = ApiEndPoints.IXIGO_FLIGHT_TRIP_DETAILS.replace("{tripId}", tripId) + "?tripFetchFlow=PAYMENT_CONFIRMATION";
            
            testLogger.logApiRequest("GET", endpoint, null);
            reportManager.logApiRequest("GET", endpoint, null, getIxigoHeaders().toString());
            
            String fullUrl = apiConfig.getBaseUrl() + ApiEndPoints.IXIGO_FLIGHT_TRIP_DETAILS.replace("{tripId}", tripId) + "?tripFetchFlow=PAYMENT_CONFIRMATION";
            testLogger.logInfo("Full Request URL: " + fullUrl);
            testLogger.logInfo("Request Headers: " + getIxigoHeaders().toString());
            
            Response response = RestAssured.given()
                    .headers(getIxigoHeaders())
                    .relaxedHTTPSValidation()
                    .log().all()
                    .when()
                    .get(ApiEndPoints.IXIGO_FLIGHT_TRIP_DETAILS.replace("{tripId}", tripId) + "?tripFetchFlow=PAYMENT_CONFIRMATION")
                    .then()
                    .log().all()
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
            long endTime = System.currentTimeMillis();
            testLogger.logTestEnd("testGetFlightTripDetails", "FAILED", endTime - startTime);
            testLogger.logError("Test failed with exception", e);
            reportManager.markTestFailed("testGetFlightTripDetails", "Test failed: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
    
    @AfterClass
    public void tearDown() {
        testLogger.logTestSuiteEnd("Flight Booking API Test Suite", 1, 1, 0, 0);
        reportManager.finalizeReport();
        testLogger.logInfo("Test execution completed. Report generated at: " + reportManager.getReportPath());
    }
}