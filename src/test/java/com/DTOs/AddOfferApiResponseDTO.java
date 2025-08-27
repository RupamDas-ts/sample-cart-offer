package com.DTOs;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddOfferApiResponseDTO {
    private String responseMsg; // camelCase field

    public AddOfferApiResponseDTO(String responseMsg) {
        this.responseMsg = responseMsg;
    }
}
