package com.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApplyOfferRequestDTO {
    private int cart_value;
    private int restaurant_id;
    private int user_id;
}
