package com.springboot.testdata;

import com.springboot.controller.OfferRequest;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

public class CartOfferTestDataProvider {

    // ============ FLATX OFFER TEST DATA ============
    /**
     * Test data for FLATX offer scenarios
     *
     * @return Stream of test arguments: cartValue, discountAmount, expectedResult,
     *         description
     */
    public static Stream<Arguments> getFlatXOfferTestData() {
        return Stream.of(
                // Basic scenarios
                Arguments.of(200, 10, 190, "Basic FLATX discount - 10 off 200"),
                Arguments.of(500, 50, 450, "Standard discount - 50 off 500"),

                // Essential edge cases
                Arguments.of(1000, 100, 900, "Large cart value with moderate discount"),
                Arguments.of(10, 0, 10, "Zero discount - no change expected"),

                // Critical boundary scenarios
                Arguments.of(50, 50, 0, "Full discount - cart becomes zero"),
                Arguments.of(30, 40, 0, "Over-discount - cart should not go negative"));
    }

    // ============ PERCENTAGE OFFER TEST DATA ============

    /**
     * Test data for percentage offer scenarios
     *
     * @return Stream of test arguments: cartValue, discountPercent, expectedResult,
     *         description
     */
    public static Stream<Arguments> getFlatPercentOfferTestData() {
        return Stream.of(
                // Standard percentages
                Arguments.of(200, 10, 180, "10% discount - 20 off 200"),
                Arguments.of(500, 20, 400, "20% discount - 100 off 500"),

                // Essential boundary percentages
                Arguments.of(200, 0, 200, "0% discount - no change"),
                Arguments.of(100, 100, 0, "100% discount - free order"),

                // Critical rounding scenarios - Real world edge cases
                Arguments.of(199, 33, 133, "33% of 199 - rounding scenario"),
                Arguments.of(101, 50, 50, "50% of 101 - rounding to nearest integer"));
    }

    // ============ OFFER CREATION TEST DATA ============

    /**
     * Test data for offer creation scenarios
     *
     * @return Stream of test arguments: restaurantId, offerType, offerValue,
     *         segments, expectedResponse
     */
    public static Stream<Arguments> getOfferCreationTestData() {
        return Stream.of(
                // Valid offer types
                Arguments.of(1, "FLATX", 10, List.of("p1"), "success", "Valid FLATX offer"),
                Arguments.of(2, "FLAT%", 15, List.of("p2"), "success", "Valid percentage offer"),

                // Essential multi-segment offers
                Arguments.of(4, "FLATX", 25, List.of("p1", "p2"), "success", "Multi-segment FLATX offer"),

                // Critical boundary values
                Arguments.of(6, "FLATX", 0, List.of("p1"), "success", "Zero discount offer"),
                Arguments.of(7, "FLAT%", 100, List.of("p1"), "success", "100% discount offer"),

                // Case sensitivity tests - Real world issue
                Arguments.of(8, "flatx", 10, List.of("p1"), "error", "Lowercase offer type"),
                Arguments.of(9, "FlatX", 10, List.of("p1"), "error", "Mixed case offer type"),

                // Essential error cases
                Arguments.of(10, "FLATX", -5, List.of("p1"), "error", "Negative discount value"),
                Arguments.of(11, "FLAT%", 110, List.of("p1"), "error", "Percentage over 100%"),

                // Key edge cases
                Arguments.of(12, "INVALID_TYPE", 10, List.of("p1"), "error", "Invalid offer type"),
                Arguments.of(13, "FLATX", 10, List.of(), "error", "Empty segment list"));
    }

    // ============ MULTI-USER SEGMENT TEST DATA ============

    /**
     * Test data for multiple user segments with different offers
     *
     * @return Stream of test arguments: userId, expectedSegment, cartValue,
     *         expectedResult, description
     */
    public static Stream<Arguments> getMultiUserSegmentTestData() {
        return Stream.of(
                Arguments.of(1, "p1", 200, 190, "User p1 gets FLATX offer (10 discount)"),
                Arguments.of(2, "p2", 200, 160, "User p2 gets PERCENTAGE offer (20% discount)"),
                Arguments.of(3, "p3", 200, 200, "User p3 gets no matching offer"));
    }

    // ============ RESTAURANT WITHOUT OFFERS TEST DATA ============

    /**
     * Test data for restaurants with no configured offers - Critical real world scenario
     *
     * @return Stream of test arguments: restaurantId, userId, cartValue,
     *         expectedResult, description
     */
    public static Stream<Arguments> getRestaurantWithoutOffersTestData() {
        return Stream.of(
                Arguments.of(999, 1, 200, 200, "Restaurant with no offers - p1 user"),
                Arguments.of(999, 2, 150, 150, "Restaurant with no offers - p2 user"),
                Arguments.of(999, 3, 300, 300, "Restaurant with no offers - p3 user"),

                // Edge case: Restaurant that never had offers configured
                Arguments.of(1001, 1, 100, 100, "Fresh restaurant with no offer history"));
    }

    // ============ MULTIPLE OFFERS SAME SEGMENT TEST DATA ============

    /**
     * Test data for restaurants with multiple offers for the same segment - Business rule testing
     *
     * @return Stream of test arguments: scenario, cartValue, expectedResult, description
     */
    public static Stream<Arguments> getMultipleOffersSameSegmentTestData() {
        return Stream.of(
                // Multiple FLATX offers for p1 segment
                Arguments.of("MULTIPLE_FLATX_P1", 200, 190, "Multiple FLATX offers for p1 - first match wins"),

                // Mixed offer types for same segment
                Arguments.of("MIXED_OFFERS_P1", 200, 190, "FLATX and PERCENTAGE for p1 - first match wins"),

                // Multiple percentage offers
                Arguments.of("MULTIPLE_PERCENT_P2", 100, 90, "Multiple percentage offers for p2 - first match wins"));
    }

    // ============ INVALID SCENARIO TEST DATA ============

    /**
     * Test data for invalid/error scenarios
     *
     * @return Stream of test arguments: restaurantId, userId, cartValue,
     *         expectedResult, description
     */
    public static Stream<Arguments> getInvalidScenarioTestData() {
        return Stream.of(
                Arguments.of(999, 1, 200, 200, "Non-existent restaurant - no discount"),

                // Essential boundary cases
                Arguments.of(0, 1, 200, 200, "Zero restaurant ID - invalid input"),
                Arguments.of(-1, 1, 200, 200, "Negative restaurant ID - invalid input"),

                // Invalid user IDs - Real world scenarios
                Arguments.of(1, 0, 200, 200, "Zero user ID - invalid user"),
                Arguments.of(1, -1, 200, 200, "Negative user ID - invalid user"));
    }

    // ============ SERVICE FAILURE SIMULATION TEST DATA ============

    /**
     * Test data for user segment service failure scenarios - Critical for production
     *
     * @return Stream of test arguments: userId, expectedBehavior, cartValue, description
     */
    public static Stream<Arguments> getUserSegmentServiceFailureTestData() {
        return Stream.of(
                // Users not configured in mock service will cause service failure
                Arguments.of(404, "SERVICE_ERROR", 200, "User not found in segment service"),
                Arguments.of(500, "SERVICE_ERROR", 150, "Service returns 500 error"),
                Arguments.of(999, "SERVICE_ERROR", 100, "User segment service timeout scenario"));
    }

    // ============ INPUT VALIDATION TEST DATA ============

    /**
     * Test data for null and invalid input validation
     *
     * @return Stream of test arguments: cartValue, userId, restaurantId, expectedBehavior, description
     */
    public static Stream<Arguments> getInputValidationTestData() {
        return Stream.of(
                // Zero and negative cart values
                Arguments.of(0, 1, 1, "ZERO_CART", "Zero cart value - should handle gracefully"),
                Arguments.of(-100, 1, 1, "NEGATIVE_CART", "Negative cart value - should handle gracefully"),

                // Large cart values - Real world high-value orders
                Arguments.of(999999, 1, 1, "LARGE_CART", "Very large cart value - system boundary test"));
    }

    // ============ CART BOUNDARY TEST DATA ============

    /**
     * Test data for cart boundary scenarios - Focused on realistic boundaries
     *
     * @return Stream of test arguments: cartValue, discountAmount, expectedResult,
     *         description
     */
    public static Stream<Arguments> getCartBoundaryTestData() {
        return Stream.of(
                // Essential boundary scenarios
                Arguments.of(50, 50, 0, "Full discount - cart becomes zero"),
                Arguments.of(30, 40, 0, "Over-discount - cart should not go negative"),
                Arguments.of(1, 5, 0, "Small cart with large discount"),

                // Real-world minimum order scenarios
                Arguments.of(0, 10, 0, "Zero cart value - no discount applied"),
                Arguments.of(-5, 10, 0, "Negative cart value - should be handled gracefully"));
    }

    // ============ BUSINESS RULE TEST DATA ============

    /**
     * Test data for complex business rule scenarios - Enhanced with realistic scenarios
     *
     * @return Stream of test arguments: scenario, userId, cartValue, expectedBehavior
     */
    public static Stream<Arguments> getBusinessRuleTestData() {
        return Stream.of(
                // Essential business rules
                Arguments.of("FIRST_MATCH", 1, 200, "First matching offer should be applied"),
                Arguments.of("SEGMENT_PRIORITY", 2, 300, "Segment-specific offer priority"),
                Arguments.of("NO_STACKING", 1, 150, "Multiple offers should not stack"),

                // Enhanced business logic scenarios
                Arguments.of("DISCOUNT_CAP", 1, 100, "High percentage discounts should not exceed cart value"),
                Arguments.of("OFFER_PRECEDENCE", 1, 250, "FLATX vs PERCENTAGE precedence rules"));
    }

    /**
     * Creates a standard FLATX offer for testing
     */
    public static OfferRequest createFlatXOffer(int restaurantId, int discountAmount, String... segments) {
        return new OfferRequest(restaurantId, "FLATX", discountAmount, List.of(segments));
    }

    /**
     * Creates a standard percentage offer for testing
     */
    public static OfferRequest createPercentageOffer(int restaurantId, int discountPercent, String... segments) {
        return new OfferRequest(restaurantId, "FLAT%", discountPercent, List.of(segments));
    }

    /**
     * Creates multiple offers for the same segment - for testing business rules
     */
    public static OfferRequest[] createMultipleOffersForSegment(int restaurantId, String segment) {
        return new OfferRequest[]{
                new OfferRequest(restaurantId, "FLATX", 10, List.of(segment)),
                new OfferRequest(restaurantId, "FLATX", 20, List.of(segment)),
                new OfferRequest(restaurantId, "FLAT%", 15, List.of(segment))
        };
    }

    /**
     * Creates test offer with invalid data for validation testing
     */
    public static OfferRequest createInvalidOffer(String invalidOfferType, int restaurantId) {
        return new OfferRequest(restaurantId, invalidOfferType, 10, List.of("p1"));
    }
}