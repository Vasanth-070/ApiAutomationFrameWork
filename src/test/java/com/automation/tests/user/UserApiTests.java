package com.automation.tests.user;

import com.automation.framework.base.BaseApiTest;
import com.automation.framework.models.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.*;
import java.util.Map;

public class UserApiTests extends BaseApiTest {

    @BeforeClass
    @Override
    public void baseSetup() {
        super.baseSetup();
        
        reportManager.initializeReport("User API Tests", apiConfig.getEnvironment());
        testLogger.logTestSuiteStart("User API Test Suite");
    }

    @Override
    public void setupTestData() {
        if (testDataProvider != null) {
            testDataProvider.loadTestData("src/testData/userTestData.json");
            testLogger.logInfo("Test data loaded successfully");
        }
    }

    @Test(priority = 1, description = "Get list of users")
    public void testGetUsersList() {
        executeApiTest("Get Users List", "Verify that users list API returns correct data", 
                      "testGetUsersList", "UserApiTests", () -> {
            
            Map<String, Object> testData = testDataProvider.getTestDataForCase("getUsersList");
            int page = (Integer) testData.get("page");
            
            logApiRequest("GET", "/users?page=" + page, null, getApiHeaders());
            
            Response response = RestAssured.given()
                    .headers(getApiHeaders())
                    .when()
                    .get("/users?page=" + page);
            
            logApiResponse(response);
            
            // Validate status code
            validateStatusCode(response, 200);
            testLogger.logAssertionPassed("Status code is 200");
            reportManager.logStep("Status code validation passed", "PASS");
            
            // Parse response
            UsersListResponse usersResponse = objectMapper.readValue(response.getBody().asString(), UsersListResponse.class);
            
            // Validate response data
            Assert.assertEquals(usersResponse.getPage(), testData.get("page"));
            testLogger.logAssertionPassed("Page number matches expected value");
            
            Assert.assertEquals(usersResponse.getPerPage(), testData.get("expectedPerPage"));
            testLogger.logAssertionPassed("Per page count matches expected value");
            
            Assert.assertEquals(usersResponse.getTotal(), testData.get("expectedTotal"));
            testLogger.logAssertionPassed("Total count matches expected value");
            
            Assert.assertNotNull(usersResponse.getData());
            Assert.assertFalse(usersResponse.getData().isEmpty());
            testLogger.logAssertionPassed("Users data is present and not empty");
        });
    }

    @Test(priority = 2, description = "Get single user by ID")
    public void testGetUserById() {
        executeApiTestWithManualExceptionHandling("Get User By ID", "Verify that single user API returns correct user data",
                                                 "testGetUserById", "UserApiTests", () -> {
            long startTime = System.currentTimeMillis();
            
            try {
                Map<String, Object> testData = testDataProvider.getTestDataForCase("getUserById");
                int userId = (Integer) testData.get("userId");
                
                logApiRequest("GET", "/users/" + userId, null, getApiHeaders());
                
                Response response = RestAssured.given()
                        .headers(getApiHeaders())
                        .when()
                        .get("/users/" + userId);
                
                logApiResponse(response);
                
                // Validate status code
                validateStatusCode(response, 200);
                testLogger.logAssertionPassed("Status code is 200");
                
                // Parse response
                UserResponse userResponse = objectMapper.readValue(response.getBody().asString(), UserResponse.class);
                
                // Validate user data
                Assert.assertEquals(userResponse.getData().getId(), userId);
                testLogger.logAssertionPassed("User ID matches expected value");
                
                Assert.assertEquals(userResponse.getData().getEmail(), testData.get("expectedEmail"));
                testLogger.logAssertionPassed("Email matches expected value");
                
                Assert.assertEquals(userResponse.getData().getFirstName(), testData.get("expectedFirstName"));
                testLogger.logAssertionPassed("First name matches expected value");
                
                endTestPassed("testGetUserById", "User data validation successful", startTime);
                
            } catch (Exception e) {
                handleTestException("testGetUserById", e, startTime);
            }
        });
    }

    @Test(priority = 3, description = "Create new user")
    public void testCreateUser() {
        reportManager.startTest("Create User", "Verify that user creation API works correctly");
        testLogger.logTestStart("testCreateUser", "UserApiTests");
        
        long startTime = System.currentTimeMillis();
        
        try {
            Map<String, Object> testData = testDataProvider.getTestDataForCase("createUser");
            CreateUserRequest createRequest = new CreateUserRequest(
                    (String) testData.get("name"),
                    (String) testData.get("job")
            );
            
            String requestBody = objectMapper.writeValueAsString(createRequest);
            
            testLogger.logApiRequest("POST", "/users", requestBody);
            reportManager.logApiRequest("POST", "/users", requestBody, getApiHeaders().toString());
            
            Response response = RestAssured.given()
                    .headers(getApiHeaders())
                    .body(requestBody)
                    .when()
                    .post("/users");
            
            testLogger.logApiResponse(response.getStatusCode(), response.getBody().asString(), response.getTime());
            reportManager.logApiResponse(response, response.getBody().asString());
            
            // Validate status code
            validateStatusCode(response, 201);
            testLogger.logAssertionPassed("Status code is 201");
            
            // Parse response
            CreateUserResponse createResponse = objectMapper.readValue(response.getBody().asString(), CreateUserResponse.class);
            
            // Validate created user data
            Assert.assertEquals(createResponse.getName(), testData.get("name"));
            testLogger.logAssertionPassed("Created user name matches request");
            
            Assert.assertEquals(createResponse.getJob(), testData.get("job"));
            testLogger.logAssertionPassed("Created user job matches request");
            
            Assert.assertNotNull(createResponse.getId());
            testLogger.logAssertionPassed("User ID is generated");
            
            Assert.assertNotNull(createResponse.getCreatedAt());
            testLogger.logAssertionPassed("Creation timestamp is present");
            
            long endTime = System.currentTimeMillis();
            testLogger.logTestEnd("testCreateUser", "PASSED", endTime - startTime);
            reportManager.markTestPassed("testCreateUser", "User created successfully with all validations passed");
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            testLogger.logTestEnd("testCreateUser", "FAILED", endTime - startTime);
            testLogger.logError("Test failed with exception", e);
            reportManager.markTestFailed("testCreateUser", "Test failed: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Test(priority = 4, description = "Test user not found scenario")
    public void testUserNotFound() {
        reportManager.startTest("User Not Found", "Verify that API returns 404 for non-existent user");
        testLogger.logTestStart("testUserNotFound", "UserApiTests");
        
        long startTime = System.currentTimeMillis();
        
        try {
            Map<String, Object> testData = testDataProvider.getTestDataForCase("userNotFound");
            int userId = (Integer) testData.get("userId");
            
            testLogger.logApiRequest("GET", "/users/" + userId, null);
            reportManager.logApiRequest("GET", "/users/" + userId, null, getApiHeaders().toString());
            
            Response response = RestAssured.given()
                    .headers(getApiHeaders())
                    .when()
                    .get("/users/" + userId);
            
            testLogger.logApiResponse(response.getStatusCode(), response.getBody().asString(), response.getTime());
            reportManager.logApiResponse(response, response.getBody().asString());
            
            // Validate status code for not found
            validateStatusCode(response, 404);
            testLogger.logAssertionPassed("Status code is 404 for non-existent user");
            
            long endTime = System.currentTimeMillis();
            testLogger.logTestEnd("testUserNotFound", "PASSED", endTime - startTime);
            reportManager.markTestPassed("testUserNotFound", "404 status code validation successful");
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            testLogger.logTestEnd("testUserNotFound", "FAILED", endTime - startTime);
            testLogger.logError("Test failed with exception", e);
            reportManager.markTestFailed("testUserNotFound", "Test failed: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public void tearDown() {
        testLogger.logTestSuiteEnd("User API Test Suite", 4, 4, 0, 0);
        reportManager.finalizeReport();
        testLogger.logInfo("Test execution completed. Report generated at: " + reportManager.getReportPath());
    }
}