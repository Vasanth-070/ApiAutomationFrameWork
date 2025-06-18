package com.automation.tests.services.payment;

import com.automation.framework.core.auth.AuthenticationManager;
import com.automation.framework.core.auth.RedisManager;
import com.automation.framework.core.auth.AuthResponse;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class PhoneAuthenticationTest {

    private AuthenticationManager authenticationManager;
    private RedisManager redisManager;
    private static final String BASE_URL = "https://your-api-base-url.com"; // Replace with your actual base URL
    private static final String PHONE_NUMBER = "1234567890"; // Replace with the actual phone number
    private static final String CLIENT_ID = "your-client-id"; // Replace with your actual client ID
    private static final String DEVICE_ID = "your-device-id"; // Replace with your actual device ID

    @BeforeClass
    public void setup() {
        authenticationManager = new AuthenticationManager(BASE_URL);
        redisManager = RedisManager.getInstance();
    }

    @Test
    public void testPhoneAuthentication() {
        // Step 1: Send OTP to the phone number
        Response otpResponse = authenticationManager.sendOtp(PHONE_NUMBER, CLIENT_ID, DEVICE_ID);
        assertEquals(otpResponse.getStatusCode(), 200, "OTP sending failed");

        // Step 2: Retrieve OTP from Redis
        String otp = redisManager.getOtp(PHONE_NUMBER);
        assertNotNull(otp, "OTP retrieval failed");

        // Step 3: Login with the OTP
        AuthResponse authResponse = authenticationManager.authenticate(PHONE_NUMBER, CLIENT_ID, DEVICE_ID);
        assertEquals(authResponse.isSuccess(), true, "Authentication failed");
        assertNotNull(authResponse.getAccessToken(), "Access token is null");
        assertNotNull(authResponse.getCookie(), "Cookie is null");
    }
} 