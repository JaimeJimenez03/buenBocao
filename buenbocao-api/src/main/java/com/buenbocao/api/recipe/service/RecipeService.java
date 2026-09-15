package com.buenbocao.api.recipe.service;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.enums.RecipeStatus;
import com.buenbocao.api.common.exception.BusinessException;
import com.buenbocao.api.common.exception.ResourceNotFoundException;
import com.buenbocao.api.common.exception.UnauthorizedException;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.common.util.SlugUtils;
import com.buenbocao.api.notification.service.NotificationService;
import com.buenbocao.api.recipe.dto.request.CreateRecipeRequest;
import com.buenbocao.api.recipe.dto.request.MediaRequest;
import com.buenbocao.api.recipe.dto.request.RecipeIngredientRequest;
import com.buenbocao.api.recipe.dto.request.StepRequest;
import com.buenbocao.api.recipe.dto.response.RecipeCardResponse;
import com.buenbocao.api.recipe.dto.response.RecipeDetailResponse;
import com.buenbocao.api.recipe.entity.Category;
import com.buenbocao.api.recipe.entity.Diet;
import com.buenbocao.api.recipe.entity.Ingredient;
import com.buenbocao.api.recipe.entity.Recipe;
import com.buenbocao.api.recipe.entity.RecipeIngredient;
import com.buenbocao.api.recipe.entity.RecipeMedia;
import com.buenbocao.api.recipe.entity.Step;
import com.buenbocao.api.recipe.entity.Tag;
import com.buenbocao.api.recipe.mapper.RecipeMapper;
import com.buenbocao.api.recipe.repository.CategoryRepository;
import com.buenbocao.api.recipe.repository.DietRepository;
import com.buenbocao.api.recipe.repository.IngredientRepository;
import com.buenbocao.api.recipe.repository.RecipeRepository;
import com.buenbocao.api.recipe.repository.TagRepository;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.social.repository.FavoriteRepository;
import com.buenbocao.api.social.repository.FollowRepository;
import com.buenbocao.api.social.repository.LikeRepository;
import com.buenbocao.api.social.repository.ReviewRepository;
import com.buenbocao.api.user.entity.User;
import com.buenbocao.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Servicio de recetas.
 * Contiene toda la lógica de negocio para crear, editar, eliminar y consultar
 * recetas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final DietRepository dietRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeMapper recipeMapper;
    private final RecipeEnricher recipeEnricher;
    private final NotificationService notificationService;
    private final FollowRepository followRepository;

    private final LikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;
    private final ReviewRepository reviewRepository;

    /**
     * Crea una nueva receta.
     */
    @Transactional
    public RecipeDetailResponse createRecipe(CustomUserDetails currentUser, CreateRecipeRequest request) {
        User author = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", currentUser.getId()));

        String slug = generateUniqueSlug(request.getTitle());

        Recipe recipe = Recipe.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .slug(slug)
                .difficulty(request.getDifficulty())
                .typeDish(request.getTypeDish())
                .mealTime(request.getMealTime())
                .servings(request.getServings())
                .prepTimeMinutes(request.getPrepTimeMinutes())
                .status(request.getStatus() != null ? request.getStatus() : RecipeStatus.DRAFT)
                .author(author)
                .build();

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", request.getCategoryId()));
            recipe.setCategory(category);
        }

        processIngredients(recipe, request.getIngredients());
        processSteps(recipe, request.getSteps());

        if (request.getMedia() != null) {
            processMedia(recipe, request.getMedia());
        }
        if (request.getTags() != null) {
            recipe.setTags(resolveTags(request.getTags()));
        }
        if (request.getDiets() != null) {
            recipe.setDiets(resolveDiets(request.getDiets()));
        }

        Recipe savedRecipe = recipeRepository.save(recipe);

        // Solo notificar a seguidores si se publica directamente (no borrador)
        if (savedRecipe.getStatus() == RecipeStatus.PUBLISHED) {
            List<Long> followerIds = followRepository
                    .findAllByFollowedId(author.getId())
                    .stream()
                    .map(follow -> follow.getFollower().getId())
                    .toList();

            if (!followerIds.isEmpty()) {
                notificationService.notifyRecipePublished(author.getId(), followerIds, savedRecipe.getId(),
                        savedRecipe.getTitle());
            }
        }

        return recipeMapper.toDetailResponse(savedRecipe);
    }

    /**
     * Actualiza una receta existente (solo el autor puede hacerlo).
     */
    @Transactional
    public RecipeDetailResponse updateRecipe(CustomUserDetails currentUser, Long recipeId,
            CreateRecipeRequest request) {
        Recipe recipe = findRecipeById(recipeId);
        verifyAuthor(recipe, currentUser);

        recipe.setTitle(request.getTitle());
        recipe.setDescription(request.getDescription());
        recipe.setDifficulty(request.getDifficulty());
        recipe.setTypeDish(request.getTypeDish());
        recipe.setMealTime(request.getMealTime());
        recipe.setServings(request.getServings());
        recipe.setPrepTimeMinutes(request.getPrepTimeMinutes());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", request.getCategoryId()));
            recipe.setCategory(category);
        } else {
            recipe.setCategory(null);
        }

        recipe.clearIngredients();
        processIngredients(recipe, request.getIngredients());

        recipe.clearSteps();
        processSteps(recipe, request.getSteps());

        recipe.clearMedia();
        if (request.getMedia() != null) {
            processMedia(recipe, request.getMedia());
        }

        recipe.setTags(request.getTags() != null ? resolveTags(request.getTags()) : new HashSet<>());
        recipe.setDiets(request.getDiets() != null ? resolveDiets(request.getDiets()) : new HashSet<>());

        Recipe updatedRecipe = recipeRepository.save(recipe);
        log.info("Receta actualizada: '{}'", updatedRecipe.getTitle());

        return recipeMapper.toDetailResponse(updatedRecipe);
    }

    /**
     * Elimina una receta (solo el autor o un admin).
     */
    @Transactional
    public void deleteRecipe(CustomUserDetails currentUser, Long recipeId) {
        Recipe recipe = findRecipeById(recipeId);
        verifyAuthor(recipe, currentUser);

        recipeRepository.delete(recipe);
        log.info("Receta eliminada: '{}'", recipe.getTitle());
    }

    /**
     * Obtiene el detalle de una receta por su slug.
     */
    @Transactional(readOnly = true)
    public RecipeDetailResponse getRecipeBySlug(String slug, CustomUserDetails currentUser) {
        Recipe recipe = recipeRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Receta", "slug", slug));

        if (recipe.getStatus() != RecipeStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Receta", "slug", slug);
        }

        RecipeDetailResponse detail = recipeMapper.toDetailResponse(recipe);
        enrichDetail(detail, recipe.getId(), currentUser);
        return detail;
    }

    /**
     * Obtiene el detalle de una receta por su ID.
     */
    @Transactional(readOnly = true)
    public RecipeDetailResponse getRecipeById(Long recipeId, CustomUserDetails currentUser) {
        Recipe recipe = findRecipeById(recipeId);
        RecipeDetailResponse detail = recipeMapper.toDetailResponse(recipe);
        enrichDetail(detail, recipe.getId(), currentUser);
        return detail;
    }

    /**
     * Lista recetas publicadas (feed general) con paginación.
     */
    @Transactional(readOnly = true)
    public PagedResponse<RecipeCardResponse> getPublishedRecipes(CustomUserDetails currentUser, int page, int size) {
        Pageable pageable = createPageable(page, size);
        Page<Recipe> recipesPage = recipeRepository.findByStatus(RecipeStatus.PUBLISHED, pageable);
        Long userId = currentUser != null ? currentUser.getId() : null;

        return toCardPagedResponse(recipesPage, userId);
    }

    /**
     * Lista recetas publicadas de un usuario.
     */
    @Transactional(readOnly = true)
    public PagedResponse<RecipeCardResponse> getRecipesByUser(CustomUserDetails currentUser, Long userId, int page,
            int size) {
        Pageable pageable = createPageable(page, size);
        Page<Recipe> recipesPage = recipeRepository.findByAuthorIdAndStatus(userId, RecipeStatus.PUBLISHED, pageable);
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        return toCardPagedResponse(recipesPage, currentUserId);
    }

    /**
     * Lista mis recetas (incluye borradores y archivadas).
     */
    @Transactional(readOnly = true)
    public PagedResponse<RecipeCardResponse> getMyRecipes(CustomUserDetails currentUser, int page, int size) {
        Pageable pageable = createPageable(page, size);
        Page<Recipe> recipesPage = recipeRepository.findByAuthorId(currentUser.getId(), pageable);
        return toCardPagedResponse(recipesPage, currentUser.getId());
    }

    /**
     * Busca recetas por texto en título o descripción.
     */
    @Transactional(readOnly = true)
    public PagedResponse<RecipeCardResponse> searchRecipes(CustomUserDetails currentUser, String query, int page,
            int size) {
        Pageable pageable = createPageable(page, size);
        Page<Recipe> recipesPage = recipeRepository.searchByTitleOrDescription(query, RecipeStatus.PUBLISHED, pageable);
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        return toCardPagedResponse(recipesPage, currentUserId);
    }

    /**
     * Lista recetas por categoría.
     */
    @Transactional(readOnly = true)
    public PagedResponse<RecipeCardResponse> getRecipesByCategory(CustomUserDetails currentUser, Long categoryId,
            int page, int size) {
        Pageable pageable = createPageable(page, size);
        Page<Recipe> recipesPage = recipeRepository.findByCategoryIdAndStatus(categoryId, RecipeStatus.PUBLISHED,
                pageable);
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        return toCardPagedResponse(recipesPage, currentUserId);
    }

    /**
     * Lista recetas que contienen un ingrediente específico.
     */
    @Transactional(readOnly = true)
    public PagedResponse<RecipeCardResponse> getRecipesByIngredient(CustomUserDetails currentUser, Long ingredientId,
            int page, int size) {
        Pageable pageable = createPageable(page, size);
        Page<Recipe> recipesPage = recipeRepository.findByIngredientIdAndStatus(ingredientId, RecipeStatus.PUBLISHED,
                pageable);
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        return toCardPagedResponse(recipesPage, currentUserId);
    }

    /**
     * Lista recetas por tag.
     */
    @Transactional(readOnly = true)
    public PagedResponse<RecipeCardResponse> getRecipesByTag(CustomUserDetails currentUser, Long tagId, int page,
            int size) {
        Pageable pageable = createPageable(page, size);
        Page<Recipe> recipesPage = recipeRepository.findByTagIdAndStatus(tagId, RecipeStatus.PUBLISHED, pageable);
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        return toCardPagedResponse(recipesPage, currentUserId);
    }

    /**
     * Lista recetas por dieta.
     */
    @Transactional(readOnly = true)
    public PagedResponse<RecipeCardResponse> getRecipesByDiet(CustomUserDetails currentUser, Long dietId, int page,
            int size) {
        Pageable pageable = createPageable(page, size);
        Page<Recipe> recipesPage = recipeRepository.findByDietIdAndStatus(dietId, RecipeStatus.PUBLISHED, pageable);
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        return toCardPagedResponse(recipesPage, currentUserId);
    }

    /**
     * Cambia el estado de una receta (publicar, archivar, volver a borrador).
     */
    @Transactional
    public RecipeDetailResponse changeStatus(CustomUserDetails currentUser, Long recipeId, RecipeStatus newStatus) {
        Recipe recipe = findRecipeById(recipeId);
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            verifyAuthor(recipe, currentUser);
            if (recipe.getStatus() == RecipeStatus.LOCKED) {
                throw new com.buenbocao.api.common.exception.BusinessException(
                        "Esta receta ha sido bloqueada por moderacion. No puedes cambiar su estado.");
            }
        }
        RecipeStatus previousStatus = recipe.getStatus();
        recipe.setStatus(newStatus);
        Recipe updatedRecipe = recipeRepository.save(recipe);

        // Notificar al autor si un admin desbloquea su receta
        if (isAdmin && previousStatus == RecipeStatus.LOCKED) {
            notificationService.notifySystem(
                    recipe.getAuthor().getId(),
                    "Receta desbloqueada",
                    "Tu receta \"" + recipe.getTitle()
                            + "\" ha sido desbloqueada por moderacion y vuelve a estar disponible.");
        }

        log.info("Estado de receta '{}' cambiado a: {} (admin: {})", recipe.getTitle(), newStatus, isAdmin);
        return recipeMapper.toDetailResponse(updatedRecipe);
    }

    @Transactional
    public RecipeDetailResponse adminLockRecipe(CustomUserDetails admin, Long recipeId, String reason) {
        Recipe recipe = findRecipeById(recipeId);
        recipe.setStatus(RecipeStatus.LOCKED);
        recipeRepository.save(recipe);
        notificationService.notifySystem(recipe.getAuthor().getId(), "Receta bloqueada por moderación",
                "Tu receta \"" + recipe.getTitle() + "\" ha sido bloqueada por moderación." +
                        (reason != null && !reason.isBlank() ? " Motivo: " + reason : "") +
                        " Puedes solicitar una revisión desde la sección de tus recetas.");
        log.info("Receta '{}' bloqueada por admin {}", recipe.getTitle(), admin.getUsername());
        return recipeMapper.toDetailResponse(recipe);
    }

    /**
     * Busca recetas usando filtros de categoría, tags y dietas.
     */
    @Transactional(readOnly = true)
    public PagedResponse<RecipeCardResponse> searchRecipesWithFilters(
            CustomUserDetails currentUser,
            String query,
            List<Long> categoryIds,
            List<Long> tagIds,
            List<Long> dietIds,
            int page,
            int size) {

        Pageable pageable = createPageable(page, size);

        if (categoryIds != null && categoryIds.isEmpty())
            categoryIds = null;
        if (tagIds != null && tagIds.isEmpty())
            tagIds = null;
        if (dietIds != null && dietIds.isEmpty())
            dietIds = null;

        Page<Recipe> recipesPage;

        if (query != null && !query.isBlank()) {
            recipesPage = recipeRepository.searchByTitleOrDescription(
                    query,
                    RecipeStatus.PUBLISHED,
                    pageable);
        } else {
            recipesPage = recipeRepository.searchWithFilters(
                    categoryIds,
                    tagIds,
                    dietIds,
                    RecipeStatus.PUBLISHED,
                    pageable);
        }

        Long currentUserId = currentUser != null ? currentUser.getId() : null;

        return toCardPagedResponse(recipesPage, currentUserId);
    }

    // ====================================================================================
    // MÉTODOS PRIVADOS
    // ====================================================================================

    private Recipe findRecipeById(Long id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receta", "id", id));
    }

    private void verifyAuthor(Recipe recipe, CustomUserDetails currentUser) {
        if (!recipe.getAuthor().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("No eres el autor de esta receta");
        }
    }

    private String generateUniqueSlug(String title) {
        String slug = SlugUtils.toSlug(title);
        if (recipeRepository.existsBySlug(slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 6);
        }
        return slug;
    }

    private void processIngredients(Recipe recipe, List<RecipeIngredientRequest> ingredientRequests) {
        for (int i = 0; i < ingredientRequests.size(); i++) {
            RecipeIngredientRequest req = ingredientRequests.get(i);

            Ingredient ingredient = ingredientRepository.findByNameIgnoreCase(req.getIngredientName())
                    .orElseGet(() -> ingredientRepository.save(
                            Ingredient.builder()
                                    .name(req.getIngredientName())
                                    .slug(SlugUtils.toSlug(req.getIngredientName()))
                                    .build()));

            RecipeIngredient recipeIngredient = RecipeIngredient.builder()
                    .ingredient(ingredient)
                    .quantity(req.getQuantity())
                    .unit(req.getUnit())
                    .notes(req.getNotes())
                    .sortOrder(i)
                    .build();

            recipe.addIngredient(recipeIngredient);
        }
    }

    private void processSteps(Recipe recipe, List<StepRequest> stepRequests) {
        for (int i = 0; i < stepRequests.size(); i++) {
            StepRequest req = stepRequests.get(i);

            Step step = Step.builder()
                    .stepNumber(i + 1)
                    .description(req.getDescription())
                    .imageUrl(req.getImageUrl())
                    .build();

            recipe.addStep(step);
        }
    }

    private void processMedia(Recipe recipe, List<MediaRequest> mediaRequests) {
        for (int i = 0; i < mediaRequests.size(); i++) {
            MediaRequest req = mediaRequests.get(i);

            // La portada (sortOrder=0) debe ser siempre una imagen
            if (i == 0 && req.getMediaType() == RecipeMedia.MediaType.VIDEO) {
                throw new BusinessException("La primera imagen de la receta (portada) no puede ser un vídeo.");
            }

            RecipeMedia media = RecipeMedia.builder()
                    .mediaUrl(req.getMediaUrl())
                    .mediaType(req.getMediaType())
                    .sortOrder(i)
                    .build();

            recipe.addMedia(media);
        }
    }

    private Set<Tag> resolveTags(Set<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        for (String name : tagNames) {
            Tag tag = tagRepository.findByNameIgnoreCase(name.trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Tag", "nombre", name.trim()));
            tags.add(tag);
        }
        return tags;
    }

    private Set<Diet> resolveDiets(Set<String> dietNames) {
        Set<Diet> diets = new HashSet<>();
        for (String name : dietNames) {
            Diet diet = dietRepository.findByNameIgnoreCase(name.trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Dieta", "nombre", name.trim()));
            diets.add(diet);
        }
        return diets;
    }

    private Pageable createPageable(int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, AppConstants.DEFAULT_SORT_BY));
    }

    private void enrichDetail(RecipeDetailResponse detail, Long recipeId, CustomUserDetails currentUser) {
        detail.setLikesCount(likeRepository.countByRecipeId(recipeId));
        detail.setCommentsCount(reviewRepository.countByRecipeId(recipeId));

        if (currentUser != null) {
            detail.setLikedByCurrentUser(
                    likeRepository.existsByUserIdAndRecipeId(currentUser.getId(), recipeId));
            detail.setSavedByCurrentUser(
                    favoriteRepository.existsByUserIdAndRecipeId(currentUser.getId(), recipeId));
        }
    }

    private PagedResponse<RecipeCardResponse> toCardPagedResponse(Page<Recipe> recipesPage, Long currentUserId) {
        List<RecipeCardResponse> content = recipesPage.getContent().stream()
                .map(recipeMapper::toCardResponse)
                .toList();
        recipeEnricher.enrichAll(content, currentUserId);

        return PagedResponse.of(recipesPage, content);
    }

    private PagedResponse<RecipeCardResponse> toCardPagedResponse(Page<Recipe> recipesPage) {
        return toCardPagedResponse(recipesPage, null);
    }
}