# Cart Offer Test Cases - Summary Documentation

## Overview
**Project**: Zomato Cart Offer System  
**Test Framework**: JUnit 5 with Parameterized Tests  
**Total Test Methods**: 11  
**Total Test Scenarios**: ~42  
**Focus**: Real-world user scenarios and production reliability

---

## Test Suite Architecture

### Test Categories
1. **Happy Path Testing** - Core functionality validation
2. **Error Handling** - Edge cases and failure scenarios
3. **Business Rules** - Complex business logic validation
4. **Production Reliability** - Service failure and timeout scenarios
5. **Input Validation** - Boundary and invalid input testing

### Priority Classification
- **CRITICAL** (15 scenarios) - Core business functionality, must pass
- **HIGH** (20 scenarios) - Important edge cases and error handling
- **MEDIUM** (7 scenarios) - Nice-to-have validations and edge cases

---

## Detailed Test Case Breakdown

## 1. FLATX Offer Testing
**Method**: `testFlatXOfferApplication()`  
**Scenarios**: 6 test cases

| Test Case | Input | Expected | Description | Priority |
|-----------|--------|----------|-------------|----------|
| Basic discount | Cart: 200, Discount: 10 | 190 | Standard flat amount discount | HIGH |
| Large cart | Cart: 1000, Discount: 100 | 900 | High-value order scenario | MEDIUM |
| Zero discount | Cart: 10, Discount: 0 | 10 | Edge case: no discount applied | HIGH |
| Full discount | Cart: 50, Discount: 50 | 0 | Cart becomes zero | HIGH |
| Over-discount | Cart: 30, Discount: 40 | 0 | Negative protection rule | CRITICAL |
| Standard case | Cart: 500, Discount: 50 | 450 | Typical usage scenario | MEDIUM |

**Business Rules Validated**:
- Cart value never goes negative
- Flat amount subtraction logic
- Zero discount handling

---

## 2. Percentage Offer Testing
**Method**: `testPercentageOfferApplication()`  
**Scenarios**: 6 test cases

| Test Case | Input | Expected | Description | Priority |
|-----------|--------|----------|-------------|----------|
| Standard 10% | Cart: 200, Discount: 10% | 180 | Common percentage discount | HIGH |
| Standard 20% | Cart: 500, Discount: 20% | 400 | Higher percentage scenario | MEDIUM |
| Zero percent | Cart: 200, Discount: 0% | 200 | No discount edge case | HIGH |
| Full discount | Cart: 100, Discount: 100% | 0 | Free order scenario | HIGH |
| Rounding case 1 | Cart: 199, Discount: 33% | 133 | Decimal rounding validation | HIGH |
| Rounding case 2 | Cart: 101, Discount: 50% | 50 | Integer rounding edge case | HIGH |

**Business Rules Validated**:
- Percentage calculation accuracy
- Rounding behavior for decimal results
- Full discount (100%) handling

---

## 3. Multi-User Segment Testing
**Method**: `testMultiUserSegmentScenarios()`  
**Scenarios**: 3 test cases

| User Segment | Input | Expected | Description | Priority |
|--------------|--------|----------|-------------|----------|
| p1 user | User: 1, Cart: 200 | 190 | FLATX offer application | HIGH |
| p2 user | User: 2, Cart: 200 | 160 | Percentage offer application | HIGH |
| p3 user | User: 3, Cart: 200 | 200 | No matching offer available | HIGH |

**Business Rules Validated**:
- Segment-based offer matching
- Different offer types for different segments
- No offer fallback behavior

---

## 4. Restaurant Without Offers
**Method**: `testRestaurantWithoutOffers()`  
**Scenarios**: 4 test cases

| Scenario | Input | Expected | Description | Priority |
|----------|--------|----------|-------------|----------|
| New restaurant p1 | Restaurant: 999, User: 1 | No discount | Fresh restaurant, p1 user | CRITICAL |
| New restaurant p2 | Restaurant: 999, User: 2 | No discount | Fresh restaurant, p2 user | CRITICAL |
| New restaurant p3 | Restaurant: 999, User: 3 | No discount | Fresh restaurant, p3 user | CRITICAL |
| No offer history | Restaurant: 1001, User: 1 | No discount | Restaurant never configured offers | HIGH |

**Real-World Scenario**: New restaurants joining platform without offers configured yet.

**Business Rules Validated**:
- Graceful handling when no offers exist
- System doesn't crash on missing data
- Cart value remains unchanged

---

## 5. Multiple Offers Same Segment
**Method**: `testMultipleOffersSameSegment()`  
**Scenarios**: 3 test cases

| Scenario | Setup | Expected | Description | Priority |
|----------|--------|----------|-------------|----------|
| Multiple FLATX | Two FLATX offers (10, 20) for p1 | First wins (10) | Business rule: first match wins | CRITICAL |
| Mixed offers | FLATX (10) + Percentage (15%) for p1 | FLATX wins (10) | Offer type precedence | CRITICAL |
| Multiple % | Two percentage offers (10%, 25%) for p2 | First wins (10%) | First match rule validation | CRITICAL |

**Real-World Scenario**: Restaurant creates multiple promotions for same customer segment.

**Business Rules Validated**:
- First matching offer wins (no stacking)
- Offer creation order matters
- System doesn't apply multiple discounts

---

## 6. User Segment Service Failure
**Method**: `testUserSegmentServiceFailure()`  
**Scenarios**: 3 test cases

| Failure Type | Mock Setup | Expected Behavior | Description | Priority |
|--------------|------------|-------------------|-------------|----------|
| User not found | User ID: 404 → 404 response | Graceful degradation | User doesn't exist in system | CRITICAL |
| Service error | User ID: 500 → 500 response | Error handling | Internal service failure | CRITICAL |
| Service timeout | User ID: 999 → 5s delay + 503 | Timeout handling | Network/performance issues | CRITICAL |

**Real-World Scenario**: User segment microservice experiences downtime or performance issues.

**Business Rules Validated**:
- System resilience to dependency failures
- Graceful degradation strategies
- No cart functionality blocking on segment service

---

## 7. Input Validation Testing
**Method**: `testInputValidation()`  
**Scenarios**: 3 test cases

| Input Type | Test Data | Expected Behavior | Description | Priority |
|------------|-----------|-------------------|-------------|----------|
| Zero cart | Cart: 0, User: 1 | Handle gracefully | Empty cart edge case | HIGH |
| Negative cart | Cart: -100, User: 1 | Handle gracefully | Invalid negative cart | HIGH |
| Large cart | Cart: 999999, User: 1 | Process normally | High-value order boundary | MEDIUM |

**Real-World Scenario**: Invalid or edge-case input from frontend applications.

**Business Rules Validated**:
- Input parameter validation
- System boundary handling
- Error response consistency

---

## 8. Invalid Scenarios
**Method**: `testInvalidScenarios()`  
**Scenarios**: 5 test cases

| Invalid Input | Test Data | Expected | Description | Priority |
|---------------|-----------|----------|-------------|----------|
| Bad restaurant | Restaurant: 999 | No discount | Non-existent restaurant ID | HIGH |
| Zero restaurant | Restaurant: 0 | No discount | Invalid zero ID | MEDIUM |
| Negative restaurant | Restaurant: -1 | No discount | Invalid negative ID | MEDIUM |
| Zero user | User: 0 | No discount | Invalid zero user ID | HIGH |
| Negative user | User: -1 | No discount | Invalid negative user ID | HIGH |

**Business Rules Validated**:
- Invalid ID handling
- System doesn't crash on bad input
- Consistent error behavior

---

## 9. Cart Boundary Testing
**Method**: `testCartBoundaryScenarios()`  
**Scenarios**: 5 test cases

| Boundary Case | Input | Expected | Description | Priority |
|---------------|--------|----------|-------------|----------|
| Exact discount | Cart: 50, Discount: 50 | 0 | Perfect discount match | HIGH |
| Over discount | Cart: 30, Discount: 40 | 0 | Discount exceeds cart | CRITICAL |
| Minimal cart | Cart: 1, Discount: 5 | 0 | Small cart, large discount | HIGH |
| Zero cart | Cart: 0, Discount: 10 | 0 | Empty cart handling | HIGH |
| Negative cart | Cart: -5, Discount: 10 | 0 | Invalid negative cart | MEDIUM |

**Business Rules Validated**:
- Mathematical boundary conditions
- Negative value protection
- Zero handling consistency

---

## 10. Business Rule Testing
**Method**: `testBusinessRules()`  
**Scenarios**: 5 test cases

| Business Rule | Scenario Setup | Expected Outcome | Description | Priority |
|---------------|----------------|------------------|-------------|----------|
| First match wins | Multiple offers for p1 | First offer applied | Precedence rule validation | CRITICAL |
| Segment priority | Different offers per segment | Correct segment offer | Segment matching logic | HIGH |
| No stacking | Single offer setup | One discount only | Anti-stacking rule | CRITICAL |
| Discount cap | High percentage offer | Cart ≥ 0 | Negative protection | HIGH |
| Offer precedence | FLATX vs Percentage | First created wins | Type precedence rule | CRITICAL |

**Business Rules Validated**:
- Core business logic integrity
- Offer conflict resolution
- System consistency rules

---

## 11. Offer Creation Testing 
**Method**: `testOfferCreation()`  
**Scenarios**: 11 test cases

| Creation Scenario | Input | Expected | Description | Priority |
|-------------------|--------|----------|-------------|----------|
| Valid FLATX | Standard FLATX offer | Success | Happy path creation | HIGH |
| Valid percentage | Standard percentage offer | Success | Happy path creation | HIGH |
| Multi-segment | Offer for [p1,p2] | Success | Multiple segment targeting | HIGH |
| Zero discount | 0 amount/percentage | Success | Edge case: no discount | MEDIUM |
| Full discount | 100% discount | Success | Maximum discount case | HIGH |
| **Case sensitivity** | "flatx" (lowercase) | Error | Case sensitivity validation | HIGH |
| **Mixed case** | "FlatX" (mixed) | Error | Case sensitivity validation | HIGH |
| Negative value | Negative discount | Error | Invalid input validation | HIGH |
| Over 100% | 110% discount | Error | Percentage limit validation | HIGH |
| Invalid type | "INVALID_TYPE" | Error | Type validation | MEDIUM |
| Empty segments | Empty segment list | Error | Required field validation | MEDIUM |

**Business Rules Validated**:
- Offer creation input validation
- Case sensitivity requirements
- Business rule enforcement at creation

---

## Test Execution Strategy

### **Critical Path (Must Pass)**
These tests validate core business functionality:
1. Basic FLATX and Percentage offers
2. Over-discount protection
3. Restaurant without offers
4. Multiple offers precedence
5. Service failure handling

### **Execution Priority Order**
1. **CRITICAL** (15 scenarios) - Core business functionality
2. **HIGH** (20 scenarios) - Important edge cases
3. **MEDIUM** (7 scenarios) - Nice-to-have validations

### **Real-World Scenarios Covered**
- ✅ New restaurants without offers configured
- ✅ Microservice downtime/failures
- ✅ Multiple promotional offers conflict resolution
- ✅ Input validation and error handling
- ✅ Percentage calculation rounding edge cases
- ✅ API case sensitivity issues
- ✅ Large order value processing
- ✅ Business rule enforcement

---

## Production Readiness Validation

### **Reliability Testing**
- Service failure graceful degradation
- Timeout handling with actual delays
- Error response consistency
- Input validation completeness

### **Business Logic Integrity**
- Mathematical accuracy (rounding, negatives)
- Offer precedence rules
- Segment-based targeting
- Anti-fraud protection (no negative carts)

### **User Experience**
- Consistent behavior across scenarios
- Clear error messages
- Performance under edge conditions
- Graceful handling of invalid inputs

---

## Mock Service Configuration

### **User Segment Service Endpoints**
- `user_id=1` → `{"segment": "p1"}` (200 OK)
- `user_id=2` → `{"segment": "p2"}` (200 OK)
- `user_id=3` → `{"segment": "p3"}` (200 OK)
- `user_id=404` → User not found (404 Error)
- `user_id=500` → Internal error (500 Error)
- `user_id=999` → Service timeout (503 Error with 5s delay)
- `user_id=0` → Invalid ID (400 Error)
- `user_id=-1` → Invalid negative ID (400 Error)
- `user_id=4` → User exists but no segment (422 Error)

---

## Key Business Rules Validated

1. **Cart Protection**: Cart value never goes negative
2. **Offer Precedence**: First matching offer wins (no stacking)
3. **Segment Targeting**: Offers apply only to designated user segments
4. **Service Resilience**: System handles dependency failures gracefully
5. **Input Validation**: All API parameters validated for edge cases
6. **Mathematical Accuracy**: Percentage calculations handle rounding correctly
7. **Case Sensitivity**: Offer types are case-sensitive
8. **Business Logic**: Over-discounts capped at cart value

---