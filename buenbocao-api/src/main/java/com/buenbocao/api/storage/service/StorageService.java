package com.buenbocao.api.storage.service;

import com.buenbocao.api.common.exception.BusinessException;
import com.buenbocao.api.common.util.FileUtils;
import com.buenbocao.api.storage.config.StorageProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Servicio de almacenamiento de archivos en el sistema de ficheros local.
 * Gestiona la subida, eliminación y resolución de rutas de archivos.
 * Soporta imágenes y vídeos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final StorageProperties storageProperties;
    private Path rootLocation;

    @PostConstruct
    public void init() {
        rootLocation = Paths.get(storageProperties.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
            Files.createDirectories(rootLocation.resolve("profiles"));
            Files.createDirectories(rootLocation.resolve("recipes"));
            Files.createDirectories(rootLocation.resolve("reviews"));
            log.info("Directorio de almacenamiento inicializado: {}", rootLocation);
        } catch (IOException e) {
            throw new BusinessException("No se pudo crear el directorio de almacenamiento: " + e.getMessage());
        }
    }

    /**
     * Sube un archivo (imagen o vídeo) al subdirectorio indicado.
     *
     * @param file         Archivo a subir
     * @param subdirectory Subdirectorio ("profiles", "recipes", "reviews", etc.)
     * @return Ruta relativa del archivo guardado
     */
    public String store(MultipartFile file, String subdirectory) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = FileUtils.generateUniqueFileName(originalFilename);

        try {
            Path destinationDir = rootLocation.resolve(subdirectory).normalize();
            Files.createDirectories(destinationDir);

            Path destinationFile = destinationDir.resolve(uniqueFilename).normalize();

            if (!destinationFile.startsWith(rootLocation)) {
                throw new BusinessException("No se puede almacenar el archivo fuera del directorio permitido");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            String relativePath = subdirectory + "/" + uniqueFilename;
            log.info("Archivo almacenado: {}", relativePath);
            return relativePath;

        } catch (IOException e) {
            throw new BusinessException("Error al almacenar el archivo: " + e.getMessage());
        }
    }

    /**
     * Detecta si el archivo es imagen o vídeo.
     */
    public boolean isVideo(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) return false;
        return FileUtils.isValidVideoExtension(filename);
    }

    /**
     * Elimina un archivo por su ruta relativa.
     */
    public void delete(String relativePath) {
        try {
            Path file = rootLocation.resolve(relativePath).normalize();
            if (!file.startsWith(rootLocation)) {
                throw new BusinessException("Ruta de archivo no válida");
            }
            Files.deleteIfExists(file);
            log.info("Archivo eliminado: {}", relativePath);
        } catch (IOException e) {
            log.warn("No se pudo eliminar el archivo {}: {}", relativePath, e.getMessage());
        }
    }

    /**
     * Obtiene la ruta absoluta de un archivo.
     */
    public Path resolve(String relativePath) {
        return rootLocation.resolve(relativePath).normalize();
    }

    // ── Validaciones ──────────────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("El archivo está vacío");
        }

        if (file.getSize() > storageProperties.getMaxFileSize()) {
            throw new BusinessException(String.format(
                    "El archivo supera el tamaño máximo permitido (%d MB)",
                    storageProperties.getMaxFileSize() / (1024 * 1024)));
        }

        String filename = file.getOriginalFilename();
        if (filename == null
                || (!FileUtils.isValidImageExtension(filename)
                        && !FileUtils.isValidVideoExtension(filename))) {
            throw new BusinessException(
                    "Formato de archivo no soportado. Usa: jpg, jpeg, png, gif, webp, mp4, mov, avi");
        }

        // Validar magic bytes para prevenir archivos con extensión falsificada
        validateMagicBytes(file, filename);
    }

    private void validateMagicBytes(MultipartFile file, String filename) {
        try {
            byte[] header = new byte[12];
            int bytesRead;
            try (InputStream is = file.getInputStream()) {
                bytesRead = is.read(header);
            }
            if (bytesRead < 4) {
                throw new BusinessException("El archivo está dañado o vacío");
            }

            boolean isImage = FileUtils.isValidImageExtension(filename);
            boolean isVideo = FileUtils.isValidVideoExtension(filename);

            if (isImage && !isValidImageMagic(header, filename)) {
                log.warn("[STORAGE] Archivo rechazado: extensión de imagen con contenido no válido — {}", filename);
                throw new BusinessException("El contenido del archivo no coincide con su extensión");
            }

            if (isVideo && !isValidVideoMagic(header)) {
                log.warn("[STORAGE] Archivo rechazado: extensión de vídeo con contenido no válido — {}", filename);
                throw new BusinessException("El contenido del archivo no coincide con su extensión");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            throw new BusinessException("No se pudo leer el archivo: " + e.getMessage());
        }
    }

    private boolean isValidImageMagic(byte[] h, String filename) {
        String ext = FileUtils.getExtension(filename);
        return switch (ext) {
            case "jpg", "jpeg" -> h[0] == (byte) 0xFF && h[1] == (byte) 0xD8 && h[2] == (byte) 0xFF;
            case "png" -> h[0] == (byte) 0x89 && h[1] == 0x50 && h[2] == 0x4E && h[3] == 0x47;
            case "gif" -> h[0] == 0x47 && h[1] == 0x49 && h[2] == 0x46;
            case "webp" -> h[0] == 0x52 && h[1] == 0x49 && h[2] == 0x46 && h[3] == 0x46;
            default -> true;
        };
    }

    private boolean isValidVideoMagic(byte[] h) {
        // MP4 / MOV — ftyp box en bytes 4-7
        boolean isMp4 = h.length >= 8 && h[4] == 0x66 && h[5] == 0x74 && h[6] == 0x79 && h[7] == 0x70;
        // WebM — EBML header
        boolean isWebm = h[0] == (byte) 0x1A && h[1] == 0x45 && h[2] == (byte) 0xDF && h[3] == (byte) 0xA3;
        // AVI — RIFF....AVI
        boolean isAvi = h[0] == 0x52 && h[1] == 0x49 && h[2] == 0x46 && h[3] == 0x46;
        return isMp4 || isWebm || isAvi;
    }
}
