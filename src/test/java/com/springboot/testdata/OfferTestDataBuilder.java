package com.springboot.testdata;

import com.springboot.controller.OfferRequest;

import java.util.ArrayList;
import java.util.List;

public class OfferTestDataBuilder {
    private int restaurantId;
    private String offerType;
    private int offerValue;
    private List<String> segments = new ArrayList<>();

    public static OfferTestDataBuilder anOffer() {
        return new OfferTestDataBuilder();
    }

    public OfferTestDataBuilder forRestaurant(int restaurantId) {
        this.restaurantId = restaurantId;
        return this;
    }

    public OfferTestDataBuilder withFlatDiscount(int amount) {
        this.offerType = "FLATX";
        this.offerValue = amount;
        return this;
    }

    public OfferTestDataBuilder withPercentageDiscount(int percentage) {
        this.offerType = "FLAT%";
        this.offerValue = percentage;
        return this;
    }

    public OfferTestDataBuilder forSegments(String... segments) {
        this.segments = List.of(segments);
        return this;
    }

    public OfferRequest build() {
        if (restaurantId == 0) {
            restaurantId = TestDataConfig.generateUniqueRestaurantId();
        }
        return new OfferRequest(restaurantId, offerType, offerValue, segments);
    }
}
