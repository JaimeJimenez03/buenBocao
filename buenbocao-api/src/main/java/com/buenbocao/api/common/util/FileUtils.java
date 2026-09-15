package com.buenbocao.api.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * Utilidades para manejo de archivos e imágenes.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUtils {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp");

    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = Set.of(
            "mp4", "mov", "avi", "webm");

    public static String generateUniqueFileName(String originalFilename) {
        String extension = getExtension(originalFilename);
        return UUID.randomUUID() + "." + extension;
    }

    public static String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    public static boolean isValidImageExtension(String filename) {
        return ALLOWED_IMAGE_EXTENSIONS.contains(getExtension(filename));
    }

    public static boolean isValidVideoExtension(String filename) {
        return ALLOWED_VIDEO_EXTENSIONS.contains(getExtension(filename));
    }
}
