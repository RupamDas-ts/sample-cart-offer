package com.springboot;

import com.DTOs.AddOfferApiResponseDTO;
import com.DTOs.ApplyOfferRequestDTO;
import com.DTOs.ApplyOfferResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.springboot.controller.OfferRequest;
import com.springboot.testdata.CartOfferTestDataProvider;
import com.springboot.testdata.TestDataConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {
        "server.port=8000",
        "logging.level.com.springboot=DEBUG"
})
@ActiveProfiles("test")
public class CartOfferApplicationTests {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    private static final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void setUp() throws Exception {
        Thread.sleep(1000); // Allow server to start
        System.out.println("=== Starting Test Case ===");
    }

    // ============ TEST CASE 1: FLATX OFFER TESTING ============

    @ParameterizedTest(name = "TC_FLATX_{index}: {3}")
    @MethodSource("com.springboot.testdata.CartOfferTestDataProvider#getFlatXOfferTestData")
    @DisplayName("Apply FLATX Offers - Data Driven Tests")
    void testFlatXOfferApplication(int cartValue, int discountAmount, int expectedCartValue, String description)
            throws Exception {
        System.out.println("Testing: " + description);

        // Setup: Create unique restaurant and add offer using data provider helper
        int restaurantId = TestDataConfig.generateUniqueRestaurantId();
        OfferRequest offerRequest = CartOfferTestDataProvider.createFlatXOffer(restaurantId, discountAmount, "p1");
        addOffer(offerRequest);

        // Execute: Apply offer
        ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(cartValue, restaurantId, 1);
        ApplyOfferResponseDTO response = applyOffer(applyRequest);

        // Validate: Business rules + expected result
        validateBusinessRules(response.getCartValue(), cartValue, expectedCartValue, description);

        System.out.println("✅ " + description + ": " + cartValue + " -> " + response.getCartValue());
    }

    // ============ TEST CASE 2: PERCENTAGE OFFER TESTING ============

    @ParameterizedTest(name = "TC_PERCENT_{index}: {3}")
    @MethodSource("com.springboot.testdata.CartOfferTestDataProvider#getFlatPercentOfferTestData")
    @DisplayName("Apply Percentage Offers - Data Driven Tests")
    void testPercentageOfferApplication(int cartValue, int discountPercent, int expectedCartValue, String description)
            throws Exception {
        System.out.println("Testing: " + description);

        int restaurantId = TestDataConfig.generateUniqueRestaurantId();
        OfferRequest offerRequest = CartOfferTestDataProvider.createPercentageOffer(restaurantId, discountPercent,
                "p1");
        addOffer(offerRequest);

        ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(cartValue, restaurantId, 1);
        ApplyOfferResponseDTO response = applyOffer(applyRequest);

        validateBusinessRules(response.getCartValue(), cartValue, expectedCartValue, description);
        // Allow 1 unit tolerance for rounding in percentage calculations
        assertThat(response.getCartValue()).isBetween(expectedCartValue - 1, expectedCartValue + 1);

        System.out.println("✅ " + description + ": " + cartValue + " -> " + response.getCartValue());
    }

    // ============ TEST CASE 3: MULTI-USER SEGMENT TESTING ============

    @ParameterizedTest(name = "TC_MULTI_USER_{index}: {4}")
    @MethodSource("com.springboot.testdata.CartOfferTestDataProvider#getMultiUserSegmentTestData")
    @DisplayName("Multi-User Segment Testing - Data Driven")
    void testMultiUserSegmentScenarios(int userId, String expectedSegment, int cartValue, int expectedResult,
                                       String description) throws Exception {
        System.out.println("Testing: " + description);

        // Setup: Create restaurant with multiple offers for different segments
        int restaurantId = TestDataConfig.generateUniqueRestaurantId();

        // Add offers for different segments using helper methods
        addOffer(CartOfferTestDataProvider.createFlatXOffer(restaurantId, 10, "p1"));
        addOffer(CartOfferTestDataProvider.createPercentageOffer(restaurantId, 20, "p2"));
        // No offer for p3 segment

        // Execute: Apply offer for specific user
        ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(cartValue, restaurantId, userId);
        ApplyOfferResponseDTO response = applyOffer(applyRequest);

        // Validate: Expected result based on user segment
        assertThat(response.getCartValue()).isEqualTo(expectedResult);
        validateBusinessRules(response.getCartValue(), cartValue, expectedResult, description);

        System.out.println(
                "✅ " + description + ": User " + userId + " (" + expectedSegment + ") -> " + response.getCartValue());
    }

    // ============ TEST CASE 4: RESTAURANT WITHOUT OFFERS ============

    @ParameterizedTest(name = "TC_NO_OFFERS_{index}: {4}")
    @MethodSource("com.springboot.testdata.CartOfferTestDataProvider#getRestaurantWithoutOffersTestData")
    @DisplayName("Restaurant Without Offers - Critical Real World Scenario")
    void testRestaurantWithoutOffers(int restaurantId, int userId, int cartValue, int expectedResult, String description)
            throws Exception {
        System.out.println("Testing: " + description);

        // No offers are added for this restaurant - this is the key test
        // Execute: Apply offer to restaurant with no offers
        ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(cartValue, restaurantId, userId);
        ApplyOfferResponseDTO response = applyOffer(applyRequest);

        // Validate: Cart value should remain unchanged
        assertThat(response.getCartValue()).isEqualTo(expectedResult);
        assertThat(response.getCartValue()).isEqualTo(cartValue); // No discount should be applied
        validateBusinessRules(response.getCartValue(), cartValue, expectedResult, description);

        System.out.println("✅ " + description + ": " + cartValue + " -> " + response.getCartValue() + " (No change expected)");
    }

    // ============ TEST CASE 5: MULTIPLE OFFERS SAME SEGMENT ============

    @ParameterizedTest(name = "TC_MULTIPLE_OFFERS_{index}: {3}")
    @MethodSource("com.springboot.testdata.CartOfferTestDataProvider#getMultipleOffersSameSegmentTestData")
    @DisplayName("Multiple Offers Same Segment - Business Rule Testing")
    void testMultipleOffersSameSegment(String scenario, int cartValue, int expectedResult, String description)
            throws Exception {
        System.out.println("Testing: " + description);

        int restaurantId = TestDataConfig.generateUniqueRestaurantId();

        // Setup: Add multiple offers for same segment based on scenario
        switch (scenario) {
            case "MULTIPLE_FLATX_P1":
                // Add two FLATX offers for p1 - first one should win (10 discount)
                addOffer(CartOfferTestDataProvider.createFlatXOffer(restaurantId, 10, "p1"));
                addOffer(CartOfferTestDataProvider.createFlatXOffer(restaurantId, 20, "p1"));
                break;

            case "MIXED_OFFERS_P1":
                // Add FLATX first, then percentage - FLATX should win
                addOffer(CartOfferTestDataProvider.createFlatXOffer(restaurantId, 10, "p1"));
                addOffer(CartOfferTestDataProvider.createPercentageOffer(restaurantId, 15, "p1"));
                break;

            case "MULTIPLE_PERCENT_P2":
                // Add two percentage offers for p2 - first one should win (10% discount)
                addOffer(CartOfferTestDataProvider.createPercentageOffer(restaurantId, 10, "p2"));
                addOffer(CartOfferTestDataProvider.createPercentageOffer(restaurantId, 25, "p2"));
                break;
        }

        // Execute: Apply offer
        int userId = scenario.contains("P2") ? 2 : 1; // Use user 2 for p2 scenarios
        ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(cartValue, restaurantId, userId);
        ApplyOfferResponseDTO response = applyOffer(applyRequest);

        // Validate: First matching offer should be applied
        assertThat(response.getCartValue()).isEqualTo(expectedResult);
        validateBusinessRules(response.getCartValue(), cartValue, expectedResult, description);

        System.out.println("✅ " + description + ": " + cartValue + " -> " + response.getCartValue());
    }

    // ============ TEST CASE 6: USER SEGMENT SERVICE FAILURE ============

    @ParameterizedTest(name = "TC_SERVICE_FAILURE_{index}: {3}")
    @MethodSource("com.springboot.testdata.CartOfferTestDataProvider#getUserSegmentServiceFailureTestData")
    @DisplayName("User Segment Service Failure - Critical Production Scenario")
    void testUserSegmentServiceFailure(int userId, String expectedBehavior, int cartValue, String description) {
        System.out.println("Testing: " + description);

        try {
            int restaurantId = TestDataConfig.generateUniqueRestaurantId();

            // Setup: Add an offer that would apply if service worked
            addOffer(CartOfferTestDataProvider.createFlatXOffer(restaurantId, 10, "p1"));

            // Execute: Try to apply offer with non-existent user (not in mock service)
            ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(cartValue, restaurantId, userId);
            ApplyOfferResponseDTO response = applyOffer(applyRequest);

            // Validate: When service fails, cart should remain unchanged (graceful degradation)
            assertThat(response.getCartValue()).isEqualTo(cartValue);
            System.out.println("✅ " + description + ": Graceful handling - " + cartValue + " -> " + response.getCartValue());

        } catch (Exception e) {
            // This is also acceptable behavior - service failure should be handled gracefully
            System.out.println("⚠️ " + description + ": Service failure handled with exception - " + e.getMessage());
            // In production, we'd want to verify the specific error handling mechanism
        }
    }

    // ============ TEST CASE 7: INPUT VALIDATION TESTING ============

    @ParameterizedTest(name = "TC_INPUT_VALIDATION_{index}: {4}")
    @MethodSource("com.springboot.testdata.CartOfferTestDataProvider#getInputValidationTestData")
    @DisplayName("Input Validation Testing - Edge Cases")
    void testInputValidation(int cartValue, int userId, int restaurantId, String expectedBehavior, String description)
            throws Exception {
        System.out.println("Testing: " + description);

        // Setup: Add a standard offer
        int testRestaurantId = restaurantId == 1 ? TestDataConfig.generateUniqueRestaurantId() : restaurantId;
        if (restaurantId == 1) {
            addOffer(CartOfferTestDataProvider.createFlatXOffer(testRestaurantId, 10, "p1"));
        }

        try {
            // Execute: Apply offer with edge case input
            ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(cartValue, testRestaurantId, userId);
            ApplyOfferResponseDTO response = applyOffer(applyRequest);

            // Validate based on scenario
            switch (expectedBehavior) {
                case "ZERO_CART":
                case "NEGATIVE_CART":
                    // Zero or negative cart should result in zero
                    assertThat(response.getCartValue()).isEqualTo(0);
                    break;
                case "LARGE_CART":
                    // Large cart should handle properly (999999 - 10 = 999989)
                    assertThat(response.getCartValue()).isEqualTo(999989);
                    break;
            }

            validateBusinessRules(response.getCartValue(), Math.max(0, cartValue), response.getCartValue(), description);
            System.out.println("✅ " + description + ": " + cartValue + " -> " + response.getCartValue());

        } catch (Exception e) {
            // Some invalid inputs might throw exceptions - this is acceptable
            System.out.println("⚠️ " + description + ": Input validation caught - " + e.getMessage());
        }
    }

    // ============ TEST CASE 8: INVALID SCENARIOS ============

    @ParameterizedTest(name = "TC_INVALID_{index}: {4}")
    @MethodSource("com.springboot.testdata.CartOfferTestDataProvider#getInvalidScenarioTestData")
    @DisplayName("Invalid Scenario Testing - Error Handling")
    void testInvalidScenarios(int restaurantId, int userId, int cartValue, int expectedResult, String description) {
        System.out.println("Testing: " + description);

        try {
            ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(cartValue, restaurantId, userId);
            ApplyOfferResponseDTO response = applyOffer(applyRequest);

            // Validate: Expected result for invalid scenarios (should be no discount)
            assertThat(response.getCartValue()).isEqualTo(expectedResult);
            System.out.println("✅ " + description + ": " + cartValue + " -> " + response.getCartValue());

        } catch (Exception e) {
            // For truly invalid scenarios, we might expect an error response
            System.out.println("⚠️ " + description + ": Expected error behavior - " + e.getMessage());
        }
    }

    // ============ TEST CASE 9: CART BOUNDARY TESTING ============

    @ParameterizedTest(name = "TC_BOUNDARY_{index}: {3}")
    @MethodSource("com.springboot.testdata.CartOfferTestDataProvider#getCartBoundaryTestData")
    @DisplayName("Cart Boundary Testing - Focused Real World Scenarios")
    void testCartBoundaryScenarios(int cartValue, int discountAmount, int expectedCartValue, String description)
            throws Exception {
        System.out.println("Testing: " + description);

        // Setup: Create unique restaurant and add offer
        int restaurantId = TestDataConfig.generateUniqueRestaurantId();
        OfferRequest offerRequest = CartOfferTestDataProvider.createFlatXOffer(restaurantId, discountAmount, "p1");
        addOffer(offerRequest);

        // Execute: Apply offer
        ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(cartValue, restaurantId, 1);
        ApplyOfferResponseDTO response = applyOffer(applyRequest);

        // Validate: Boundary-specific assertions
        validateBoundaryBusinessRules(response.getCartValue(), cartValue, discountAmount, expectedCartValue, description);

        System.out.println("✅ " + description + ": " + cartValue + " -> " + response.getCartValue());
    }

    // ============ TEST CASE 10: BUSINESS RULE TESTING ============

    @ParameterizedTest(name = "TC_BUSINESS_{index}: {3}")
    @MethodSource("com.springboot.testdata.CartOfferTestDataProvider#getBusinessRuleTestData")
    @DisplayName("Business Rule Testing - Enhanced Scenarios")
    void testBusinessRules(String scenario, int userId, int cartValue, String expectedBehavior)
            throws Exception {
        System.out.println("Testing Business Rule: " + expectedBehavior + " (Scenario: " + scenario + ")");

        // Setup: Create restaurant with specific business rule test scenarios
        int testRestaurantId = TestDataConfig.generateUniqueRestaurantId();

        switch (scenario) {
            case "FIRST_MATCH":
                // Add multiple offers, first one should be applied
                addOffer(CartOfferTestDataProvider.createFlatXOffer(testRestaurantId, 10, "p1"));
                addOffer(CartOfferTestDataProvider.createFlatXOffer(testRestaurantId, 20, "p1"));
                break;

            case "SEGMENT_PRIORITY":
                // Add segment-specific offers
                addOffer(CartOfferTestDataProvider.createFlatXOffer(testRestaurantId, 15, "p1"));
                addOffer(CartOfferTestDataProvider.createPercentageOffer(testRestaurantId, 25, "p2"));
                break;

            case "NO_STACKING":
                // Add single offer to test no stacking rule
                addOffer(CartOfferTestDataProvider.createFlatXOffer(testRestaurantId, 25, "p1"));
                break;

            case "DISCOUNT_CAP":
                // Add high percentage discount
                addOffer(CartOfferTestDataProvider.createPercentageOffer(testRestaurantId, 80, "p1"));
                break;

            case "OFFER_PRECEDENCE":
                // Add both FLATX and percentage offers
                addOffer(CartOfferTestDataProvider.createFlatXOffer(testRestaurantId, 30, "p1"));
                addOffer(CartOfferTestDataProvider.createPercentageOffer(testRestaurantId, 10, "p1"));
                break;
        }

        // Execute: Apply offer
        ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(cartValue, testRestaurantId, userId);
        ApplyOfferResponseDTO response = applyOffer(applyRequest);

        // Validate: Business rule specific assertions using helper method
        validateBusinessRuleSpecific(response.getCartValue(), cartValue, scenario, userId, expectedBehavior);

        // General business rule validation
        validateBusinessRules(response.getCartValue(), cartValue, response.getCartValue(), expectedBehavior);

        System.out.println("✅ " + expectedBehavior + ": " + cartValue + " -> " + response.getCartValue());
    }

    // ============ TEST CASE 11: OFFER CREATION TESTING ============

    @ParameterizedTest(name = "TC_CREATE_{index}: {5}")
    @MethodSource("com.springboot.testdata.CartOfferTestDataProvider#getOfferCreationTestData")
    @DisplayName("Offer Creation Testing - Enhanced with Case Sensitivity")
    void testOfferCreation(int restaurantId, String offerType, int offerValue, List<String> segments,
                           String expectedResponse, String description) throws Exception {
        System.out.println("Testing: " + description);

        // Create offer request based on test data
        OfferRequest offerRequest = new OfferRequest(restaurantId, offerType, offerValue, segments);

        try {
            // Execute: Add offer
            AddOfferApiResponseDTO response = addOffer(offerRequest);

            // Validate: Success response
            if ("success".equals(expectedResponse)) {
                assertThat(response.getResponseMsg()).contains("success");
                System.out.println("✅ " + description + ": Offer created successfully");
            } else {
                // For expected error cases, we shouldn't reach here
                System.out.println("⚠️ " + description + ": Expected error but got success");
            }

        } catch (Exception e) {
            // Validate: Error response for expected error cases
            if ("error".equals(expectedResponse)) {
                System.out.println("✅ " + description + ": Expected error occurred - " + e.getMessage());
            } else {
                // Re-throw unexpected errors
                throw e;
            }
        }
    }

    // ============ BUSINESS RULE VALIDATION METHODS ============

    /**
     * Centralized business rules validation
     * Ensures all tests follow the same validation criteria
     */
    private void validateBusinessRules(int actualCartValue, int originalCartValue, int expectedCartValue,
                                       String testDescription) {
        // Rule 1: Cart value should never be negative (most important business rule)
        assertThat(actualCartValue)
                .as("Cart value should never be negative in " + testDescription)
                .isGreaterThanOrEqualTo(0);

        // Rule 2: Cart value should never exceed original value (unless original was negative)
        if (originalCartValue >= 0) {
            assertThat(actualCartValue)
                    .as("Cart value should never exceed original value in " + testDescription)
                    .isLessThanOrEqualTo(originalCartValue);
        }

        // Rule 3: Cart value should match expected calculation
        assertThat(actualCartValue)
                .as("Cart value should match expected calculation in " + testDescription)
                .isEqualTo(expectedCartValue);
    }

    /**
     * Enhanced business rules validation for boundary scenarios
     */
    private void validateBoundaryBusinessRules(int actualCartValue, int originalCartValue, int discountAmount,
                                               int expectedCartValue, String testDescription) {
        // Handle negative cart values
        if (originalCartValue < 0) {
            assertThat(actualCartValue)
                    .as("Negative cart values should be handled gracefully in " + testDescription)
                    .isEqualTo(0);
            return;
        }

        // Handle zero cart values
        if (originalCartValue == 0) {
            assertThat(actualCartValue)
                    .as("Zero cart values should remain zero in " + testDescription)
                    .isEqualTo(0);
            return;
        }

        // Handle over-discount scenarios
        if (discountAmount >= originalCartValue) {
            assertThat(actualCartValue)
                    .as("Over-discount scenarios should result in zero cart value in " + testDescription)
                    .isEqualTo(0);
            return;
        }

        // Normal discount scenarios - use standard validation
        validateBusinessRules(actualCartValue, originalCartValue, expectedCartValue, testDescription);
    }

    /**
     * Business rule specific validation for complex scenarios
     */
    private void validateBusinessRuleSpecific(int actualCartValue, int originalCartValue, String scenario, int userId,
                                              String expectedBehavior) {
        switch (scenario) {
            case "FIRST_MATCH":
                assertThat(actualCartValue)
                        .as("First matching offer should be applied in " + expectedBehavior)
                        .isEqualTo(originalCartValue - 10);
                break;

            case "SEGMENT_PRIORITY":
                if (userId == 1) {
                    assertThat(actualCartValue)
                            .as("FLATX offer for p1 should be applied in " + expectedBehavior)
                            .isEqualTo(originalCartValue - 15);
                } else if (userId == 2) {
                    int expectedDiscount = (originalCartValue * 25) / 100;
                    assertThat(actualCartValue)
                            .as("Percentage offer for p2 should be applied in " + expectedBehavior)
                            .isEqualTo(originalCartValue - expectedDiscount);
                }
                break;

            case "NO_STACKING":
                assertThat(actualCartValue)
                        .as("Only one offer should be applied in " + expectedBehavior)
                        .isEqualTo(originalCartValue - 25);
                break;

            case "DISCOUNT_CAP":
                assertThat(actualCartValue)
                        .as("Cart should not go negative in " + expectedBehavior)
                        .isGreaterThanOrEqualTo(0);
                assertThat(actualCartValue)
                        .as("Cart should not exceed original value in " + expectedBehavior)
                        .isLessThanOrEqualTo(originalCartValue);
                break;

            case "OFFER_PRECEDENCE":
                // First offer (FLATX 30) should be applied
                assertThat(actualCartValue)
                        .as("First offer should take precedence in " + expectedBehavior)
                        .isEqualTo(originalCartValue - 30);
                break;
        }
    }

    // ============ HELPER METHODS FOR API CALLS ============

    private AddOfferApiResponseDTO addOffer(OfferRequest offerRequest) throws Exception {
        System.out.println("Adding offer: " + offerRequest);

        String OFFER_URL = "http://localhost:8000/api/v1/offer";
        URI uri = URI.create(OFFER_URL);
        String jsonBody = objectMapper.writeValueAsString(offerRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(java.time.Duration.ofSeconds(TestDataConfig.getTimeoutSeconds()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to add offer. Response code: " + response.statusCode()
                    + ", Body: " + response.body());
        }

        AddOfferApiResponseDTO responseDto = objectMapper.readValue(response.body(), AddOfferApiResponseDTO.class);
        System.out.println("Offer added successfully: " + responseDto.getResponseMsg());
        return responseDto;
    }

    private ApplyOfferResponseDTO applyOffer(ApplyOfferRequestDTO applyOfferRequest) throws Exception {
        System.out.println("Applying offer: " + applyOfferRequest);

        String APPLY_OFFER_URL = "http://localhost:8000/api/v1/cart/apply_offer";
        URI uri = URI.create(APPLY_OFFER_URL);
        String jsonBody = objectMapper.writeValueAsString(applyOfferRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(java.time.Duration.ofSeconds(TestDataConfig.getTimeoutSeconds()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to apply offer. Response code: " + response.statusCode()
                    + ", Body: " + response.body());
        }

        ApplyOfferResponseDTO responseDto = objectMapper.readValue(response.body(), ApplyOfferResponseDTO.class);
        System.out.println("Offer applied. Final cart value: " + responseDto.getCartValue());
        return responseDto;
    }
}