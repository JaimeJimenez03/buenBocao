package com.buenbocao.api.recipe.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buenbocao.api.common.response.ApiResponse;
import com.buenbocao.api.recipe.entity.Category;
import com.buenbocao.api.recipe.entity.Diet;
import com.buenbocao.api.recipe.entity.Tag;
import com.buenbocao.api.recipe.service.OtherService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/other")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Otros", description = "Gestión y consulta de Categorias, Tags y Diets")
public class OtherController {

    private final OtherService otherService;

    // ==================== CONSULTAS ====================

    @Operation(summary = "Obtener lista de Categorias")
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        List<Category> categories = otherService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.ok(categories));
    }

    @Operation(summary = "Obtener lista de Tags")
    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<Tag>>> getAllTags() {
        List<Tag> tags = otherService.getAllTags();
        return ResponseEntity.ok(ApiResponse.ok(tags));
    }

    @Operation(summary = "Obtener lista de Diets")
    @GetMapping("/diets")
    public ResponseEntity<ApiResponse<List<Diet>>> getAllDiets() {
        List<Diet> diets = otherService.getAllDiets();
        return ResponseEntity.ok(ApiResponse.ok(diets));
    }
}