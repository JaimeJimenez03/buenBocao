package com.buenbocao.api.storage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuración del almacenamiento de archivos.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    /**
     * Directorio base donde se guardan los archivos subidos.
     */
    private String uploadDir = "./uploads";

    /**
     * Tamaño máximo de archivo en bytes (por defecto 5MB).
     */
    private long maxFileSize = 52428800;
}
