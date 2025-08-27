package com.springboot;

import com.DTOs.AddOfferApiResponseDTO;
import com.DTOs.ApplyOfferRequestDTO;
import com.DTOs.ApplyOfferResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.springboot.controller.ApplyOfferRequest;
import com.springboot.controller.ApplyOfferResponse;
import com.springboot.controller.OfferRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {
		"server.port=8000",
		"logging.level.com.springboot=DEBUG"
})

@ActiveProfiles("test")
class CartOfferApplicationTests {

	private final ObjectMapper objectMapper = new ObjectMapper()
			.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
	private final String OFFER_URL = "http://localhost:8080/api/v1/offer";
	private final String APPLY_OFFER_URL = "http://localhost:8080/api/v1/cart/apply_offer";
	private static final HttpClient client = HttpClient.newHttpClient();

	@BeforeEach
	void setUp() throws Exception {
		// Give server time to start and ensure mock server is ready
		Thread.sleep(2000);
		System.out.println("=== Starting Test Case ===");
	}

	// ============ ADD OFFER TESTS ============
	@Test
	@DisplayName("TC_001: Add FLATX offer for single segment successfully")
	void addFlatXOfferForSingleSegment() throws Exception {
		System.out.println("Testing: Add FLATX offer for single segment");

		OfferRequest offerRequest = new OfferRequest(1, "FLATX", 10, List.of("p1"));

		AddOfferApiResponseDTO response = addOffer(offerRequest);

		assertThat(response.getResponseMsg()).isEqualTo("success");

		System.out.println("✅ FLATX offer added successfully");
	}

	@Test
	@DisplayName("TC_002: Add FLAT% offer for single segment successfully")
	void addFlatPercentOfferForSingleSegment() throws Exception {
		System.out.println("Testing: Add FLAT% offer for single segment");

		OfferRequest offerRequest = new OfferRequest(2, "FLAT%", 20, List.of("p1"));

		AddOfferApiResponseDTO response = addOffer(offerRequest);

		assertThat(response.getResponseMsg()).isEqualTo("success");
		System.out.println("✅ FLAT% offer added successfully");
	}

	@Test
	@DisplayName("TC_003: Add offer for multiple segments successfully")
	void addOfferForMultipleSegments() throws Exception {
		System.out.println("Testing: Add offer for multiple segments");

		OfferRequest offerRequest = new OfferRequest(3, "FLATX", 15, List.of("p1", "p2"));

		AddOfferApiResponseDTO response = addOffer(offerRequest);

		assertThat(response.getResponseMsg()).isEqualTo("success");
		System.out.println("✅ Multi-segment offer added successfully");
	}

	@Test
	@DisplayName("TC_004: Add multiple offers for same restaurant")
	void addMultipleOffersForSameRestaurant() throws Exception {
		System.out.println("Testing: Add multiple offers for same restaurant");

		OfferRequest offer1 = new OfferRequest(4, "FLATX", 10, List.of("p1"));
		OfferRequest offer2 = new OfferRequest(4, "FLAT%", 15, List.of("p2"));

		AddOfferApiResponseDTO response1 = addOffer(offer1);
		AddOfferApiResponseDTO response2 = addOffer(offer2);

		assertThat(response1.getResponseMsg()).isEqualTo("success");
		assertThat(response2.getResponseMsg()).isEqualTo("success");
		System.out.println("✅ Multiple offers for same restaurant added successfully");
	}

	@Test
	@DisplayName("TC_005: Add offers for different restaurants")
	void addOffersForDifferentRestaurants() throws Exception {
		System.out.println("Testing: Add offers for different restaurants");

		OfferRequest offer1 = new OfferRequest(5, "FLATX", 10, List.of("p1"));
		OfferRequest offer2 = new OfferRequest(6, "FLAT%", 20, List.of("p1"));

		AddOfferApiResponseDTO response1 = addOffer(offer1);
		AddOfferApiResponseDTO response2 = addOffer(offer2);

		assertThat(response1.getResponseMsg()).isEqualTo("success");
		assertThat(response2.getResponseMsg()).isEqualTo("success");
		System.out.println("✅ Offers for different restaurants added successfully");
	}

	@Test
	@DisplayName("TC_006: Add offer with zero value")
	void addOfferWithZeroValue() throws Exception {
		System.out.println("Testing: Add offer with zero value");

		OfferRequest offerRequest = new OfferRequest(7, "FLATX", 0, List.of("p1"));

		AddOfferApiResponseDTO response = addOffer(offerRequest);

		assertThat(response.getResponseMsg()).isEqualTo("success");
		System.out.println("✅ Zero value offer added successfully");
	}

	// ============ APPLY OFFER TESTS - FLATX ============
	@Test
	@DisplayName("TC_012: Apply FLATX offer successfully")
	void applyFlatXOfferSuccessfully() throws Exception {
		System.out.println("Testing: Apply FLATX offer successfully");

		// Setup: Add FLATX offer
		OfferRequest offerRequest = new OfferRequest(10, "FLATX", 10, List.of("p1"));
		addOffer(offerRequest);

		// Test: Apply offer (user_id=1 is mocked to return segment "p1")
		ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(200, 10, 1);
		ApplyOfferResponseDTO response = applyOffer(applyRequest);

		assertThat(response.getCartValue()).isEqualTo(190); // 200 - 10 = 190
		System.out.println("✅ FLATX offer applied successfully: 200 -> 190");
	}

	@Test
	@DisplayName("TC_013: Apply FLATX offer with exact match")
	void applyFlatXOfferExactMatch() throws Exception {
		System.out.println("Testing: Apply FLATX offer with exact match");

		// Setup: Add FLATX offer
		OfferRequest offerRequest = new OfferRequest(11, "FLATX", 50, List.of("p1"));
		addOffer(offerRequest);

		// Test: Apply offer
		ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(100, 11, 1);
		ApplyOfferResponseDTO response = applyOffer(applyRequest);

		assertThat(response.getCartValue()).isEqualTo(50); // 100 - 50 = 50
		System.out.println("✅ FLATX offer applied with exact match: 100 -> 50");
	}

	@Test
	@DisplayName("TC_014: Apply FLATX offer resulting in zero cart value")
	void applyFlatXOfferResultingInZero() throws Exception {
		System.out.println("Testing: Apply FLATX offer resulting in zero cart value");

		// Setup: Add FLATX offer
		OfferRequest offerRequest = new OfferRequest(12, "FLATX", 200, List.of("p1"));
		addOffer(offerRequest);

		// Test: Apply offer
		ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(200, 12, 1);
		ApplyOfferResponseDTO response = applyOffer(applyRequest);

		assertThat(response.getCartValue()).isEqualTo(0); // 200 - 200 = 0
		System.out.println("✅ FLATX offer applied resulting in zero: 200 -> 0");
	}

	@Test
	@DisplayName("TC_015: Apply FLATX offer resulting in negative cart value")
	void applyFlatXOfferResultingInNegative() throws Exception {
		System.out.println("Testing: Apply FLATX offer resulting in negative cart value");

		// Setup: Add FLATX offer
		OfferRequest offerRequest = new OfferRequest(13, "FLATX", 250, List.of("p1"));
		addOffer(offerRequest);

		// Test: Apply offer
		ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(200, 13, 1);
		ApplyOfferResponseDTO response = applyOffer(applyRequest);

		assertThat(response.getCartValue()).isEqualTo(-50); // 200 - 250 = -50
		System.out.println("✅ FLATX offer applied resulting in negative: 200 -> -50");
	}

	// ============ APPLY OFFER TESTS - PERCENTAGE ============

	@Test
	@DisplayName("TC_016: Apply FLAT% offer successfully")
	void applyFlatPercentOfferSuccessfully() throws Exception {
		System.out.println("Testing: Apply FLAT% offer successfully");

		// Setup: Add FLAT% offer (any type other than "FLATX" is treated as percentage)
		OfferRequest offerRequest = new OfferRequest(14, "FLAT%", 10, List.of("p1"));
		addOffer(offerRequest);

		// Test: Apply offer
		ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(200, 14, 1);
		ApplyOfferResponseDTO response = applyOffer(applyRequest);

		// Expected: 200 - (200 * 10 * 0.01) = 200 - 20 = 180
		assertThat(response.getCartValue()).isEqualTo(180);
		System.out.println("✅ FLAT% offer applied successfully: 200 -> 180 (10% discount)");
	}

	@Test
	@DisplayName("TC_018: Apply FLAT% offer with 100% discount")
	void applyFlatPercentOfferHundredPercent() throws Exception {
		System.out.println("Testing: Apply FLAT% offer with 100% discount");

		// Setup: Add 100% offer
		OfferRequest offerRequest = new OfferRequest(16, "PERCENTAGE", 100, List.of("p1"));
		addOffer(offerRequest);

		// Test: Apply offer
		ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(200, 16, 1);
		ApplyOfferResponseDTO response = applyOffer(applyRequest);

		// Expected: 200 - (200 * 100 * 0.01) = 200 - 200 = 0
		assertThat(response.getCartValue()).isEqualTo(0);
		System.out.println("✅ 100% offer applied successfully: 200 -> 0");
	}

	@Test
	@DisplayName("TC_019: Apply FLAT% offer with decimal result")
	void applyFlatPercentOfferWithDecimalResult() throws Exception {
		System.out.println("Testing: Apply FLAT% offer with decimal result");

		// Setup: Add 15% offer
		OfferRequest offerRequest = new OfferRequest(17, "PERCENTAGE", 15, List.of("p1"));
		addOffer(offerRequest);

		// Test: Apply offer
		ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(333, 17, 1);
		ApplyOfferResponseDTO response = applyOffer(applyRequest);

		// Expected: 333 - (333 * 15 * 0.01) = 333 - 49.95 = 283.05 -> 283 (cast to int)
		assertThat(response.getCartValue()).isEqualTo(283);
		System.out.println("✅ 15% offer applied with decimal handling: 333 -> 283");
	}

	// ============ NO MATCH SCENARIOS ============
	@Test
	@DisplayName("TC_022: No offer for restaurant")
	void noOfferForRestaurant() throws Exception {
		System.out.println("Testing: No offer for restaurant");

		// Test: Apply offer for restaurant id above 100 (no offer exists)
		ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(200, new Random().nextInt(100, 200), 1);
		ApplyOfferResponseDTO response = applyOffer(applyRequest);

		assertThat(response.getCartValue()).isEqualTo(200); // No discount applied
		System.out.println("✅ No offer found for restaurant - original cart value maintained: 200");
	}

	@Test
	@DisplayName("TC_023: User segment doesn't match offer segments")
	void userSegmentDoesNotMatchOfferSegments() throws Exception {
		System.out.println("Testing: User segment doesn't match offer segments");

		// Setup: Add offer for segment p2 only
		OfferRequest offerRequest = new OfferRequest(22, "FLATX", 25, List.of("p2"));
		addOffer(offerRequest);

		// Test: Apply offer for user_id=1 (which returns segment "p1" from mock)
		ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(200, 22, 1);
		ApplyOfferResponseDTO response = applyOffer(applyRequest);

		assertThat(response.getCartValue()).isEqualTo(200); // No discount applied
		System.out.println("✅ User segment p1 doesn't match offer segment p2 - no discount applied: 200");
	}

	// ============ MULTIPLE OFFERS SCENARIOS ============
	@Test
	@DisplayName("TC_020: Multiple offers for same restaurant, user matches one segment")
	void multipleOffersUserMatchesOneSegment() throws Exception {
		System.out.println("Testing: Multiple offers for same restaurant, user matches one segment");

		// Setup: Add multiple offers for same restaurant
		OfferRequest offer1 = new OfferRequest(25, "FLATX", 10, List.of("p1"));
		OfferRequest offer2 = new OfferRequest(25, "PERCENTAGE", 20, List.of("p2"));
		addOffer(offer1);
		addOffer(offer2);

		// Test: Apply offer (user_id=1 returns segment "p1")
		ApplyOfferRequestDTO applyRequestUser1 = new ApplyOfferRequestDTO(200, 25, 1);
		ApplyOfferResponseDTO responseUser1 = applyOffer(applyRequestUser1);

		// Test: Apply offer (user_id=2 returns segment "p2")
		ApplyOfferRequestDTO applyRequestUser2 = new ApplyOfferRequestDTO(200, 25, 2);
		ApplyOfferResponseDTO responseUser2 = applyOffer(applyRequestUser2);

		// Expected: First matching offer (FLATX 10) should be applied
		assertThat(responseUser1.getCartValue()).isEqualTo(190);

		// Expected: First matching offer (PERCENTAGE 20%) should be applied
		assertThat(responseUser2.getCartValue()).isEqualTo(160);

		System.out.println("✅ First matching offer applied (FLATX): 200 -> 190");
	}

	@Test
	@DisplayName("TC_021: User segment matches multiple offers")
	void userSegmentMatchesMultipleOffers() throws Exception {
		System.out.println("Testing: User segment matches multiple offers");

		// Setup: Add multiple offers that both contain p1
		OfferRequest offer1 = new OfferRequest(26, "FLATX", 10, List.of("p1", "p2"));
		OfferRequest offer2 = new OfferRequest(26, "PERCENTAGE", 15, List.of("p1", "p3"));
		addOffer(offer1);
		addOffer(offer2);

		// Test: Apply offer (user_id=1 returns segment "p1")
		ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(200, 26, 1);
		ApplyOfferResponseDTO response = applyOffer(applyRequest);

		// Expected: First matching offer (FLATX 10) should be applied
		assertThat(response.getCartValue()).isEqualTo(190);
		System.out.println("✅ First matching offer from multiple matches applied: 200 -> 190");
	}

	// ============ EDGE CASES ============

	@Test
	@DisplayName("TC_027: Zero cart value")
	void zeroCartValue() throws Exception {
		System.out.println("Testing: Zero cart value");

		// Setup: Add FLATX offer
		OfferRequest offerRequest = new OfferRequest(27, "FLATX", 10, List.of("p1"));
		addOffer(offerRequest);

		// Test: Apply offer with zero cart value
		ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(0, 27, 1);
		ApplyOfferResponseDTO response = applyOffer(applyRequest);

		assertThat(response.getCartValue()).isEqualTo(-10); // 0 - 10 = -10
		System.out.println("✅ Zero cart value with FLATX offer: 0 -> -10");
	}

	@Test
	@DisplayName("TC_028: Very large cart value")
	void veryLargeCartValue() throws Exception {
		System.out.println("Testing: Very large cart value");

		// Setup: Add percentage offer
		OfferRequest offerRequest = new OfferRequest(28, "PERCENTAGE", 10, List.of("p1"));
		addOffer(offerRequest);

		// Test: Apply offer with large cart value
		ApplyOfferRequestDTO applyRequest = new ApplyOfferRequestDTO(999999, 28, 1);
		ApplyOfferResponseDTO response = applyOffer(applyRequest);

		// Expected: 999999 - (999999 * 10 * 0.01) = 999999 - 99999 = 899999
		assertThat(response.getCartValue()).isEqualTo(899999);
		System.out.println("✅ Large cart value with 10% discount: 999999 -> 899999");
	}

	// ============ END-TO-END INTEGRATION TESTS ============
	@Test
	@DisplayName("TC_031: Complex scenario - Multiple restaurants and segments")
	void complexScenarioMultipleRestaurantsAndSegments() throws Exception {
		System.out.println("Testing: Complex scenario - Multiple restaurants and segments");

		// Setup: Add offers for different restaurants
		OfferRequest offer1 = new OfferRequest(31, "FLATX", 20, List.of("p1"));
		OfferRequest offer2 = new OfferRequest(32, "PERCENTAGE", 15, List.of("p1"));
		OfferRequest offer3 = new OfferRequest(31, "FLATX", 30, List.of("p2")); // Same restaurant, different segment

		addOffer(offer1);
		addOffer(offer2);
		addOffer(offer3);

		// Test 1: Apply offer for restaurant 31 (should get p1 offer)
		ApplyOfferRequestDTO applyRequest1 = new ApplyOfferRequestDTO(200, 31, 1);
		ApplyOfferResponseDTO response1 = applyOffer(applyRequest1);
		assertThat(response1.getCartValue()).isEqualTo(180); // 200 - 20 = 180
		System.out.println("Restaurant 31 with p1 segment: 200 -> 180");

		// Test 2: Apply offer for restaurant 32
		ApplyOfferRequestDTO applyRequest2 = new ApplyOfferRequestDTO(200, 32, 1);
		ApplyOfferResponseDTO response2 = applyOffer(applyRequest2);
		assertThat(response2.getCartValue()).isEqualTo(170); // 200 - (200 * 0.15) = 170
		System.out.println("Restaurant 32 with 15% discount: 200 -> 170");

		System.out.println("✅ Complex scenario completed successfully");
	}


	// ============ HELPER METHODS ============

	private AddOfferApiResponseDTO addOffer(OfferRequest offerRequest) throws Exception {
		System.out.println("Adding offer: " + offerRequest);

		// Create URI object
		URI uri = URI.create(OFFER_URL);

		// Convert OfferRequest object to JSON
		String jsonBody = objectMapper.writeValueAsString(offerRequest);
		System.out.println("Request body: " + jsonBody);

		// Prepare the HTTP request
		HttpRequest request = HttpRequest.newBuilder()
				.uri(uri)  // Use URI here
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody)) // Use BodyPublishers.ofString for POST body
				.timeout(java.time.Duration.ofSeconds(5))  // Optional: Set a timeout
				.build();

		// Send the request and get the response
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		int responseCode = response.statusCode();
		System.out.println("Response code: " + responseCode);

		if (responseCode != 200) {  // HTTP_OK = 200
			throw new RuntimeException("Failed to add offer. Response code: " + responseCode);
		}

		// Read and process the response
		String responseBody = response.body();
		System.out.println("Response body: " + responseBody);

		// Convert the response body to AddOfferApiResponseDTO object
		return objectMapper.readValue(responseBody, AddOfferApiResponseDTO.class);
	}

	private ApplyOfferResponseDTO applyOffer(ApplyOfferRequestDTO applyOfferRequest) throws Exception {
		System.out.println("Applying offer: " + applyOfferRequest);

		// Create URI object
		URI uri = URI.create(APPLY_OFFER_URL);

		// Convert ApplyOfferRequest object to JSON
		String jsonBody = objectMapper.writeValueAsString(applyOfferRequest);
		System.out.println("Request body: " + jsonBody);

		// Prepare the HTTP request
		HttpRequest request = HttpRequest.newBuilder()
				.uri(uri)
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
				.timeout(java.time.Duration.ofSeconds(5))
				.build();

		// Send the request and get the response
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		int responseCode = response.statusCode();
		System.out.println("Response code: : " + responseCode);

		if (responseCode != 200) {
			throw new RuntimeException("Failed to apply offer. Response code: " + responseCode);
		}

		// Read and process the response
		String responseBody = response.body();
		System.out.println("Response body: " + responseBody);

		// Convert the response body to ApplyOfferResponseDTO object
		return objectMapper.readValue(responseBody, ApplyOfferResponseDTO.class);
	}
}