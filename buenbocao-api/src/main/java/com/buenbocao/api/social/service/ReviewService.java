package com.buenbocao.api.social.service;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.exception.BusinessException;
import com.buenbocao.api.common.exception.ResourceNotFoundException;
import com.buenbocao.api.common.exception.UnauthorizedException;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.recipe.entity.Recipe;
import com.buenbocao.api.recipe.repository.RecipeRepository;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.social.dto.request.CreateReviewRequest;
import com.buenbocao.api.social.dto.response.RecipeStatsResponse;
import com.buenbocao.api.social.dto.response.ReviewResponse;
import com.buenbocao.api.social.entity.Favorite;
import com.buenbocao.api.social.entity.Review;
import com.buenbocao.api.social.entity.ReviewMedia;
import com.buenbocao.api.social.mapper.SocialMapper;
import com.buenbocao.api.social.repository.FavoriteRepository;
import com.buenbocao.api.social.repository.ReviewRepository;
import com.buenbocao.api.storage.service.StorageService;
import com.buenbocao.api.user.entity.User;
import com.buenbocao.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de reviews.
 * Permite crear, actualizar, eliminar y consultar reviews de recetas.
 * Las reviews pueden incluir media adjunta (imágenes / vídeos).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int MAX_MEDIA_PER_REVIEW = 5;

    private final com.buenbocao.api.notification.service.NotificationService notificationService;
    private final ReviewRepository reviewRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final SocialMapper socialMapper;
    private final StorageService storageService;

    /**
     * Crea una review para una receta.
     * Las rutas de media (ya subidas vía /storage/review-media) se reciben en el
     * request.
     */
    @Transactional
    public ReviewResponse createReview(CustomUserDetails currentUser, Long recipeId, CreateReviewRequest request) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Receta", "id", recipeId));

        if (recipe.getAuthor().getId().equals(currentUser.getId())) {
            throw new BusinessException("No puedes hacer una review de tu propia receta");
        }

        if (reviewRepository.existsByUserIdAndRecipeId(currentUser.getId(), recipeId)) {
            throw new BusinessException("Ya has dejado una review en esta receta. Puedes editarla.");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", currentUser.getId()));

        Review review = Review.builder()
                .user(user)
                .recipe(recipe)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        // Añadir media si viene en el request
        if (request.getMedia() != null && !request.getMedia().isEmpty()) {
            if (request.getMedia().size() > MAX_MEDIA_PER_REVIEW) {
                throw new BusinessException("Máximo " + MAX_MEDIA_PER_REVIEW + " archivos por review");
            }
            for (int i = 0; i < request.getMedia().size(); i++) {
                CreateReviewRequest.MediaItem item = request.getMedia().get(i);
                ReviewMedia media = ReviewMedia.builder()
                        .review(review)
                        .mediaUrl(item.getMediaUrl())
                        .mediaType(ReviewMedia.MediaType.valueOf(item.getMediaType()))
                        .sortOrder(i)
                        .build();
                review.getMedia().add(media);
            }
        }

        Review savedReview = reviewRepository.save(review);

        notificationService.notifyNewReviewToAuthor(
                user.getId(), recipe.getAuthor().getId(), recipe.getId(), recipe.getTitle());

        return socialMapper.toReviewResponse(savedReview);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getMyReviews(CustomUserDetails currentUser, int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);

        Page<Review> reviewsPage = reviewRepository
                .findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable);

        List<ReviewResponse> content = reviewsPage.getContent()
                .stream()
                .map(socialMapper::toReviewResponse)
                .toList();

        return PagedResponse.of(reviewsPage, content);
    }

    /**
     * Actualiza la review del usuario.
     * El rating no cambia. Se reemplaza la lista de media por la nueva.
     */
    @Transactional
    public ReviewResponse updateReview(CustomUserDetails currentUser, Long reviewId, CreateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("No puedes editar esta review");
        }

        review.setComment(request.getComment());
        review.setEdited(true);

        // Reemplazar media: borrar física la que ya no está y limpiar la colección
        List<String> newUrls = request.getMedia() == null
                ? List.of()
                : request.getMedia().stream().map(CreateReviewRequest.MediaItem::getMediaUrl).toList();

        review.getMedia().stream()
                .filter(m -> !newUrls.contains(m.getMediaUrl()))
                .forEach(m -> storageService.delete(m.getMediaUrl()));

        review.getMedia().clear();

        if (request.getMedia() != null && !request.getMedia().isEmpty()) {
            if (request.getMedia().size() > MAX_MEDIA_PER_REVIEW) {
                throw new BusinessException("Máximo " + MAX_MEDIA_PER_REVIEW + " archivos por review");
            }
            for (int i = 0; i < request.getMedia().size(); i++) {
                CreateReviewRequest.MediaItem item = request.getMedia().get(i);
                review.getMedia().add(ReviewMedia.builder()
                        .review(review)
                        .mediaUrl(item.getMediaUrl())
                        .mediaType(ReviewMedia.MediaType.valueOf(item.getMediaType()))
                        .sortOrder(i)
                        .build());
            }
        }

        return socialMapper.toReviewResponse(reviewRepository.save(review));
    }

    /**
     * Elimina la review y borra físicamente su media del storage.
     */
    @Transactional
    public void deleteReview(CustomUserDetails currentUser, Long recipeId) {
        Review review = reviewRepository.findByUserIdAndRecipeId(currentUser.getId(), recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "recipeId", recipeId));

        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("No puedes eliminar esta review");
        }

        review.getMedia().forEach(m -> {
            try {
                storageService.delete(m.getMediaUrl());
            } catch (Exception e) {
                log.warn("No se pudo borrar el archivo {}: {}", m.getMediaUrl(), e.getMessage());
            }
        });

        reviewRepository.delete(review);
        log.info("Review eliminada en receta {} por usuario {}", recipeId, currentUser.getUsername());
    }

    @Transactional
    public void deleteReviewById(CustomUserDetails currentUser, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "reviewId", reviewId));

        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("No puedes eliminar esta review");
        }

        review.getMedia().forEach(m -> {
            try {
                storageService.delete(m.getMediaUrl());
            } catch (Exception e) {
                log.warn("No se pudo borrar el archivo {}: {}", m.getMediaUrl(), e.getMessage());
            }
        });

        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getReviewsByRecipe(Long recipeId, int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);

        Page<Review> reviewsPage = reviewRepository.findByRecipeIdOrderByCreatedAtDesc(recipeId, pageable);

        List<ReviewResponse> content = reviewsPage.getContent().stream()
                .map(socialMapper::toReviewResponse)
                .toList();

        return PagedResponse.of(reviewsPage, content);
    }

    @Transactional(readOnly = true)
    public RecipeStatsResponse getRecipeStats(Long recipeId, Long currentUserId) {
        if (!recipeRepository.existsById(recipeId)) {
            throw new ResourceNotFoundException("Receta", "id", recipeId);
        }

        double averageRating = reviewRepository.getAverageRatingByRecipeId(recipeId);
        long reviewsCount = reviewRepository.countByRecipeId(recipeId);
        long favoritesCount = favoriteRepository.countByRecipeId(recipeId);

        boolean favoritedByCurrentUser = false;
        boolean reviewedByCurrentUser = false;

        if (currentUserId != null) {
            favoritedByCurrentUser = favoriteRepository.existsByUserIdAndRecipeId(currentUserId, recipeId);
            reviewedByCurrentUser = reviewRepository.existsByUserIdAndRecipeId(currentUserId, recipeId);
        }

        return RecipeStatsResponse.builder()
                .recipeId(recipeId)
                .averageRating(Math.round(averageRating * 10.0) / 10.0)
                .reviewsCount(reviewsCount)
                .favoritesCount(favoritesCount)
                .favoritedByCurrentUser(favoritedByCurrentUser)
                .reviewedByCurrentUser(reviewedByCurrentUser)
                .build();
    }
}
