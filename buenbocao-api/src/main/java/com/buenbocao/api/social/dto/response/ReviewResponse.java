package com.buenbocao.api.social.dto.response;

import com.buenbocao.api.user.dto.response.UserSummaryResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para una review.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private Long recipeId;
    private int rating;
    private String comment;
    private boolean edited;
    private UserSummaryResponse author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Media adjunta a la review (puede estar vacía). */
    @Builder.Default
    private List<ReviewMediaResponse> media = List.of();
}
