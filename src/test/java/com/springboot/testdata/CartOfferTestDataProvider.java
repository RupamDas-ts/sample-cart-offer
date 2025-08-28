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

                // Critical precision scenarios
                Arguments.of(100, 99, 1, "99% discount - precision edge case")
        );
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
                Arguments.of(50, 4, 200, 200, "Invalid user segment - no discount"),

                // Essential boundary cases
                Arguments.of(0, 1, 200, 200, "Zero restaurant ID - invalid input"),
                Arguments.of(-1, 1, 200, 200, "Negative restaurant ID - invalid input"));
    }

    // ============ BUSINESS RULE TEST DATA ============

    /**
     * Test data for complex business rule scenarios
     * 
     * @return Stream of test arguments: scenario, restaurantId, userId, cartValue,
     *         expectedBehavior
     */
    public static Stream<Arguments> getBusinessRuleTestData() {
        return Stream.of(
                // Essential business rules
                Arguments.of("FIRST_MATCH", 1, 200, "First matching offer should be applied"),
                Arguments.of("SEGMENT_PRIORITY", 2, 300, "Segment-specific offer priority"),
                Arguments.of("NO_STACKING", 1, 150, "Multiple offers should not stack"),

                // Critical business logic
                Arguments.of("OFFER_CONFLICT", 1, 200, "Conflicting offers - should resolve gracefully"));
    }

    // ============ CART BOUNDARY TEST DATA ============

    /**
     * Test data for cart boundary scenarios
     * 
     * @return Stream of test arguments: cartValue, discountAmount, expectedResult,
     *         description
     */
    public static Stream<Arguments> getCartBoundaryTestData() {
        return Stream.of(
                // Essential boundary scenarios
                Arguments.of(50, 50, 0, "Full discount - cart becomes zero"),
                Arguments.of(30, 40, 0, "Over-discount - cart should not go negative"),

                // Critical edge cases
                Arguments.of(0, 10, 0, "Zero cart value - no discount applied"),
                Arguments.of(-5, 10, 0, "Negative cart value - should be handled gracefully"));
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

    // ============ DECIMAL & EXTREME VALUE TEST DATA ============

    /**
     * Test data for decimal cart values and extreme integer scenarios
     * Note: This method uses double parameters and should be used with test methods
     * that can handle decimal values
     * 
     * @return Stream of test arguments: cartValue, discountAmount, expectedResult,
     *         description
     */
    public static Stream<Arguments> getDecimalAndExtremeValueTestData() {
        return Stream.of(
                // Decimal cart value scenarios
                Arguments.of(99.99, 10.0, 89.99, "Decimal cart value with FLATX discount"),
                Arguments.of(100.50, 50.0, 50.50, "Half decimal cart value with large discount"),
                Arguments.of(0.01, 0.01, 0.0, "Minimum decimal cart value boundary"),
                Arguments.of(999.99, 999.99, 0.0, "High precision decimal cart with exact discount"),

                // Maximum integer scenarios
                Arguments.of((double) Integer.MAX_VALUE, 100.0, (double) (Integer.MAX_VALUE - 100),
                        "Maximum integer cart value handling"),
                Arguments.of((double) Integer.MAX_VALUE, (double) Integer.MAX_VALUE, 0.0,
                        "Maximum integer cart with maximum discount"),
                Arguments.of((double) Integer.MAX_VALUE, 0.0, (double) Integer.MAX_VALUE,
                        "Maximum integer cart with zero discount"),

                // Mixed precision scenarios
                Arguments.of(100.0, 99.99, 0.01, "Whole number cart with high precision discount"),
                Arguments.of(99.99, 100.0, 0.0, "Decimal cart with whole number discount"));
    }
}
