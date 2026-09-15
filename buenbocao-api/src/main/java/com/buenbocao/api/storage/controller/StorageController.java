package com.buenbocao.api.storage.controller;

import com.buenbocao.api.common.response.ApiResponse;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.storage.service.StorageService;
import com.buenbocao.api.user.dto.response.UserProfileResponse;
import com.buenbocao.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Controller de almacenamiento.
 * Endpoints para subir archivos y servir imágenes/vídeos.
 */
@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
@Tag(name = "Almacenamiento", description = "Subida y acceso a archivos")
public class StorageController {

    private final StorageService storageService;
    private final UserService userService;

    @Operation(summary = "Subir imagen de perfil")
    @PostMapping("/profile-image")
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadProfileImage(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam("file") MultipartFile file) {

        String relativePath = storageService.store(file, "profiles");
        UserProfileResponse profile = userService.updateProfileImage(currentUser, relativePath);
        return ResponseEntity.ok(ApiResponse.ok("Foto de perfil actualizada", profile));
    }

    @Operation(summary = "Subir imagen de receta (solo imágenes, usada también para portada)")
    @PostMapping("/recipe-image")
    public ResponseEntity<ApiResponse<String>> uploadRecipeImage(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam("file") MultipartFile file) {

        // Este endpoint se usa para imágenes puras (portada y pasos).
        // Rechazamos explícitamente vídeos.
        if (storageService.isVideo(file)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("La portada y las imágenes de pasos no pueden ser vídeos. " +
                            "Usa /storage/recipe-media para vídeos adicionales."));
        }
        String relativePath = storageService.store(file, "recipes");
        return ResponseEntity.ok(ApiResponse.ok("Imagen subida", relativePath));
    }

    @Operation(summary = "Subir imagen o vídeo de receta")
    @PostMapping("/recipe-media")
    public ResponseEntity<ApiResponse<ReviewMediaUploadResponse>> uploadRecipeMedia(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam("file") MultipartFile file) {

        String relativePath = storageService.store(file, "recipes");
        String mediaType = storageService.isVideo(file) ? "VIDEO" : "IMAGE";
        return ResponseEntity.ok(ApiResponse.ok("Media subida",
                new ReviewMediaUploadResponse(relativePath, mediaType)));
    }

    /**
     * Sube un archivo (imagen o vídeo) para adjuntarlo a una review.
     * Devuelve la ruta relativa y el tipo detectado.
     */
    @Operation(summary = "Subir media para una review (imagen o vídeo)")
    @PostMapping("/review-media")
    public ResponseEntity<ApiResponse<ReviewMediaUploadResponse>> uploadReviewMedia(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam("file") MultipartFile file) {

        String relativePath = storageService.store(file, "reviews");
        String mediaType = storageService.isVideo(file) ? "VIDEO" : "IMAGE";
        return ResponseEntity.ok(ApiResponse.ok("Media subida",
                new ReviewMediaUploadResponse(relativePath, mediaType)));
    }

    @Operation(summary = "Servir un archivo almacenado")
    @GetMapping("/{subdirectory}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String subdirectory,
            @PathVariable String filename) {

        try {
            Path file = storageService.resolve(subdirectory + "/" + filename);
            Resource resource = new UrlResource(file.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType;
            try {
                contentType = Files.probeContentType(file);
            } catch (IOException e) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── DTOs internos ─────────────────────────────────────────────────────

    public record ReviewMediaUploadResponse(String mediaUrl, String mediaType) {
    }
}