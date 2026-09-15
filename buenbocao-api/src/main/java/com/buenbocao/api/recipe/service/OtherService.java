package com.buenbocao.api.recipe.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.buenbocao.api.common.enums.RecipeStatus;
import com.buenbocao.api.common.exception.ResourceNotFoundException;
import com.buenbocao.api.recipe.dto.request.CreateRecipeRequest;
import com.buenbocao.api.recipe.dto.response.RecipeDetailResponse;
import com.buenbocao.api.recipe.entity.Category;
import com.buenbocao.api.recipe.entity.Diet;
import com.buenbocao.api.recipe.entity.Recipe;
import com.buenbocao.api.recipe.entity.Tag;
import com.buenbocao.api.recipe.repository.CategoryRepository;
import com.buenbocao.api.recipe.repository.DietRepository;
import com.buenbocao.api.recipe.repository.TagRepository;
import com.buenbocao.api.security.CustomUserDetails;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio de Otros.
 * Contiene toda la lógica de listado de Categoria, Tags, y Diets.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class OtherService {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final DietRepository dietRepository;

    /**
     * Devuelve Todas las Categorias.
     */
    @Transactional
    public List<Category> getAllCategories() {

        List<Category> categories = categoryRepository.findAll();
        return categories;
    }

    /**
     * Devuelve Todas los Tags.
     */
    @Transactional
    public List<Tag> getAllTags() {

        List<Tag> tags = tagRepository.findAll();
        return tags;
    }

    /**
     * Devuelve Todas las Dietas.
     */
    @Transactional
    public List<Diet> getAllDiets() {

        List<Diet> diets = dietRepository.findAll();
        return diets;
    }
}
