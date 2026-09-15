package com.buenbocao.api.social.dto.response;

import lombok.*;

/**
 * DTO de respuesta para un elemento de media de una review.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewMediaResponse {

    private Long id;
    private String mediaUrl;
    private String mediaType; // "IMAGE" | "VIDEO"
    private int sortOrder;
}
