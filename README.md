# Cart Offer Application

This is a Spring Boot application for testing cart offers functionality for Zomato's customer segmentation system.

## Project Overview

The application provides APIs to:
1. **Add offers** to restaurants for specific customer segments
2. **Apply offers** to cart based on user segments

### Customer Segments
- `p1`, `p2`, `p3` - Three customer segments with different offer eligibility

### Offer Types
- **FLATX** - Flat amount discount (e.g., ₹10 off)
- **FLAT%** - Percentage discount (e.g., 10% off)

## Prerequisites

- Java 21
- Maven 3.6 or higher
- Docker (for mock server)

## Quick Start

### 1. Start Mock Server (User Segment Service)
```bash
cd mockserver
docker-compose up -d
```

This starts the mock user segment service on `http://localhost:1080`

### 2. Build the Application

⚠️ **Important**: Build with tests skipped initially due to genuine test failures that need to be addressed.

```bash
# Build without running tests (RECOMMENDED for first build)
./mvnw clean package -DskipTests
```

**Why skip tests initially?**
- There are genuine test failures in the current test cases that need investigation
- Maven will not create the binary/JAR file if tests fail
- This allows you to build and run the application while test issues are being resolved

### 3. Run the Application
```bash
# Run the built JAR
java -jar target/CartOfferApplication-*.jar

# OR run directly with Maven
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Test the APIs

#### Add an Offer
```bash
curl -X POST http://localhost:8080/api/v1/offer \
  -H "Content-Type: application/json" \
  -d '{
    "restaurant_id": 1,
    "offer_type": "FLATX",
    "offer_value": 10,
    "customer_segment": ["p1"]
  }'
```

#### Apply Offer to Cart
```bash
curl -X POST http://localhost:8080/api/v1/cart/apply_offer \
  -H "Content-Type: application/json" \
  -d '{
    "cart_value": 200,
    "user_id": 1,
    "restaurant_id": 1
  }'
```

## Testing

### Run Tests (After Fixing Issues)

Once the test issues are resolved, you can build and test normally:

```bash
# Full build with tests (use after fixing test issues)
./mvnw clean package

# Run tests only
./mvnw test

# Run specific test class
./mvnw test -Dtest=CartOfferApplicationTests

# Run specific test method
./mvnw test -Dtest=CartOfferApplicationTests#testFlatXOfferApplication
```

### Test Categories

The test suite includes:
1. **FLATX Offer Testing** - Basic flat amount discounts
2. **Percentage Offer Testing** - Percentage-based discounts
3. **Multi-User Segment Testing** - Different user segments
4. **Restaurant Without Offers** - Edge case handling
5. **Multiple Offers Same Segment** - Business rule testing
6. **Service Failure Testing** - Production reliability scenarios
7. **Input Validation Testing** - Error handling
8. **Boundary Testing** - Edge cases and limits
9. **Business Rule Testing** - Core business logic
10. **Offer Creation Testing** - API validation

### Known Test Issues

⚠️ **Current Test Status**: Some test cases have genuine failures that need investigation:

- Service failure simulation tests may need mock server configuration updates
- Business rule tests may need implementation fixes
- Input validation tests may need enhanced error handling

**Recommendation**:
1. Build with `-DskipTests` first to get the application running
2. Investigate and fix test failures one by one
3. Once tests are stable, switch to normal build process

## API Documentation

### Add Offer API
- **URL**: `POST /api/v1/offer`
- **Request**:
  ```json
  {
    "restaurant_id": 1,
    "offer_type": "FLATX",
    "offer_value": 10,
    "customer_segment": ["p1", "p2"]
  }
  ```
- **Response**:
  ```json
  {
    "response_msg": "success"
  }
  ```

### Apply Offer API
- **URL**: `POST /api/v1/cart/apply_offer`
- **Request**:
  ```json
  {
    "cart_value": 200,
    "user_id": 1,
    "restaurant_id": 1
  }
  ```
- **Response**:
  ```json
  {
    "cart_value": 190
  }
  ```

### User Segment API (Mock)
- **URL**: `GET /api/v1/user_segment?user_id=1`
- **Response**:
  ```json
  {
    "segment": "p1"
  }
  ```

## Project Structure

```
src/
├── main/java/com/springboot/
│   ├── CartOfferApplication.java          # Main application class
│   ├── controller/
│   │   ├── AutowiredController.java       # Main REST controller
│   │   ├── OfferRequest.java             # Offer creation request DTO
│   │   ├── ApplyOfferRequest.java        # Apply offer request DTO
│   │   └── ...
│   └── service/                          # Service layer
├── test/java/com/springboot/
│   ├── CartOfferApplicationTests.java    # Main test class
│   ├── testdata/
│   │   ├── CartOfferTestDataProvider.java # Test data provider
│   │   └── TestDataConfig.java          # Test configuration
│   └── DTOs/                           # Test DTOs
└── mockserver/
    ├── docker-compose.yml              # Mock server setup
    └── initializerJson.json           # Mock responses
```

## Business Rules

1. **Offer Precedence**: First matching offer for a user segment is applied
2. **No Stacking**: Only one offer can be applied per cart
3. **Segment Matching**: Offers apply only to users in specified segments
4. **Over-discount Protection**: Cart value cannot go below zero
5. **Graceful Degradation**: System handles service failures gracefully

## Development Notes

- Application runs on port `8080`
- Mock server runs on port `1080`
- Tests run on port `8000` to avoid conflicts
- User segments are mocked in `mockserver/initializerJson.json`
- Test data is generated dynamically to avoid conflicts

## Troubleshooting

### Build Issues
```bash
# If build fails, try cleaning first
./mvnw clean

# Build without tests
./mvnw clean package -DskipTests

# Check Java version
java -version
```

### Test Issues
```bash
# Verify mock server is running
curl http://localhost:1080/api/v1/user_segment?user_id=1

# Check application logs
./mvnw spring-boot:run --debug

# Run specific failing test
./mvnw test -Dtest=CartOfferApplicationTests#testMethodName
```

### Mock Server Issues
```bash
# Restart mock server
cd mockserver
docker-compose down
docker-compose up -d

# Check mock server logs  
docker-compose logs
```